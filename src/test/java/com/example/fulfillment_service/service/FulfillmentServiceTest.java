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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FulfillmentServiceTest {

    @Mock
    private FulfillmentRepository fulfillmentRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FulfillmentService fulfillmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateFulfillment() {
        FulfillmentRequest request = new FulfillmentRequest();
        request.setOrderId(1L);

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setRestaurantID(1L);
        orderResponse.setStatus("PENDING");

        DeliveryPartnerResponse deliveryPartnerResponse = new DeliveryPartnerResponse();
        deliveryPartnerResponse.setUserId(1L);

        when(restTemplate.getForEntity(anyString(), eq(OrderResponse.class)))
                .thenReturn(new ResponseEntity<>(orderResponse, HttpStatus.OK));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<DeliveryPartnerResponse>>() {
        })))
                .thenReturn(new ResponseEntity<>(List.of(deliveryPartnerResponse), HttpStatus.OK));
        when(fulfillmentRepository.save(any(Fulfillment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FulfillmentResponse response = fulfillmentService.createFulfillment(request);

        assertNotNull(response);
        assertEquals(1L, response.getOrderId());
        assertEquals(1L, response.getDeliveryPartnerId());
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(OrderResponse.class));
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<DeliveryPartnerResponse>>() {
        }));
        verify(fulfillmentRepository, times(1)).save(any(Fulfillment.class));
    }

    @Test
    void testGetFulfillment() {
        Fulfillment fulfillment = new Fulfillment();
        fulfillment.setFulfillmentId(1L);
        fulfillment.setOrderId(1L);
        fulfillment.setDeliveryPartnerId(1L);

        when(fulfillmentRepository.findById(1L)).thenReturn(Optional.of(fulfillment));

        FulfillmentResponse response = fulfillmentService.getFulfillment(1L);

        assertNotNull(response);
        assertEquals(1L, response.getFulfillmentId());
        assertEquals(1L, response.getOrderId());
        assertEquals(1L, response.getDeliveryPartnerId());
        verify(fulfillmentRepository, times(1)).findById(1L);
    }

    @Test
    void testGetFulfillmentNotFound() {
        when(fulfillmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(FulfillmentNotFoundException.class, () -> fulfillmentService.getFulfillment(1L));
        verify(fulfillmentRepository, times(1)).findById(1L);
    }

    @Test
    void testFetchOrderFromOrderServiceSuccess() {
        Long orderId = 1L;
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setRestaurantID(1L);
        orderResponse.setStatus("PENDING");

        when(restTemplate.getForEntity(anyString(), eq(OrderResponse.class)))
                .thenReturn(new ResponseEntity<>(orderResponse, HttpStatus.OK));

        OrderResponse response = fulfillmentService.fetchOrderFromOrderService(orderId);

        assertNotNull(response);
        assertEquals(1L, response.getRestaurantID());
        assertEquals("PENDING", response.getStatus());
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(OrderResponse.class));
    }

    @Test
    void testFetchOrderFromOrderServiceNonOkStatus() {
        Long orderId = 1L;

        when(restTemplate.getForEntity(anyString(), eq(OrderResponse.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.BAD_REQUEST));

        assertThrows(OrderServiceException.class, () -> fulfillmentService.fetchOrderFromOrderService(orderId));
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(OrderResponse.class));
    }

    @Test
    void testExceptionFetchOrderFromOrderServiceOrderAlreadyFulfilled() {
        Long orderId = 1L;
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setRestaurantID(1L);
        orderResponse.setStatus("DONE");

        when(restTemplate.getForEntity(anyString(), eq(OrderResponse.class)))
                .thenReturn(new ResponseEntity<>(orderResponse, HttpStatus.OK));

        assertThrows(OrderRequestException.class, () -> fulfillmentService.fetchOrderFromOrderService(orderId));
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(OrderResponse.class));
    }

    @Test
    void testExceptionFetchOrderFromOrderService() {
        Long orderId = 1L;

        when(restTemplate.getForEntity(anyString(), eq(OrderResponse.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        assertThrows(OrderServiceException.class, () -> fulfillmentService.fetchOrderFromOrderService(orderId));
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(OrderResponse.class));
    }

    @Test
    void testUpdateOrderStatusToDone() {
        Long orderId = 1L;
        doNothing().when(restTemplate).put(anyString(), any(OrderStatusUpdateRequest.class));

        fulfillmentService.updateOrderStatusToDone(orderId);

        verify(restTemplate, times(1)).put(eq("http://localhost:8081/orders/" + orderId), any(OrderStatusUpdateRequest.class));
    }

    @Test
    void testFetchDeliveryPartnersSuccess() {
        Long restaurantId = 1L;
        DeliveryPartnerResponse deliveryPartnerResponse = new DeliveryPartnerResponse();
        deliveryPartnerResponse.setUserId(1L);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<DeliveryPartnerResponse>>() {
        })))
                .thenReturn(new ResponseEntity<>(List.of(deliveryPartnerResponse), HttpStatus.OK));

        Long deliveryPartnerId = fulfillmentService.fetchDeliveryPartners(restaurantId);

        assertNotNull(deliveryPartnerId);
        assertEquals(1L, deliveryPartnerId);
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<DeliveryPartnerResponse>>() {
        }));
    }

    @Test
    void testFetchDeliveryPartnersEmptyResponse() {
        Long restaurantId = 1L;

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<DeliveryPartnerResponse>>() {
        })))
                .thenReturn(new ResponseEntity<>(List.of(), HttpStatus.OK));

        assertThrows(CatalogServiceException.class, () -> fulfillmentService.fetchDeliveryPartners(restaurantId));
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<DeliveryPartnerResponse>>() {
        }));
    }

    @Test
    void testFetchDeliveryPartnersException() {
        Long restaurantId = 1L;

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<DeliveryPartnerResponse>>() {
        })))
                .thenThrow(new RuntimeException("Service unavailable"));

        assertThrows(CatalogServiceException.class, () -> fulfillmentService.fetchDeliveryPartners(restaurantId));
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(new ParameterizedTypeReference<List<DeliveryPartnerResponse>>() {
        }));
    }
}