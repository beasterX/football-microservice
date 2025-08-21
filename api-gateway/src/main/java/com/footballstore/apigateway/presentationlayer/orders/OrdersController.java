package com.footballstore.apigateway.presentationlayer.orders;

import com.footballstore.apigateway.businesslayer.orders.OrdersService;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/orders")
public class OrdersController {

    private final OrdersService service;

    public OrdersController(OrdersService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<List<OrderResponseModel>> getAllCustomerOrders(
            @PathVariable String customerId) {
        return ResponseEntity.ok(service.getAllCustomerOrders(customerId));
    }

    @GetMapping(value = "/{orderId}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<OrderResponseModel> getCustomerOrderById(
            @PathVariable String customerId,
            @PathVariable String orderId) {
        return ResponseEntity.ok(service.getCustomerOrderById(customerId, orderId));
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaTypes.HAL_JSON_VALUE
    )
    public ResponseEntity<OrderResponseModel> processCustomerOrder(
            @PathVariable String customerId,
            @RequestBody OrderRequestModel request) {

        OrderResponseModel created = service.processCustomerOrder(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping(
            value = "/{orderId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaTypes.HAL_JSON_VALUE
    )
    public ResponseEntity<OrderResponseModel> updateCustomerOrder(
            @PathVariable String customerId,
            @PathVariable String orderId,
            @RequestBody OrderRequestModel request) {

        return ResponseEntity.ok(
                service.updateCustomerOrder(customerId, orderId, request));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteCustomerOrder(
            @PathVariable String customerId,
            @PathVariable String orderId) {

        service.deleteCustomerOrder(customerId, orderId);
        return ResponseEntity.noContent().build();
    }
}
