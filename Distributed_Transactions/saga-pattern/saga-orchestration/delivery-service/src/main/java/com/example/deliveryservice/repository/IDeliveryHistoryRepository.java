package com.example.deliveryservice.repository;

import com.example.deliveryservice.entity.DeliveryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IDeliveryHistoryRepository extends JpaRepository<DeliveryHistory, UUID> {

}