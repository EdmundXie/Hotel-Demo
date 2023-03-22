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
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
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
            String key = requestParams.getKey();
            String sortBy = requestParams.getSortBy();
            String brand     = requestParams.getBrand();
            String city      = requestParams.getCity();
            String starName  = requestParams.getStarName();
            Integer minPrice = requestParams.getMinPrice();
            Integer maxPrice = requestParams.getMaxPrice();
            String location  = requestParams.getLocation();
            //1.准备request对象
            SearchRequest request = new SearchRequest("hotel");

            //构建bool查询
            BoolQueryBuilder boolQuery = buildBoolQuery(key, brand, city, starName, minPrice, maxPrice);

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

    private BoolQueryBuilder buildBoolQuery(String key, String brand, String city, String starName, Integer minPrice, Integer maxPrice) {
        //1.1 准备query对象
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //2.设置request对象

        if(StringUtils.isEmpty(key)){
            boolQuery.must(QueryBuilders.matchAllQuery());
        }
        else{
            boolQuery.must(QueryBuilders.matchQuery("all", key));
        }

        //2.1 filter过滤器

        //品牌条件
        if(!StringUtils.isEmpty(brand)){
            boolQuery.filter(QueryBuilders.termQuery("brand", brand));
        }
        //星级条件
        if(!StringUtils.isEmpty(starName)){
            boolQuery.filter(QueryBuilders.termQuery("starName", starName));
        }
        //城市条件
        if(!StringUtils.isEmpty(city)){
            boolQuery.filter(QueryBuilders.termQuery("city", city));
        }
        //价格条件
        if(!StringUtils.isEmpty(minPrice)&&!StringUtils.isEmpty(maxPrice)){
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(minPrice).lte(maxPrice));
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
}
