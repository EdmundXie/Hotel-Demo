package cn.itcast.hotel;

import cn.itcast.hotel.pojo.HotelDoc;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Projectname: hotel-demo
 * @Filename: HotelDocumentTest
 * @Author: EdmundXie
 * @Data:2023/3/19 18:11
 * @Email: 609031809@qq.com
 * @Description:
 */
@SpringBootTest
public class HotelSearchTest {

    private RestHighLevelClient client;

    @Test
    public void testMatchAll() throws IOException {
        //1.准备request对象
        SearchRequest request = new SearchRequest("hotel");
        //2.设置request对象
        request.source().query(QueryBuilders.matchAllQuery()).size(1000);
        //3.发送request
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        //解析相应成java对象
        SearchHits hits = response.getHits();
        //查询条数
        long total = hits.getTotalHits().value;
        //获得hits数组
        SearchHit[] searchHits = hits.getHits();

        List<HotelDoc> hotelDocList = new ArrayList<>();
        //取得source的json数据
        for(SearchHit hit:searchHits){
            String json = hit.getSourceAsString();
            HotelDoc hotelDoc = JSON.parseObject(json,HotelDoc.class);
            hotelDocList.add(hotelDoc);
        }

        //打印结果
        System.out.println(hotelDocList.get(1));
//        System.out.println(response);
    }

    @BeforeEach
    public void setUp(){
        this.client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://124.222.138.40:9200")
        ));
    }

    @AfterEach
    public void destroy() throws IOException {
        this.client.close();
    }
}
