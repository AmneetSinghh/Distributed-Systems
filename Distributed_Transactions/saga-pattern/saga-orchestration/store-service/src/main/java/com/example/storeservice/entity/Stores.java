package com.example.storeservice.entity;

import com.saga.orchestration.enums.StoreStatus;
import com.saga.orchestration.event.StoreEvent;
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
@Entity(name = "stores")
@Table(name = "stores")
@EntityListeners(AuditingEntityListener.class)
public class Stores {

    @Id
    @Column(name = "store_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "order_id")
    private UUID orderId;   // assigned to particular order, in our case it will be assigned to x order always.

    @Column(name = "status")
    private String status;

    @Column(name = "food_item")
    private String foodItem;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public StoreEvent convertEntityToEvent(){
        StoreEvent storeEvent = new StoreEvent();
        storeEvent.setOrderId(getOrderId());
        StoreStatus status = StoreStatus.valueOf(getStatus().toUpperCase()); // Convert string to enum
        storeEvent.setStatus(status);
        storeEvent.setStoreId(getId());
        return storeEvent;
    }
}

