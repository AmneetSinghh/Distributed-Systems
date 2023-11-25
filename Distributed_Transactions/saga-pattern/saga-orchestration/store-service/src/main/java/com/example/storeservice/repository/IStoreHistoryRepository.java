package com.example.storeservice.repository;

import com.example.storeservice.entity.StoresHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IStoreHistoryRepository extends JpaRepository<StoresHistory, UUID> {

}