package com.modernricemill.service;

import com.modernricemill.dto.FeedbackRequest;
import com.modernricemill.entity.Feedback;
import com.modernricemill.entity.Order;
import com.modernricemill.entity.User;
import com.modernricemill.repository.FeedbackRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final OrderService orderService;

    public FeedbackService(FeedbackRepository feedbackRepository, OrderService orderService) {
        this.feedbackRepository = feedbackRepository;
        this.orderService = orderService;
    }

    @Transactional
    public Feedback submit(User user, FeedbackRequest req) {
        Order order = orderService.getOrderForUser(req.getOrderId(), user);
        Feedback fb = new Feedback();
        fb.setUser(user);
        fb.setOrder(order);
        fb.setRating(req.getRating());
        fb.setComments(req.getComments());
        fb.setReturnReason(req.getReturnReason());
        return feedbackRepository.save(fb);
    }
}
