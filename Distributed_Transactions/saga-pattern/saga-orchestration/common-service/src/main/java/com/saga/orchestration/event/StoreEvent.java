package com.saga.orchestration.event;

import com.saga.orchestration.enums.StoreStatus;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class StoreEvent {
    private UUID orderId;
    private StoreStatus status;// either created/cancelled
    private UUID storeId;
}
