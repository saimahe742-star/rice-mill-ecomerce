package com.modernricemill.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CartService {

    public static final String SESSION_KEY = "CART_ITEMS";

    @SuppressWarnings("unchecked")
    public Map<Long, Integer> getCart(HttpSession session) {
        Object raw = session.getAttribute(SESSION_KEY);
        if (raw instanceof Map<?, ?> m) {
            Map<Long, Integer> cart = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (e.getKey() instanceof Long l && e.getValue() instanceof Integer i) {
                    cart.put(l, i);
                } else if (e.getKey() instanceof Number n && e.getValue() instanceof Number q) {
                    cart.put(n.longValue(), q.intValue());
                }
            }
            session.setAttribute(SESSION_KEY, cart);
            return cart;
        }
        Map<Long, Integer> cart = new LinkedHashMap<>();
        session.setAttribute(SESSION_KEY, cart);
        return cart;
    }

    public void add(HttpSession session, Long productId, int delta) {
        Map<Long, Integer> cart = getCart(session);
        int next = cart.getOrDefault(productId, 0) + delta;
        if (next <= 0) {
            cart.remove(productId);
        } else {
            cart.put(productId, next);
        }
        session.setAttribute(SESSION_KEY, cart);
    }

    public void setQuantity(HttpSession session, Long productId, int quantity) {
        Map<Long, Integer> cart = getCart(session);
        if (quantity <= 0) {
            cart.remove(productId);
        } else {
            cart.put(productId, quantity);
        }
        session.setAttribute(SESSION_KEY, cart);
    }

    public void clear(HttpSession session) {
        session.setAttribute(SESSION_KEY, new LinkedHashMap<Long, Integer>());
    }

    public Map<Long, Integer> readOnly(HttpSession session) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(getCart(session)));
    }
}
