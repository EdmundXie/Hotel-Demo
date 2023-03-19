package cn.itcast.hotel;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static cn.itcast.hotel.constants.HotelConstants.MAPPING_TEMPLATE;

/**
 * @Projectname: hotel-demo
 * @Filename: HotelIndexTest
 * @Author: EdmundXie
 * @Data:2023/3/19 16:17
 * @Email: 609031809@qq.com
 * @Description:
 */
public class HotelIndexTest {
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void testInit(){
        System.out.println(restHighLevelClient);
    }

    //新增索引（表）
    @Test
    public void createHotelIndex() throws IOException {
        //1.创建request对象
        CreateIndexRequest request = new CreateIndexRequest("hotel");
        //2.给request设值
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        //3.发送request
        restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);//后一个参数为请求头的设置，这里为默认
    }

    //删除索引
    @Test
    public void deleteHotelIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("hotel");
        restHighLevelClient.indices().delete(request,RequestOptions.DEFAULT);
    }

    //判断索引是否存在
    @Test
    public void existHotelIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("hotel");
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    @BeforeEach
    public void setUp(){
        this.restHighLevelClient = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://124.222.138.40:9200")
        ));
    }

    @AfterEach
    public void destroy() throws IOException {
        this.restHighLevelClient.close();
    }
}
