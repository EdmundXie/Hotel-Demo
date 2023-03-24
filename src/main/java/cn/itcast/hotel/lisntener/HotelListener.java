package cn.itcast.hotel.lisntener;

import cn.itcast.hotel.constants.MqConstants;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Projectname: hotel-demo
 * @Filename: HotelListener
 * @Author: EdmundXie
 * @Data:2023/3/24 19:37
 * @Email: 609031809@qq.com
 * @Description:
 */
@Component
public class HotelListener {
    /**
     * 测试增加queue
     * @param message
     */
    @Autowired
    private IHotelService hotelService;
    @RabbitListener(queues = MqConstants.HOTEL_INSERT_QUEUE)
    public void listenAddQueue(Long id){
        hotelService.insertById(id);
    }

    /**
     * 测试删除queue
     * @param message
     */
    @RabbitListener(queues = MqConstants.HOTEL_DELETE_QUEUE)
    public void listenDeleteQueue(Long id){
        hotelService.deleteById(id);
    }
}
