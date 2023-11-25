package com.example.orderservice.dto;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class OrdersDto {
    private UUID id;
    private UUID storeId;
    private UUID deliveryId;
    private String storeStatus;
    private String deliveryStatus;
    private String orderStatus;
}
