package com.example.fulfillment_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fulfillments")
public class Fulfillment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fulfillment_id")
    Long FulfillmentId;

    @Column(nullable = false)
    Long orderId;

    @Column(nullable = false)
    Long deliveryPartnerId;

    public Fulfillment(Long orderId, Long deliveryPartnerId) {
        this.orderId = orderId;
        this.deliveryPartnerId = deliveryPartnerId;
    }
}


