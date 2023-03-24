package cn.itcast.hotel.config;

import cn.itcast.hotel.constants.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Projectname: hotel-demo
 * @Filename: MqConfig
 * @Author: EdmundXie
 * @Data:2023/3/24 19:04
 * @Email: 609031809@qq.com
 * @Description:
 */
@Configuration
public class MqConfig {
    //创建交换机
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(MqConstants.HOTEL_EXCHANGE,true,false);
    }

    //创建修改新增队列1
    @Bean
    public Queue insertQueue(){
        return new Queue(MqConstants.HOTEL_INSERT_QUEUE,true);
    }

    //创建删除队列2
    @Bean
    public Queue deleteQueue(){
        return new Queue(MqConstants.HOTEL_DELETE_QUEUE,true);
    }

    //添加队列1绑定
    @Bean
    public Binding insertBinding(TopicExchange topicExchange,Queue insertQueue){
        return BindingBuilder.bind(insertQueue).to(topicExchange).with(MqConstants.HOTEL_INSERT_KEY);
    }

    //添加队列2绑定
    @Bean
    public Binding deleteBinding(TopicExchange topicExchange,Queue deleteQueue){
        return BindingBuilder.bind(deleteQueue).to(topicExchange).with(MqConstants.HOTEL_DELETE_KEY);
    }
}
