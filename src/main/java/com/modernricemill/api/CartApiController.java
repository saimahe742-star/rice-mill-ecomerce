package com.modernricemill.api;

import com.modernricemill.entity.Product;
import com.modernricemill.service.CartService;
import com.modernricemill.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    private final CartService cartService;
    private final ProductService productService;

    public CartApiController(CartService cartService, ProductService productService) {
        this.cartService = cartService;
        this.productService = productService;
    }

    @GetMapping
    public CartResponse get(HttpSession session) {
        return build(session);
    }

    @PostMapping("/add")
    public CartResponse add(@RequestBody Map<String, Object> body, HttpSession session) {
        Object rawId = body.get("productId");
        if (!(rawId instanceof Number n)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing or invalid productId");
        }
        long productId = n.longValue();
        int delta = body.containsKey("quantity") ? ((Number) body.get("quantity")).intValue() : 1;
        Product p = productService.getById(productId);
        Map<Long, Integer> cart = cartService.getCart(session);
        int current = cart.getOrDefault(productId, 0);
        int next = current + delta;
        if (next <= 0) {
            cartService.setQuantity(session, productId, 0);
            return build(session);
        }
        if (next > p.getStockQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only " + p.getStockQuantity() + " available in stock for this product");
        }
        cartService.add(session, productId, delta);
        return build(session);
    }

    @PutMapping("/item/{productId}")
    public CartResponse setQty(@PathVariable Long productId, @RequestBody Map<String, Integer> body,
                             HttpSession session) {
        Integer qty = body.get("quantity");
        if (qty == null) {
            return build(session);
        }
        Product p = productService.getById(productId);
        if (qty > p.getStockQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only " + p.getStockQuantity() + " available in stock for this product");
        }
        cartService.setQuantity(session, productId, qty);
        return build(session);
    }

    @DeleteMapping("/item/{productId}")
    public CartResponse remove(@PathVariable Long productId, HttpSession session) {
        cartService.setQuantity(session, productId, 0);
        return build(session);
    }

    @DeleteMapping
    public CartResponse clear(HttpSession session) {
        cartService.clear(session);
        return build(session);
    }

    private CartResponse build(HttpSession session) {
        Map<Long, Integer> cart = cartService.getCart(session);
        List<CartLine> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> e : cart.entrySet()) {
            try {
                Product p = productService.getById(e.getKey());
                BigDecimal line = p.getPrice().multiply(BigDecimal.valueOf(e.getValue()));
                total = total.add(line);
                lines.add(new CartLine(p.getId(), p.getName(), p.getPrice(), e.getValue(), p.getImageUrl(), line));
            } catch (Exception ignored) {
                // skip missing product ids
            }
        }
        return new CartResponse(lines, total);
    }

    public record CartLine(Long productId, String name, BigDecimal price, int quantity, String imageUrl,
                           BigDecimal lineTotal) {
    }

    public record CartResponse(List<CartLine> items, BigDecimal total) {
    }
}
