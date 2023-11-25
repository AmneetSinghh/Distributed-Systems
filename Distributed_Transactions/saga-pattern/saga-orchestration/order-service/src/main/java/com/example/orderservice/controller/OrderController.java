package com.example.orderservice.controller;

import com.example.orderservice.dto.PlaceOrderRequestDto;
import com.example.orderservice.dto.PlaceOrderResponseDto;
import com.example.orderservice.entity.Orders;
import com.example.orderservice.service.OrdersService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

/*
1. add common module.
2. use transactional messaging in db update + publish.
3. maintain history as well for update operation.
*/

@RestController
@RequestMapping("/order")
public class OrderController {


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
    OrdersService ordersService;

//    @PostMapping("/publish")
//    public String publish(@RequestBody Sample sample){
////        template.convertAndSend(ORDER_SEND_EXCHANGE,ORDER_SEND_STORE_ROUTING_KEY,sample);
////        template.convertAndSend(ORDER_SEND_EXCHANGE,ORDER_SEND_DELIVERY_ROUTING_KEY,sample);
////        template.convertAndSend(ORDER_REPLY_EXCHANGE,ORDER_REPLY_STORE_ROUTING_KEY,sample);
////        template.convertAndSend(ORDER_REPLY_EXCHANGE,ORDER_REPLY_DELIVERY_ROUTING_KEY,sample);
//        return "Success !!";
//    }


    /*
     * Polling
     * Webhooks
     */
    @PostMapping("/place-order")
    public ResponseEntity<Object> placeOrder(@RequestBody PlaceOrderRequestDto placeOrderRequestDto){
        PlaceOrderResponseDto placeOrderResponseDto = ordersService.placeOrder(placeOrderRequestDto);
        return new ResponseEntity<>(placeOrderResponseDto, HttpStatus.ACCEPTED);
    }

    @GetMapping("/get")
    public ResponseEntity<Object> getOrderById(@RequestParam UUID id){
        System.out.println("id : "+ id);
        Optional<Orders> orders = ordersService.getOrderById(id);
        if(orders.isEmpty()){
            return new ResponseEntity<>("Order not found", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(orders, HttpStatus.BAD_REQUEST);
    }
}