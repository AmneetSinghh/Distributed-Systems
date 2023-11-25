package com.example.orderservice.service;

import com.example.orderservice.dto.PlaceOrderRequestDto;
import com.example.orderservice.dto.PlaceOrderResponseDto;
import com.example.orderservice.entity.Orders;
import com.example.orderservice.entity.OrdersHistory;
import com.example.orderservice.repository.IOrderHistoryRepository;
import com.example.orderservice.repository.IOrdersRepository;
import com.saga.orchestration.enums.DeliveryStatus;
import com.saga.orchestration.enums.OrderStatus;
import com.saga.orchestration.enums.StoreStatus;
import com.saga.orchestration.event.DeliveryEvent;
import com.saga.orchestration.event.OrderEvent;
import com.saga.orchestration.event.StoreEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j // Lombok annotation

@Service
public class OrdersService {
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



    @Value("${order.reply.store.queue-name}")
    private String ORDER_REPLY_STORE_QUEUE;

    @Value("${order.reply.delivery.queue-name}")
    private String ORDER_REPLY_DELIVERY_QUEUE;

    @Autowired
    IOrdersRepository ordersRepository;

    @Autowired
    IOrderHistoryRepository orderHistoryRepository;

    HashMap<UUID, List<String>> eventReceivedMap = new HashMap<>();

    // Listener of Delivery
    // we can use cron here. if both events received at same time, then also no issues.....
    // for now we wil only implement hashmap approach......




    @RabbitListener(queues = "${order.reply.delivery.queue-name}")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void DeliveryReplyListener(DeliveryEvent deliveryEvent) {
        System.out.println("Received message: " + deliveryEvent.toString() + " from queue: " + ORDER_REPLY_DELIVERY_QUEUE);
        if(deliveryEvent.getStatus().equals(DeliveryStatus.DELIVERY_RESERVED)){
            eventReceivedMap.get(deliveryEvent.getOrderId()).add(deliveryEvent.getStatus().toString());
        }
        // Process the received message here
        Optional<Orders> order = getOrderById(deliveryEvent.getOrderId());
        if(order.isPresent()){
            if(!Objects.equals(order.get().getOrderStatus(), OrderStatus.ORDER_CANCELLED.toString())){
                log.info("DeliveryReplyListener:: Order cancelled ");
                if(deliveryEvent.getStatus().equals(DeliveryStatus.DELIVERY_REJECTED)){
                    publishOrderHistory(order.get(),DeliveryStatus.DELIVERY_REJECTED.toString());
                    order.get().setOrderStatus(OrderStatus.ORDER_CANCELLED.toString());
                    order.get().setDeliveryStatus(DeliveryStatus.DELIVERY_REJECTED.toString());
                    ordersRepository.save(order.get());
                    publishOrderHistory(order.get(), OrderStatus.ORDER_CANCELLED.toString());

                    /*
                     * Rollback
                     */
                    OrderEvent orderEvent = order.get().convertEntityToEvent();
                    template.convertAndSend(ORDER_SEND_EXCHANGE, ORDER_SEND_STORE_ROUTING_KEY, orderEvent);
                    template.convertAndSend(ORDER_SEND_EXCHANGE, ORDER_SEND_DELIVERY_ROUTING_KEY, orderEvent);
                } else {
                    log.info("DeliveryReplyListener:: Order pending :: delivery reserved ");
                    publishOrderHistory(order.get(), DeliveryStatus.DELIVERY_RESERVED.toString());
                    ordersRepository.updateDeliveryStatus(deliveryEvent.getDeliveryId(), DeliveryStatus.DELIVERY_RESERVED.toString(), order.get().getId());
                }
            }
        }

        /*
         * Check if both are reserved- but db call is slow,,, while reading, if they commit after read not work.
         * I not want to do 1 by 1, because
         * Maintain hashmap is not feseable for large scale.
         * Redis can help in this.
         */


        // means both are received....
        List<String> events = eventReceivedMap.getOrDefault(deliveryEvent.getOrderId(),null);
        if(events!=null && events.size() == 2){
            eventReceivedMap.remove(deliveryEvent.getOrderId());
            ordersRepository.updateOrderStatus(OrderStatus.ORDER_COMPLETED.toString(),deliveryEvent.getOrderId());
            publishOrderHistory(getOrderById(deliveryEvent.getOrderId()).get(),OrderStatus.ORDER_COMPLETED.toString());
        }
    }

    @RabbitListener(queues = "${order.reply.store.queue-name}")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void storeReplyListener(StoreEvent storeEvent) {
        System.out.println("Received message: " + storeEvent.toString() + " from queue: " + ORDER_REPLY_STORE_QUEUE);
        if(storeEvent.getStatus().equals(StoreStatus.STORE_RESERVED)){
            eventReceivedMap.get(storeEvent.getOrderId()).add(storeEvent.getStatus().toString());
        }
        Optional<Orders> order = getOrderById(storeEvent.getOrderId());
        if (order.isPresent()) {
            if (!Objects.equals(order.get().getOrderStatus(), OrderStatus.ORDER_CANCELLED.toString())) {
                log.info("storeReplyListener:: Order cancelled ");
                if (storeEvent.getStatus().equals(StoreStatus.STORE_REJECTED)) {
                    publishOrderHistory(order.get(), StoreStatus.STORE_REJECTED.toString());
                    order.get().setOrderStatus(OrderStatus.ORDER_CANCELLED.toString());
                    order.get().setStoreStatus(StoreStatus.STORE_REJECTED.toString());
                    ordersRepository.save(order.get());
                    publishOrderHistory(order.get(), OrderStatus.ORDER_CANCELLED.toString());

                    OrderEvent orderEvent = order.get().convertEntityToEvent();
                    template.convertAndSend(ORDER_SEND_EXCHANGE, ORDER_SEND_STORE_ROUTING_KEY, orderEvent);
                    template.convertAndSend(ORDER_SEND_EXCHANGE, ORDER_SEND_DELIVERY_ROUTING_KEY, orderEvent);
                } else {
                    log.info("storeReplyListener:: Order pending :: store reserved ");
                    // reserve store only
                    publishOrderHistory(order.get(), StoreStatus.STORE_RESERVED.toString());
                    ordersRepository.updateStoreStatus(storeEvent.getStoreId(), StoreStatus.STORE_RESERVED.toString(), order.get().getId());
                }
            }
        }

        /*
         * Check if both are reserved
         */

        // means both are received....
        List<String> events = eventReceivedMap.getOrDefault(storeEvent.getOrderId(),null);
        if(events!=null && events.size() == 2){
            eventReceivedMap.remove(storeEvent.getOrderId());
            ordersRepository.updateOrderStatus(OrderStatus.ORDER_COMPLETED.toString(),storeEvent.getOrderId());
            publishOrderHistory(getOrderById(storeEvent.getOrderId()).get(),OrderStatus.ORDER_COMPLETED.toString());
        }
    }


    /*
     * Orchestrator.
     */
    @Transactional
    public PlaceOrderResponseDto placeOrder(PlaceOrderRequestDto placeOrderRequestDto){
        Orders order = placeOrderRequestDto.convertDtoToEntity();
        ordersRepository.save(order);
        eventReceivedMap.put(order.getId(),new ArrayList<>());
        publishOrderHistory(order, order.getOrderStatus());
        OrderEvent orderEvent = order.convertEntityToEvent();
        template.convertAndSend(ORDER_SEND_EXCHANGE,ORDER_SEND_STORE_ROUTING_KEY,orderEvent);
        template.convertAndSend(ORDER_SEND_EXCHANGE,ORDER_SEND_DELIVERY_ROUTING_KEY,orderEvent);
        return new PlaceOrderResponseDto(order.getId(),order.getOrderStatus());
    }


    public void publishOrderHistory(Orders order, String status){
        OrdersHistory ordersHistory = new OrdersHistory();
        ordersHistory.setId(UUID.randomUUID());
        ordersHistory.setOrderId(order);
        ordersHistory.setStatus(status);
        orderHistoryRepository.save(ordersHistory);
    }

    public void updateStoreStatus(UUID storeId, String status, UUID orderId){
       int id =  ordersRepository.updateStoreStatus(storeId,status,orderId);
        System.out.println("updateStoreStatus " + id);
    }

    public void updateDeliveryStatus(UUID deliveryId, String status, UUID orderId){
        int id =  ordersRepository.updateDeliveryStatus(deliveryId,status,orderId);
        System.out.println("updateDeliveryStatus " + id);
    }

    public Optional<Orders> getOrderById(UUID id){
        return ordersRepository.findById(id);
    }

}
