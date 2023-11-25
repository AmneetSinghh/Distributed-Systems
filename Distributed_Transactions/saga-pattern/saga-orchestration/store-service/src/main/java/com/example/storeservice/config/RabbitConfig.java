package com.example.storeservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    /*
     * ORDER SEND QUEUES
     */
    @Value("${order.send.store.queue-name}")
    private String ORDER_SEND_STORE_QUEUE;
    @Value("${order.send.store.routing-key}")
    private String ORDER_SEND_STORE_ROUTING_KEY;
    @Value("${order.send.delivery.queue-name}")
    private String ORDER_SEND_DELIVERY_QUEUE;
    @Value("${order.send.delivery.routing-key}")
    private String ORDER_SEND_DELIVERY_ROUTING_KEY;
    @Value("${order.send.exchange-name}")
    private String ORDER_SEND_EXCHANGE;


    /*
     * ORDER REPLY QUEUES
     */
    @Value("${order.reply.store.queue-name}")
    private String ORDER_REPLY_STORE_QUEUE;
    @Value("${order.reply.store.routing-key}")
    private String ORDER_REPLY_STORE_ROUTING_KEY;
    @Value("${order.reply.delivery.queue-name}")
    private String ORDER_REPLY_DELIVERY_QUEUE;
    @Value("${order.reply.delivery.routing-key}")
    private String ORDER_REPLY_DELIVERY_ROUTING_KEY;
    @Value("${order.reply.exchange-name}")
    private String ORDER_REPLY_EXCHANGE;




    @Bean
    public Queue orderSendStoreQueue(){
        return new Queue(ORDER_SEND_STORE_QUEUE);
    }

    @Bean
    public Queue orderSendDeliveryQueue(){
        return new Queue(ORDER_SEND_DELIVERY_QUEUE);
    }

    @Bean
    public Queue orderReplyStoreQueue(){
        return new Queue(ORDER_REPLY_STORE_QUEUE);
    }

    @Bean
    public Queue orderReplyDeliveryQueue(){
        return new Queue(ORDER_REPLY_DELIVERY_QUEUE);
    }


    @Bean
    public TopicExchange orderSendExchange(){
        return new TopicExchange(ORDER_SEND_EXCHANGE);
    }

    @Bean
    public TopicExchange orderReplyExchange(){
        return new TopicExchange(ORDER_REPLY_EXCHANGE);
    }

    @Bean
    public Binding binding1(Queue orderSendStoreQueue, TopicExchange orderSendExchange){
        return BindingBuilder
                .bind(orderSendStoreQueue)
                .to(orderSendExchange).
                with(ORDER_SEND_STORE_ROUTING_KEY);
    }

    @Bean
    public Binding binding2(Queue orderSendDeliveryQueue, TopicExchange orderSendExchange){
        return BindingBuilder
                .bind(orderSendDeliveryQueue)
                .to(orderSendExchange).
                with(ORDER_SEND_DELIVERY_ROUTING_KEY);
    }

    @Bean
    public Binding binding3(Queue orderReplyStoreQueue, TopicExchange orderReplyExchange){
        return BindingBuilder
                .bind(orderReplyStoreQueue)
                .to(orderReplyExchange).
                with(ORDER_REPLY_STORE_ROUTING_KEY);
    }

    @Bean
    public Binding binding4(Queue orderReplyDeliveryQueue, TopicExchange orderReplyExchange){
        return BindingBuilder
                .bind(orderReplyDeliveryQueue)
                .to(orderReplyExchange).
                with(ORDER_REPLY_DELIVERY_ROUTING_KEY);
    }

    @Bean
    public MessageConverter converter(){
        // we want the message to be in json format, when published or consumer
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory){
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }

}
