package com.example.fulfillment_service.controller;

import com.example.fulfillment_service.dto.request.FulfillmentRequest;
import com.example.fulfillment_service.dto.response.FulfillmentResponse;
import com.example.fulfillment_service.service.FulfillmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FulfillmentController.class)
class FulfillmentControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FulfillmentService fulfillmentService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateFulfillmentSuccess() throws Exception {
        FulfillmentRequest request = new FulfillmentRequest();
        request.setOrderId(1L);

        FulfillmentResponse response = new FulfillmentResponse();
        response.setFulfillmentId(1L);
        response.setOrderId(1L);
        response.setDeliveryPartnerId(1L);

        when(fulfillmentService.createFulfillment(any(FulfillmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/fulfillments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }


    @Test
    void testCreateFulfillmentException() throws Exception {
        FulfillmentRequest request = new FulfillmentRequest();
        request.setOrderId(1L);

        when(fulfillmentService.createFulfillment(any(FulfillmentRequest.class))).thenThrow(new RuntimeException("Service unavailable"));

        mockMvc.perform(post("/fulfillments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetFulfillmentSuccess() throws Exception {
        FulfillmentResponse response = new FulfillmentResponse();
        response.setFulfillmentId(1L);
        response.setOrderId(1L);
        response.setDeliveryPartnerId(1L);

        when(fulfillmentService.getFulfillment(1L)).thenReturn(response);

        mockMvc.perform(get("/fulfillments/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void testGetFulfillmentException() throws Exception {
        when(fulfillmentService.getFulfillment(1L)).thenThrow(new RuntimeException("Service unavailable"));

        mockMvc.perform(get("/fulfillments/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}