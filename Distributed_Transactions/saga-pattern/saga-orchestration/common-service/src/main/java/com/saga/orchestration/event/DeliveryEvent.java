package com.saga.orchestration.event;

import com.saga.orchestration.enums.DeliveryStatus;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class DeliveryEvent {
    private UUID orderId;
    private DeliveryStatus status;// either created/cancelled
    private UUID deliveryId;
}

