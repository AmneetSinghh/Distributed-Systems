package com.example.orderservice.entity;


import com.saga.orchestration.enums.DeliveryStatus;
import com.saga.orchestration.enums.OrderStatus;
import com.saga.orchestration.enums.StoreStatus;
import com.saga.orchestration.event.OrderEvent;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Entity(name = "orders")
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
public class Orders {

    @Id
    @Column(name = "order_id", updatable = false, nullable = false)
    private UUID id;
    @Column(name = "store_id")
    private UUID storeId;
    @Column(name = "delivery_id")
    private UUID deliveryId;
    @Column(name = "store_status")
    private String storeStatus;
    @Column(name = "delivery_status")
    private String deliveryStatus;
    @Column(name = "order_status")
    private String orderStatus;
    @Column(name = "food_item")
    private String foodItem;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public OrderEvent convertEntityToEvent(){
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setOrderId(getId());
        OrderStatus status = OrderStatus.valueOf(getOrderStatus().toUpperCase()); // Convert string to enum
        orderEvent.setStatus(status);
        return orderEvent;
    }
}
