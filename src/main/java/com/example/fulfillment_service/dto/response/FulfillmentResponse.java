package com.example.fulfillment_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FulfillmentResponse {
    private Long fulfillmentId;
    private Long orderId;
    private Long deliveryPartnerId;
}
