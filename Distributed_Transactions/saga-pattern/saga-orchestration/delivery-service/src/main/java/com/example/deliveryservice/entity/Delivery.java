package com.example.deliveryservice.entity;

import com.saga.orchestration.enums.DeliveryStatus;
import com.saga.orchestration.event.DeliveryEvent;
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
@Entity(name = "delivery")
@Table(name = "delivery")
@EntityListeners(AuditingEntityListener.class)
public class Delivery {

    @Id
    @Column(name = "delivery_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "order_id")
    private UUID orderId;   // assigned to particular order, in our case it will be assigned to x order always.

    @Column(name = "status")
    private String status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    public DeliveryEvent convertEntityToEvent(){
        DeliveryEvent deliveryEvent = new DeliveryEvent();
        deliveryEvent.setOrderId(getOrderId());
        DeliveryStatus status = DeliveryStatus.valueOf(getStatus().toUpperCase()); // Convert string to enum
        deliveryEvent.setStatus(status);
        deliveryEvent.setDeliveryId(getId());
        return deliveryEvent;
    }
}

