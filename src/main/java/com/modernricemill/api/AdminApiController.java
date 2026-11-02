package com.modernricemill.api;

import com.modernricemill.dto.AdminOrderUpdateRequest;
import com.modernricemill.dto.AdminUserUpdateRequest;
import com.modernricemill.dto.ProductRequest;
import com.modernricemill.entity.Order;
import com.modernricemill.entity.OrderItem;
import com.modernricemill.entity.Product;
import com.modernricemill.entity.User;
import com.modernricemill.service.OrderService;
import com.modernricemill.service.ProductService;
import com.modernricemill.service.UserService;
import com.modernricemill.service.UserService.MapProfile;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;

    public AdminApiController(OrderService orderService, ProductService productService, UserService userService) {
        this.orderService = orderService;
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping("/orders")
    public List<Map<String, Object>> orders() {
        return orderService.allOrdersAdmin().stream().map(this::adminOrder).collect(Collectors.toList());
    }

    @PatchMapping("/orders/{id}")
    public Map<String, Object> updateOrder(@PathVariable Long id, @Valid @RequestBody AdminOrderUpdateRequest body) {
        Order o = orderService.adminUpdate(id, body);
        return adminOrder(o);
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.adminDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(req));
    }

    @PutMapping("/products/{id}")
    public Product updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest req) {
        return productService.update(id, req);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    public List<Map<String, Object>> users() {
        return userService.listAllUsers().stream().map(this::userSummary).collect(Collectors.toList());
    }

    @GetMapping("/users/{id}")
    public Map<String, Object> user(@PathVariable Long id) {
        User u = userService.requireUser(id);
        return userSummary(u);
    }

    @PatchMapping("/users/{id}")
    public Map<String, Object> updateUser(@PathVariable Long id, @Valid @RequestBody AdminUserUpdateRequest body) {
        MapProfile p = userService.adminUpdateUser(id, body);
        Map<String, Object> res = new java.util.LinkedHashMap<>();
        res.put("id", p.id());
        res.put("name", p.name());
        res.put("email", p.email());
        res.put("phone", p.phone() != null ? p.phone() : "");
        res.put("role", p.role());
        return res;
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.adminDeleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> userSummary(User u) {
        Map<String, Object> res = new java.util.LinkedHashMap<>();
        res.put("id", u.getId());
        res.put("name", u.getName());
        res.put("email", u.getEmail());
        res.put("phone", u.getPhone() != null ? u.getPhone() : "");
        res.put("role", u.getRole() != null ? u.getRole().name() : "");
        return res;
    }

    private Map<String, Object> adminOrder(Order o) {
        List<Map<String, Object>> items = o.getItems().stream().map(this::line).collect(Collectors.toList());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("customerEmail", o.getUser().getEmail());
        m.put("customerName", o.getUser().getName());
        m.put("customerPhone", o.getUser().getPhone() != null ? o.getUser().getPhone() : "");
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
        m.put("productId", i.getProduct().getId());
        m.put("productName", i.getProduct().getName());
        m.put("quantity", i.getQuantity());
        m.put("unitPrice", i.getUnitPrice());
        return m;
    }
}
