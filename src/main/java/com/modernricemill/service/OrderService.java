package com.modernricemill.service;

import com.modernricemill.dto.AdminOrderUpdateRequest;
import com.modernricemill.dto.CheckoutRequest;
import com.modernricemill.entity.*;
import com.modernricemill.repository.FeedbackRepository;
import com.modernricemill.repository.OrderRepository;
import com.modernricemill.repository.ProductRepository;
import com.modernricemill.security.CustomUserDetails;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final FeedbackRepository feedbackRepository;
    private final CartService cartService;
    private final NotificationService notificationService;
    private final int deliveryEtaDays;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            FeedbackRepository feedbackRepository,
            CartService cartService,
            NotificationService notificationService,
            @Value("${app.delivery.eta-days:2}") int deliveryEtaDays) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.feedbackRepository = feedbackRepository;
        this.cartService = cartService;
        this.notificationService = notificationService;
        this.deliveryEtaDays = deliveryEtaDays;
    }

    @Transactional
    public Order checkout(Authentication auth, HttpSession session, CheckoutRequest request) {
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in with your phone to place an order");
        }
        if (principal.getUser().getRole() != Role.ROLE_USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only customers can place orders");
        }
        User user = principal.getUser();

        Map<Long, Integer> cart = cartService.getCart(session);
        if (cart.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setItemReceived(false);
        order.setRecipientName(request.getRecipientName().trim());
        order.setDeliveryPhone(request.getDeliveryPhone().trim());
        order.setDeliveryAddress(request.getDeliveryAddress().trim());
        order.setDeliveryPincode(request.getDeliveryPincode().trim());
        String landmark = request.getDeliveryLandmark();
        order.setDeliveryLandmark(landmark == null || landmark.trim().isEmpty() ? null : landmark.trim());

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> e : cart.entrySet()) {
            Product product = productRepository.findByIdForUpdate(e.getKey())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid product in cart"));
            int qty = e.getValue();
            if (product.getStockQuantity() < qty) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock for " + product.getName());
            }
            OrderItem line = new OrderItem();
            line.setProduct(product);
            line.setQuantity(qty);
            line.setUnitPrice(product.getPrice());
            order.addItem(line);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));
            product.setStockQuantity(product.getStockQuantity() - qty);
        }
        order.setTotalPrice(total);
        order.setEstimatedDeliveryAt(Instant.now().plus(Duration.ofDays(deliveryEtaDays)));

        Order saved = orderRepository.save(order);
        cartService.clear(session);

        Order detailed = orderRepository.findDetailedById(saved.getId()).orElse(saved);
        try {
            notificationService.notifyOrderPlaced(detailed);
        } catch (Exception e) {
            // Log warning but don't fail checkout if email sending fails
            org.slf4j.LoggerFactory.getLogger(OrderService.class).warn("Failed to send order email", e);
        }
        return detailed;
    }

    @Transactional(readOnly = true)
    public List<Order> myOrders(User user) {
        return orderRepository.findByUserNewestFirst(user);
    }

    @Transactional(readOnly = true)
    public Order getOrderForUser(Long id, User user) {
        return orderRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    @Transactional(readOnly = true)
    public List<Order> allOrdersAdmin() {
        return orderRepository.findAllOrdersNewestFirst();
    }

    @Transactional
    public Order adminUpdate(Long id, AdminOrderUpdateRequest req) {
        Order order = orderRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (req.getStatus() == OrderStatus.CANCELLED) {
            cancelOrderForAdmin(order);
        } else {
            if (order.getStatus() == OrderStatus.CANCELLED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cancelled orders cannot be changed");
            }
            order.setStatus(req.getStatus());
        }
        if (req.getItemReceived() != null) {
            order.setItemReceived(req.getItemReceived());
        }
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelMyOrder(Long id, User user) {
        Order order = orderRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return order;
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order can be cancelled only before shipping");
        }
        restoreStockForOrder(order);
        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    private void cancelOrderForAdmin(Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivered order cannot be cancelled");
        }
        restoreStockForOrder(order);
        order.setStatus(OrderStatus.CANCELLED);
    }

    private void restoreStockForOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            Product locked = productRepository.findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"));
            locked.setStockQuantity(locked.getStockQuantity() + item.getQuantity());
        }
    }

    @Transactional
    public void adminDelete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        feedbackRepository.deleteAllByOrder_Id(id);
        orderRepository.deleteById(id);
    }
}
