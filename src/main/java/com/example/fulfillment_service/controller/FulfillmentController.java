package com.example.fulfillment_service.controller;

import com.example.fulfillment_service.dto.request.FulfillmentRequest;
import com.example.fulfillment_service.dto.response.FulfillmentResponse;
import com.example.fulfillment_service.service.FulfillmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fulfillments")
public class FulfillmentController {

    @Autowired
    private FulfillmentService fulfillmentService;

    @PostMapping()
    public FulfillmentResponse createFulfillment(@RequestBody FulfillmentRequest request) {
        return fulfillmentService.createFulfillment(request);
    }

    @GetMapping("/{fulfillmentId}")
    public FulfillmentResponse getFulfillment(@PathVariable Long fulfillmentId) {
        return fulfillmentService.getFulfillment(fulfillmentId);
    }
}


