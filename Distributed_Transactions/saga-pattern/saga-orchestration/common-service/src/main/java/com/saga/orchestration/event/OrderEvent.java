package com.saga.orchestration.event;


import com.saga.orchestration.enums.OrderStatus;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class OrderEvent {
    private UUID orderId;
    private OrderStatus status;// either created/cancelled
}
