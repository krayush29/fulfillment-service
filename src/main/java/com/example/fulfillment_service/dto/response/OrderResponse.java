package com.example.fulfillment_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    @JsonProperty("restaurant_id")
    private Long restaurantID;

    @JsonProperty("status")
    private String status;
}
