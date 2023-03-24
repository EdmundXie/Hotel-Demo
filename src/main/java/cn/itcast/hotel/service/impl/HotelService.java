package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    @Autowired
    private RestHighLevelClient client;
    @Override
    public PageResult search(RequestParams requestParams) {
        try {
            int page = requestParams.getPage();
            int size = requestParams.getSize();
            String sortBy = requestParams.getSortBy();
            String location  = requestParams.getLocation();
            //1.准备request对象
            SearchRequest request = new SearchRequest("hotel");

            //构建bool查询
            BoolQueryBuilder boolQuery = buildBoolQuery(requestParams);

            //新增广告算分控制，构建functionScore查询
            FunctionScoreQueryBuilder functionScoreQuery = buildFunctionScoreQuery(boolQuery);

            request.source().query(functionScoreQuery).size(1000);
            //2 排序

            //2.1 距离排序
            if(!StringUtils.isEmpty(location)){
                request.source().sort(SortBuilders.geoDistanceSort("location",new GeoPoint(location))
                        .order(SortOrder.ASC)
                        .unit(DistanceUnit.KILOMETERS));
            }

            //2.2 价格分数排序
            if("price".equals(sortBy)){
                request.source().sort(sortBy, SortOrder.ASC);
            } else if ("score".equals(sortBy)) {
                request.source().sort(sortBy, SortOrder.DESC);
            }
            request.source().from((page - 1) * size).size(size);
            //3.发送request
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            //解析响应
            return handleResponse(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FunctionScoreQueryBuilder buildFunctionScoreQuery(BoolQueryBuilder boolQuery) {
        FunctionScoreQueryBuilder functionScoreQuery =
                QueryBuilders.functionScoreQuery(
                        boolQuery,
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                            new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                    //过滤条件
                                    QueryBuilders.termQuery("isAD",true),
                                    //算法函数
                                    ScoreFunctionBuilders.weightFactorFunction(10)
                            )
        });
        return functionScoreQuery;
    }

    private BoolQueryBuilder buildBoolQuery(RequestParams requestParams) {
        //1.1 准备query对象
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //2.设置request对象

        if(StringUtils.isEmpty(requestParams.getKey())){
            boolQuery.must(QueryBuilders.matchAllQuery());
        }
        else{
            boolQuery.must(QueryBuilders.matchQuery("all", requestParams.getKey()));
        }

        //2.1 filter过滤器

        //品牌条件
        if(!StringUtils.isEmpty(requestParams.getBrand())){
            boolQuery.filter(QueryBuilders.termQuery("brand", requestParams.getBrand()));
        }
        //星级条件
        if(!StringUtils.isEmpty(requestParams.getStarName())){
            boolQuery.filter(QueryBuilders.termQuery("starName", requestParams.getStarName()));
        }
        //城市条件
        if(!StringUtils.isEmpty(requestParams.getCity())){
            boolQuery.filter(QueryBuilders.termQuery("city", requestParams.getCity()));
        }
        //价格条件
        if(!StringUtils.isEmpty(requestParams.getMinPrice())&&!StringUtils.isEmpty(requestParams.getMaxPrice())){
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(requestParams.getMinPrice()).lte(requestParams.getMaxPrice()));
        }
        return boolQuery;

    }

    private PageResult handleResponse(SearchResponse response) {
        //解析相应成java对象
        SearchHits hits = response.getHits();
        //查询条数
        long total = hits.getTotalHits().value;
        //获得hits数组
        SearchHit[] searchHits = hits.getHits();

        List<HotelDoc> hotelDocList = new ArrayList<>();
        System.out.println("total docs: "+total+"\n");
        //取得source的json数据
        for(SearchHit hit:searchHits){
            String json = hit.getSourceAsString();

//            System.out.println(json);
            HotelDoc hotelDoc = JSON.parseObject(json,HotelDoc.class);
            Object[] sortValues = hit.getSortValues();
            if(sortValues.length>0){
                hotelDoc.setDistance(sortValues[0]);
            }
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if(!CollectionUtils.isEmpty(highlightFields)){
                HighlightField highlightField = highlightFields.get("name");
                if(highlightField!=null){
                    String name = highlightField.getFragments()[0].toString();
                    hotelDoc.setName(name);
                }
            }
            System.out.println(hotelDoc);
            hotelDocList.add(hotelDoc);
        }
        PageResult result = new PageResult();
        result.setHotels(hotelDocList);
        result.setTotal(total);
        return result;
    }

    /**
     * 查询城市、星级、品牌的聚合结果
     * @return 聚合结果, format: {"city" : ["shanghai","beijing"],"starName" : ["five stars","four stars"], ...}
     */
    @Override
    public Map<String, List<String>> filters(RequestParams requestParams) {
        try {
            SearchRequest request = new SearchRequest("hotel");
            BoolQueryBuilder boolQuery = buildBoolQuery(requestParams);
            request.source().query(boolQuery);
            request.source().size(0);
            buildAggregation(request);

            Map<String, List<String>> aggResult = new HashMap<>();
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            List<String> aggByBrand = getAggByName(response,"brandAgg");
            aggResult.put("brand",aggByBrand);
            List<String> aggByCity = getAggByName(response, "cityAgg");
            aggResult.put("city",aggByCity);
            List<String> aggByStarName= getAggByName(response,"starNameAgg");
            aggResult.put("starName",aggByStarName);

            return aggResult;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> suggestions(RequestParams requestParams) {
        try {
            List<String> result = new ArrayList<>();
            String key = requestParams.getKey();
            if(StringUtils.isEmpty(key)){
                return result;
            }
            SearchRequest request = new SearchRequest("hotel");
            request.source().suggest(new SuggestBuilder().addSuggestion(
                    "suggestions",
                    SuggestBuilders.completionSuggestion("suggestion")
                            .prefix(key)
                            .skipDuplicates(true)
                            .size(10)
            ));
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
//        System.out.println(response);

            //处理结果
            Suggest suggest = response.getSuggest();
            CompletionSuggestion suggestion = suggest.getSuggestion("suggestions");
            List<CompletionSuggestion.Entry.Option> options = suggestion.getOptions();
            options.forEach(item -> {
                String text = item.getText().toString();
                result.add(text);
            });

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertById(Long id) {
        try {
            //1 创建请求
            IndexRequest request = new IndexRequest("hotel").id(id.toString());
            //2 设置请求参数

            //2.1 取得hotelDoc对象
            Hotel hotel = this.getById(id);
            HotelDoc hotelDoc = new HotelDoc(hotel);

            //2.2 将hotelDoc对象转为JSON格式
            String json = JSON.toJSONString(hotelDoc);

            request.source(json, XContentType.JSON);
            //3 发送请求
            client.index(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            DeleteRequest request = new DeleteRequest("hotel",id.toString());

            client.delete(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getAggByName(SearchResponse response, String aggName) {
        Terms brandTerms = response.getAggregations().get(aggName);
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        List<String> keys = new ArrayList<>();
        if(!buckets.isEmpty()){
            buckets.forEach(item ->{
                String key = item.getKeyAsString();
                if(!StringUtils.isEmpty(key)){
                    keys.add(key);
                }
            });
        }
        return keys;
    }

    private void buildAggregation(SearchRequest request) {
        request.source().aggregation(AggregationBuilders
                .terms("brandAgg")
                .field("brand")
                .size(50));
        request.source().aggregation(AggregationBuilders
                .terms("cityAgg")
                .field("city")
                .size(50));
        request.source().aggregation(AggregationBuilders
                .terms("starNameAgg")
                .field("starName")
                .size(50));
    }
}
