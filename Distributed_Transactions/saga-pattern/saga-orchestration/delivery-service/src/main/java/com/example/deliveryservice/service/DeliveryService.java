package com.example.deliveryservice.service;


import com.example.deliveryservice.entity.Delivery;
import com.example.deliveryservice.entity.DeliveryHistory;
import com.example.deliveryservice.repository.IDeliveryHistoryRepository;
import com.example.deliveryservice.repository.IDeliveryRepository;
import com.saga.orchestration.enums.DeliveryStatus;
import com.saga.orchestration.enums.OrderStatus;
import com.saga.orchestration.event.DeliveryEvent;
import com.saga.orchestration.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j // Lombok annotation
@Service
public class DeliveryService {

    @Autowired
    private RabbitTemplate template;

    @Autowired
    IDeliveryRepository deliveryRepository;

    @Autowired
    IDeliveryHistoryRepository deliveryHistoryRepository;

    @Value("${order.send.delivery.queue-name}")
    private String ORDER_SEND_DELIVERY_QUEUE;


    @Value("${order.reply.exchange-name}")
    private String ORDER_REPLY_EXCHANGE;
    @Value("${order.reply.delivery.routing-key}")
    private String ORDER_REPLY_DELIVERY_ROUTING_KEY;

    // order consumer.
    @RabbitListener(queues = "${order.send.delivery.queue-name}")
    public void orderConsumer(OrderEvent orderEvent) {
        System.out.println("Received message: " + orderEvent.toString() + " from queue: " + ORDER_SEND_DELIVERY_QUEUE);
        if(orderEvent.getStatus().equals(OrderStatus.ORDER_CREATED)){
            createDeliveryAgent(orderEvent);
        }
        else if(orderEvent.getStatus().equals(OrderStatus.ORDER_CANCELLED)){
            rejectDeliveryAgent(orderEvent);
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    private void rejectDeliveryAgent(OrderEvent orderEvent){
        log.info("In function rejectDeliveryAgent:: orderEvent: "+ orderEvent.toString());
        Delivery delivery = deliveryRepository.findByOrderId(orderEvent.getOrderId());
        if(delivery!=null && !delivery.getStatus().equals(DeliveryStatus.DELIVERY_REJECTED.toString())){
            delivery.setStatus(DeliveryStatus.DELIVERY_REJECTED.toString());
            deliveryRepository.save(delivery);
            publishDeliveryHistory(delivery,DeliveryStatus.DELIVERY_REJECTED.toString());
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    private void createDeliveryAgent(OrderEvent orderEvent){
        // Two cases either store reserve or store not found.
        log.info("In function createDeliveryAgent:: orderEvent: "+ orderEvent.toString());
        Optional<Delivery> delivery = deliveryRepository.findFirstByStatusIsNullOrderByCreatedAtAsc();
        if(delivery.isPresent()){
            log.info("Delivery Agent Present "+ delivery.get());
            delivery.get().setStatus(DeliveryStatus.DELIVERY_RESERVED.toString());
            delivery.get().setOrderId(orderEvent.getOrderId());
            deliveryRepository.save(delivery.get());
            publishDeliveryHistory(delivery.get(),DeliveryStatus.DELIVERY_RESERVED.toString());



            /*
             * Send to -- Order reply
             */

            DeliveryEvent deliveryEvent = new DeliveryEvent();
            deliveryEvent.setOrderId(orderEvent.getOrderId());
            deliveryEvent.setStatus(DeliveryStatus.DELIVERY_RESERVED);
            deliveryEvent.setDeliveryId(delivery.get().getId());
            template.convertAndSend(ORDER_REPLY_EXCHANGE,ORDER_REPLY_DELIVERY_ROUTING_KEY,deliveryEvent);
        }
        else{

            /*
             * Send to -- Order reply
             */

            log.info("In function createDeliveryAgent:: orderEvent: "+ "delivery agent not found rejectOrder");
            DeliveryEvent deliveryEvent = new DeliveryEvent();
            deliveryEvent.setOrderId(orderEvent.getOrderId());
            deliveryEvent.setStatus(DeliveryStatus.DELIVERY_REJECTED);
            template.convertAndSend(ORDER_REPLY_EXCHANGE,ORDER_REPLY_DELIVERY_ROUTING_KEY,deliveryEvent);
        }
    }

    
    public List<Delivery> createDeliveryAgents(){
        for(int i=1;i<=5;i++){
            Delivery delivery = new Delivery();
            delivery.setId(UUID.randomUUID());
            deliveryRepository.save(delivery);
        }

        return deliveryRepository.findAll();
    }

    public void publishDeliveryHistory(Delivery delivery, String status){
        DeliveryHistory deliveryHistory = new DeliveryHistory();
        deliveryHistory.setId(UUID.randomUUID());
        deliveryHistory.setDeliveryId(delivery);
        deliveryHistory.setStatus(status);
        deliveryHistoryRepository.save(deliveryHistory);
    }


}
