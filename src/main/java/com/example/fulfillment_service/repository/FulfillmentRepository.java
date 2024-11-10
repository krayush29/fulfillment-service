package com.example.fulfillment_service.repository;

import com.example.fulfillment_service.entity.Fulfillment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FulfillmentRepository extends JpaRepository<Fulfillment, Long> {
}
