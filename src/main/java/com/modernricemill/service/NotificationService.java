package com.modernricemill.service;

import com.modernricemill.entity.Order;
import com.modernricemill.entity.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.admin.sms-console:true}")
    private boolean smsConsole;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void notifyOrderPlaced(Order order) {
        String body = buildOrderEmailBody(order);
        if (mailEnabled) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setTo(adminEmail);
                msg.setSubject("New order #" + order.getId() + " — Sri Devi Modern Rice Mill");
                msg.setText(body);
                mailSender.send(msg);
            } catch (Exception e) {
                log.warn("Could not send order email to admin: {}", e.getMessage());
            }
        } else {
            log.info("[EMAIL disabled] Would notify {} with:\n{}", adminEmail, body);
        }

        if (smsConsole) {
            log.info(
                    "[SMS mock] To admin: New order #{} total ₹{} — customer {} — check dashboard.",
                    order.getId(),
                    order.getTotalPrice(),
                    order.getUser().getEmail()
            );
        }
    }

    private String buildOrderEmailBody(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("A new order was placed.\n\n");
        sb.append("Order ID: ").append(order.getId()).append("\n");
        sb.append("Customer: ").append(order.getUser().getName()).append(" (").append(order.getUser().getEmail()).append(")\n");
        sb.append("Status: ").append(order.getStatus()).append("\n");
        sb.append("Payment: ").append(order.getPaymentMethod()).append("\n");
        sb.append("Total: ₹").append(order.getTotalPrice()).append("\n\nItems:\n");
        for (OrderItem line : order.getItems()) {
            sb.append(" - ")
                    .append(line.getProduct().getName())
                    .append(" x ")
                    .append(line.getQuantity())
                    .append(" @ ₹")
                    .append(line.getUnitPrice())
                    .append("\n");
        }
        return sb.toString();
    }
}
