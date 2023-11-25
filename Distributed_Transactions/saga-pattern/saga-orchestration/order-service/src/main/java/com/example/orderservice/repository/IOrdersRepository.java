package com.example.orderservice.repository;

import com.example.orderservice.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Repository
public interface IOrdersRepository extends JpaRepository<Orders, UUID> {
    @Modifying
    @Query("UPDATE orders o set o.storeStatus =:status, o.storeId = :storeId where o.id = :orderId")
    @Transactional
    int updateStoreStatus(@Param("storeId") UUID storeId, @Param("status") String status, @Param("orderId") UUID orderId);


    @Modifying
    @Query("UPDATE orders o set o.deliveryStatus =:status, o.deliveryId = :deliveryId where o.id = :orderId")
    @Transactional
    int updateDeliveryStatus(@Param("deliveryId") UUID deliveryId, @Param("status") String status, @Param("orderId") UUID orderId);

    @Modifying
    @Query("UPDATE orders o set o.orderStatus =:status where o.id = :orderId")
    @Transactional
    int updateOrderStatus(@Param("status") String status,@Param("orderId") UUID orderId);
}
