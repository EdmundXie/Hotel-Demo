package cn.itcast.hotel.constants;

/**
 * @Projectname: hotel-demo
 * @Filename: MqConstants
 * @Author: EdmundXie
 * @Data:2023/3/24 18:51
 * @Email: 609031809@qq.com
 * @Description:
 */

public class MqConstants {
    /**
     * 交换机
     */
    public final static String HOTEL_EXCHANGE = "hotel.topic";

    /**
     * 监听修改队列
     */
    public final static String HOTEL_INSERT_QUEUE = "hotel.insert.queue";

    /**
     * 监听删除队列
     */
    public final static String HOTEL_DELETE_QUEUE = "hotel.delete.queue";

    /**
     * 修改routingKey
     */
    public final static String HOTEL_INSERT_KEY = "hotel.insert";
    public final static String HOTEL_DELETE_KEY = "hotel.delete";

}
