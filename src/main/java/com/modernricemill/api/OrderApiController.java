package com.modernricemill.api;

import com.modernricemill.dto.CheckoutRequest;
import com.modernricemill.entity.Order;
import com.modernricemill.entity.OrderItem;
import com.modernricemill.entity.User;
import com.modernricemill.security.CustomUserDetails;
import com.modernricemill.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class OrderApiController {

    private final OrderService orderService;

    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, Object>> checkout(
            Authentication authentication,
            HttpSession session,
            @Valid @RequestBody CheckoutRequest body) {
        Order o = orderService.checkout(authentication, session, body);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("orderId", o.getId());
        res.put("status", o.getStatus().name());
        res.put("total", o.getTotalPrice());
        res.put("message", "Order placed successfully");
        if (o.getEstimatedDeliveryAt() != null) {
            res.put("estimatedDeliveryAt", o.getEstimatedDeliveryAt().toString());
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/orders/mine")
    public List<Map<String, Object>> mine(@AuthenticationPrincipal CustomUserDetails principal) {
        User user = principal.getUser();
        return orderService.myOrders(user).stream().map(this::toSummary).collect(Collectors.toList());
    }

    @GetMapping("/orders/{id}")
    public Map<String, Object> one(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        Order o = orderService.getOrderForUser(id, principal.getUser());
        return toDetail(o);
    }

    @PostMapping("/orders/{id}/cancel")
    public Map<String, Object> cancelMine(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal) {
        Order o = orderService.cancelMyOrder(id, principal.getUser());
        return toDetail(o);
    }

    private Map<String, Object> toSummary(Order o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("status", o.getStatus().name());
        m.put("totalPrice", o.getTotalPrice());
        m.put("paymentMethod", o.getPaymentMethod().name());
        m.put("itemReceived", o.isItemReceived());
        m.put("createdAt", o.getCreatedAt().toString());
        m.put("recipientName", o.getRecipientName());
        m.put("deliveryPhone", o.getDeliveryPhone());
        m.put("deliveryAddress", o.getDeliveryAddress());
        m.put("deliveryPincode", o.getDeliveryPincode());
        m.put("deliveryLandmark", o.getDeliveryLandmark());
        if (o.getEstimatedDeliveryAt() != null) {
            m.put("estimatedDeliveryAt", o.getEstimatedDeliveryAt().toString());
        }
        return m;
    }

    private Map<String, Object> toDetail(Order o) {
        List<Map<String, Object>> items = o.getItems().stream().map(this::line).collect(Collectors.toList());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("status", o.getStatus().name());
        m.put("totalPrice", o.getTotalPrice());
        m.put("paymentMethod", o.getPaymentMethod().name());
        m.put("itemReceived", o.isItemReceived());
        m.put("createdAt", o.getCreatedAt().toString());
        m.put("recipientName", o.getRecipientName());
        m.put("deliveryPhone", o.getDeliveryPhone());
        m.put("deliveryAddress", o.getDeliveryAddress());
        m.put("deliveryPincode", o.getDeliveryPincode());
        m.put("deliveryLandmark", o.getDeliveryLandmark());
        if (o.getEstimatedDeliveryAt() != null) {
            m.put("estimatedDeliveryAt", o.getEstimatedDeliveryAt().toString());
        }
        m.put("items", items);
        return m;
    }

    private Map<String, Object> line(OrderItem i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("productName", i.getProduct().getName());
        m.put("quantity", i.getQuantity());
        m.put("unitPrice", i.getUnitPrice());
        return m;
    }
}
