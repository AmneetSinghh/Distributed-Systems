package com.example.orderservice.repository;

import com.example.orderservice.entity.Orders;
import com.example.orderservice.entity.OrdersHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IOrderHistoryRepository extends JpaRepository<OrdersHistory, UUID> {

}
