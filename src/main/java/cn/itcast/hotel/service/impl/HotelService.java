package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
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
            //1.准备request对象
            SearchRequest request = new SearchRequest("hotel");
            //2.设置request对象
            if(StringUtils.isEmpty(key)){
                request.source().query(QueryBuilders.matchAllQuery());
            }
            else{
            request.source().query(QueryBuilders.matchQuery("all",key)).size(1000);
            }
            //排序
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
