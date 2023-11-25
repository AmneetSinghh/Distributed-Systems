package com.example.storeservice.controller;

import com.example.storeservice.entity.Stores;
import com.example.storeservice.service.StoreService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/store")
public class StoreController {


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
    StoreService storeService;

//    @PostMapping("/publish")
//    public String publish(@RequestBody Sample sample){
////        template.convertAndSend(ORDER_SEND_EXCHANGE,ORDER_SEND_STORE_ROUTING_KEY,sample);
////        template.convertAndSend(ORDER_SEND_EXCHANGE,ORDER_SEND_DELIVERY_ROUTING_KEY,sample);
////        template.convertAndSend(ORDER_REPLY_EXCHANGE,ORDER_REPLY_STORE_ROUTING_KEY,sample);
////        template.convertAndSend(ORDER_REPLY_EXCHANGE,ORDER_REPLY_DELIVERY_ROUTING_KEY,sample);
//        return "Success !!";
//    }


    @PostMapping("/insert")
    public ResponseEntity<Object> insert(){
        List<Stores> stores = storeService.createStore();
        return new ResponseEntity<>(stores, HttpStatus.ACCEPTED);
    }

}
