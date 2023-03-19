package cn.itcast.hotel;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

/**
 * @Projectname: hotel-demo
 * @Filename: HotelDocumentTest
 * @Author: EdmundXie
 * @Data:2023/3/19 18:11
 * @Email: 609031809@qq.com
 * @Description:
 */
@SpringBootTest
public class HotelDocumentTest {
    @Autowired
    private IHotelService hotelService;
    private RestHighLevelClient client;

    @Test
    public void testAddDoc() throws IOException {
        //根据id查询
        Hotel hotel = hotelService.getById(609372L);
        HotelDoc hotelDoc = new HotelDoc(hotel);
        //1.新建请求（准备request对象）
        IndexRequest request = new IndexRequest("hotel").id(hotelDoc.getId().toString());

        //2.准备json文档
        request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
        //3. 发送请求
        client.index(request, RequestOptions.DEFAULT);
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
