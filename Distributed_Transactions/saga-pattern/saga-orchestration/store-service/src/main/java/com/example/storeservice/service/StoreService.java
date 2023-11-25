package com.example.storeservice.service;


import com.example.storeservice.entity.Stores;
import com.example.storeservice.entity.StoresHistory;
import com.example.storeservice.repository.IStoreHistoryRepository;
import com.example.storeservice.repository.IStoreRepository;
import com.saga.orchestration.enums.OrderStatus;
import com.saga.orchestration.enums.StoreStatus;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j // Lombok annotation
@Service
public class StoreService {

    @Autowired
    private RabbitTemplate template;

    @Autowired
    IStoreRepository storeRepository;

    @Autowired
    IStoreHistoryRepository storeHistoryRepository;

    @Value("${order.send.store.queue-name}")
    private String ORDER_SEND_STORE_QUEUE;


    @Value("${order.reply.exchange-name}")
    private String ORDER_REPLY_EXCHANGE;
    @Value("${order.reply.store.routing-key}")
    private String ORDER_REPLY_STORE_ROUTING_KEY;

    // order consumer.
    @RabbitListener(queues = "${order.send.store.queue-name}")
    public void orderConsumer(OrderEvent orderEvent) {
        System.out.println("Received message: " + orderEvent.toString() + " from queue: " + ORDER_SEND_STORE_QUEUE);
        if(orderEvent.getStatus().equals(OrderStatus.ORDER_CREATED)){
            createStore(orderEvent);
        }
        else if(orderEvent.getStatus().equals(OrderStatus.ORDER_CANCELLED)){
            rejectStore(orderEvent);
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    private void rejectStore(OrderEvent orderEvent){
        log.info("In function rejectStore:: orderEvent: "+ orderEvent.toString());
        Stores store = storeRepository.findByOrderId(orderEvent.getOrderId());
        if(store!=null && !store.getStatus().equals(StoreStatus.STORE_REJECTED.toString())){
            store.setStatus(StoreStatus.STORE_REJECTED.toString());
            storeRepository.save(store);
            publishStoreHistory(store,StoreStatus.STORE_REJECTED.toString());
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    private void createStore(OrderEvent orderEvent){
        // Two cases either store reserve or store not found.
        log.info("In function createStore:: orderEvent: "+ orderEvent.toString());
        Optional<Stores> store = storeRepository.findFirstByStatusIsNullOrderByCreatedAtAsc();
        if(store.isPresent()){
            log.info("Store Present "+ store.get().toString());
            store.get().setStatus(StoreStatus.STORE_RESERVED.toString());
            store.get().setOrderId(orderEvent.getOrderId());
            storeRepository.save(store.get());
            publishStoreHistory(store.get(),StoreStatus.STORE_RESERVED.toString());

            /*
             * Send to -- orders reply
             */
            StoreEvent storeEvent = new StoreEvent();
            storeEvent.setOrderId(orderEvent.getOrderId());
            storeEvent.setStatus(StoreStatus.STORE_RESERVED);
            storeEvent.setStoreId(store.get().getId());
            template.convertAndSend(ORDER_REPLY_EXCHANGE,ORDER_REPLY_STORE_ROUTING_KEY,storeEvent);
        }
        else{
            /*
             * Send to -- orders reply
             */
            log.info("In function createStore:: orderEvent: "+ "Store not found rejectOrder");
            StoreEvent storeEvent = new StoreEvent();
            storeEvent.setOrderId(orderEvent.getOrderId());
            storeEvent.setStatus(StoreStatus.STORE_REJECTED);
            template.convertAndSend(ORDER_REPLY_EXCHANGE,ORDER_REPLY_STORE_ROUTING_KEY,storeEvent);
        }
    }


    public List<Stores> createStore(){
        for(int i=1;i<=5;i++){
            Stores stores = new Stores();
            stores.setId(UUID.randomUUID());
            stores.setFoodItem("pizza_"+ i);
            storeRepository.save(stores);
        }

        return storeRepository.findAll();
    }

    public void publishStoreHistory(Stores store, String status){
        StoresHistory storesHistory = new StoresHistory();
        storesHistory.setId(UUID.randomUUID());
        storesHistory.setStoreId(store);
        storesHistory.setStatus(status);
        storeHistoryRepository.save(storesHistory);
    }


}
