package com.footballstore.orders.presentationlayer;

import com.footballstore.orders.businesslayer.OrderService;
import com.footballstore.orders.utils.exceptions.InvalidInputException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final int UUID_LEN = 36;
    private final OrderService service;

    @GetMapping
    public List<OrderResponseModel> getAllCustomerOrders(@PathVariable String customerId) {
        if (customerId.length() != UUID_LEN) {
            throw new InvalidInputException("Invalid customerId: " + customerId);
        }
        return service.getAllCustomerOrders(customerId);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseModel> getCustomerOrderById(
            @PathVariable String customerId,
            @PathVariable String orderId
    ) {
        if (customerId.length() != UUID_LEN || orderId.length() != UUID_LEN) {
            throw new InvalidInputException("Invalid IDs");
        }
        return ResponseEntity.ok(service.getCustomerOrderById(customerId, orderId));
    }

    @PostMapping
    public ResponseEntity<OrderResponseModel> processCustomerOrder(
            @PathVariable String customerId,
            @RequestBody OrderRequestModel request
    ) {
        var created = service.processCustomerOrder(customerId, request);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponseModel> updateCustomerOrder(
            @PathVariable String customerId,
            @PathVariable String orderId,
            @RequestBody OrderRequestModel request
    ) {
        var updated = service.updateCustomerOrder(customerId, orderId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteCustomerOrder(
            @PathVariable String customerId,
            @PathVariable String orderId
    ) {
        if (customerId.length() != UUID_LEN) {
            throw new InvalidInputException("Invalid customerId: " + customerId);
        }
        if (orderId.length() != UUID_LEN) {
            throw new InvalidInputException("Invalid orderId: " + orderId);
        }
        service.deleteCustomerOrder(customerId, orderId);
        return ResponseEntity.noContent().build();
    }
}
