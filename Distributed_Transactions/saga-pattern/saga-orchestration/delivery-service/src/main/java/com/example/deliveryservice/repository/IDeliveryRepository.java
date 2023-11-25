package com.example.deliveryservice.repository;

import com.example.deliveryservice.entity.Delivery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IDeliveryRepository extends JpaRepository<Delivery, UUID> {

    public Delivery findByOrderId(UUID orderId);


    Optional<Delivery> findFirstByStatusIsNullOrderByCreatedAtAsc();

}
