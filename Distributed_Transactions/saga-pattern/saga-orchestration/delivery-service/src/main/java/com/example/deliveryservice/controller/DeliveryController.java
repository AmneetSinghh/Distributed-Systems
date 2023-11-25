package com.example.deliveryservice.controller;

import com.example.deliveryservice.entity.Delivery;
import com.example.deliveryservice.service.DeliveryService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delivery")
public class DeliveryController {


    @Autowired
    private RabbitTemplate template;
    @Value("${order.send.exchange-name}")
    private String ORDER_SEND_EXCHANGE;
    @Value("${order.send.store.routing-key}")
    private String ORDER_SEND_STORE_ROUTING_KEY;
    @Value("${order.send.delivery.routing-key}")
    private String ORDER_SEND_DELIVERY_ROUTING_KEY;

    @Value("${order.reply.exchange-name}")
    private String ORDER_REPLY_EXCHANGE;
    @Value("${order.reply.store.routing-key}")
    private String ORDER_REPLY_STORE_ROUTING_KEY;
    @Value("${order.reply.delivery.routing-key}")
    private String ORDER_REPLY_DELIVERY_ROUTING_KEY;


    @Autowired
    DeliveryService deliveryService;

    @PostMapping("/insert")
    public ResponseEntity<Object> insert(){
        List<Delivery> deliveries = deliveryService.createDeliveryAgents();
        return new ResponseEntity<>(deliveries, HttpStatus.ACCEPTED);
    }

}
