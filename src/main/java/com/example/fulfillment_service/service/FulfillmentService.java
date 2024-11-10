package com.example.fulfillment_service.service;

import com.example.fulfillment_service.dto.request.FulfillmentRequest;
import com.example.fulfillment_service.dto.request.OrderStatusUpdateRequest;
import com.example.fulfillment_service.dto.response.DeliveryPartnerResponse;
import com.example.fulfillment_service.dto.response.FulfillmentResponse;
import com.example.fulfillment_service.dto.response.OrderResponse;
import com.example.fulfillment_service.entity.Fulfillment;
import com.example.fulfillment_service.exception.CatalogServiceException;
import com.example.fulfillment_service.exception.FulfillmentNotFoundException;
import com.example.fulfillment_service.exception.OrderRequestException;
import com.example.fulfillment_service.exception.OrderServiceException;
import com.example.fulfillment_service.repository.FulfillmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class FulfillmentService {

    @Autowired
    private FulfillmentRepository fulfillmentRepository;

    @Autowired
    private RestTemplate restTemplate;

    public FulfillmentResponse createFulfillment(FulfillmentRequest request) {
        log.info("Creating fulfillment for order: {}", request.getOrderId());

        // Step 1: Fetch the order from order service
        OrderResponse order = fetchOrderFromOrderService(request.getOrderId());
        Long restaurantId = order.getRestaurantID();

        // Step 2: Fetch the nearest delivery partner from catalog service
        Long deliveryPartnerId = fetchDeliveryPartners(restaurantId);

        // Step 3: Update order status to done
        updateOrderStatusToDone(request.getOrderId());

        // Step 4: Save the Fulfillment
        Fulfillment fulfillment = new Fulfillment(request.getOrderId(), deliveryPartnerId);
        log.info("Saving fulfillment: {}", fulfillment);
        fulfillmentRepository.save(fulfillment);

        // Return the FulfillmentResponse
        return new FulfillmentResponse(fulfillment.getFulfillmentId(), fulfillment.getOrderId(), fulfillment.getDeliveryPartnerId());
    }

    public FulfillmentResponse getFulfillment(Long fulfillmentId) {
        Fulfillment fulfillment = fulfillmentRepository.findById(fulfillmentId).orElseThrow(() -> new FulfillmentNotFoundException("Fulfillment not found"));
        return new FulfillmentResponse(fulfillment.getFulfillmentId(), fulfillment.getOrderId(), fulfillment.getDeliveryPartnerId());
    }

    public OrderResponse fetchOrderFromOrderService(Long orderId) {
        log.info("Fetching order from order service: {}", orderId);
        try {
            String orderUrl = "http://localhost:8081/orders/" + orderId;
            ResponseEntity<OrderResponse> response = restTemplate.getForEntity(orderUrl, OrderResponse.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new OrderServiceException("Failed to fetch order from order service, status code: " + response.getStatusCode());
            }

            if (Objects.equals(Objects.requireNonNull(response.getBody()).getStatus(), "DONE")) {
                throw new OrderRequestException("Order is already fulfilled");
            }

            return response.getBody();
        } catch (OrderRequestException e) {
            throw new OrderRequestException(e.getMessage());
        } catch (Exception e) {
            // Handle the exception appropriately
            throw new OrderServiceException(e.getMessage());
        }
    }

    public void updateOrderStatusToDone(Long orderId) {
        log.info("Updating order status to DONE for order: {}", orderId);
        try {
            String updateOrderUrl = "http://localhost:8081/orders/" + orderId;
            OrderStatusUpdateRequest orderStatusUpdate = new OrderStatusUpdateRequest("DONE");
            restTemplate.put(updateOrderUrl, orderStatusUpdate);
        } catch (Exception e) {
            throw new OrderServiceException(e.getMessage());
        }
    }

    public Long fetchDeliveryPartners(Long restaurantId) {
        log.info("Fetching nearest delivery partners for restaurant: {}", restaurantId);
        String url = "http://localhost:8080/delivery-partners?fetch-type=NEAREST&availability=true";

        try {
            // Create the request body
            String requestBody = "{ \"restaurantId\": " + restaurantId + " }";

            // Make the API call and fetch the response
            ResponseEntity<List<DeliveryPartnerResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, createHeaders()),
                    new ParameterizedTypeReference<List<DeliveryPartnerResponse>>() {
                    }
            );

            if (response.getBody() == null || response.getBody().isEmpty()) {
                throw new CatalogServiceException("Failed to fetch delivery partners");
            }

            // Return the list of DeliveryPartnerResponse
            return response.getBody().getFirst().getUserId();
        } catch (Exception e) {
            throw new CatalogServiceException(e.getMessage());
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}