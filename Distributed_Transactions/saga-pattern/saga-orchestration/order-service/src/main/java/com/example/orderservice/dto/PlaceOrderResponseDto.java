package com.example.orderservice.dto;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class PlaceOrderResponseDto {
    private UUID orderId;
    private String status;
}
