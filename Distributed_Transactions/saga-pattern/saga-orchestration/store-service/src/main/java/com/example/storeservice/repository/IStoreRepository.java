package com.example.storeservice.repository;

import com.example.storeservice.entity.Stores;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IStoreRepository extends JpaRepository<Stores, UUID> {

    public Stores findByOrderId(UUID orderId);


    Optional<Stores> findFirstByStatusIsNullOrderByCreatedAtAsc();

}
