package cn.itcast.hotel;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

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

    //新增文档
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

    //根据id查询文档
    @Test
    public void testGetDocById() throws IOException {
        GetRequest request = new GetRequest("hotel","609372");
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        String json = response.getSourceAsString();
        HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
        System.out.println(hotelDoc);
    }
    //根据id局部更新文档
    @Test
    public void testUpdateDocById() throws IOException {
        //1 创建请求
        UpdateRequest request = new UpdateRequest("hotel","609372");
        //2 设置请求参数
        request.doc(
                "price","551",
                "starName","五星"
        );
        //3 发送请求
        client.update(request,RequestOptions.DEFAULT);
    }

    //批量操作文档
    @Test
    public void testBulkRequest() throws IOException {
        List<Hotel> list = hotelService.list();
        BulkRequest bulkRequest = new BulkRequest();
        list.forEach(item ->{
            HotelDoc hotelDoc = new HotelDoc(item);
            bulkRequest.add(new IndexRequest("hotel")
                    .id(hotelDoc.getId().toString())
                    .source(JSON.toJSONString(hotelDoc),XContentType.JSON));

        });
        client.bulk(bulkRequest,RequestOptions.DEFAULT);
    }

    //删除文档
    @Test
    public void testDeleteDocById() throws IOException {
        DeleteRequest request = new DeleteRequest("hotel","609372");

        client.delete(request,RequestOptions.DEFAULT);
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
