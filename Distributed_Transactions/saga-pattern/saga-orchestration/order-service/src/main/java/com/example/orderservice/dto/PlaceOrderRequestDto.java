package com.example.orderservice.dto;

import com.example.orderservice.entity.Orders;
import com.saga.orchestration.enums.OrderStatus;
import com.saga.orchestration.event.OrderEvent;
import lombok.*;
import org.hibernate.criterion.Order;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class PlaceOrderRequestDto {
    private String food;


    public Orders convertDtoToEntity(){
       Orders order = new Orders();
       order.setOrderStatus(OrderStatus.ORDER_CREATED.toString());
       order.setId(UUID.randomUUID());
       order.setFoodItem(getFood());
       return order;
    }

}
