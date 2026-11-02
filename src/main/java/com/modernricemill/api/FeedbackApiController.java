package com.modernricemill.api;

import com.modernricemill.dto.FeedbackRequest;
import com.modernricemill.entity.Feedback;
import com.modernricemill.entity.User;
import com.modernricemill.security.CustomUserDetails;
import com.modernricemill.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackApiController {

    private final FeedbackService feedbackService;

    public FeedbackApiController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> submit(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody FeedbackRequest request) {
        User user = principal.getUser();
        Feedback fb = feedbackService.submit(user, request);
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("id", fb.getId());
        body.put("message", "Thank you — your return/feedback request was recorded.");
        return ResponseEntity.ok(body);
    }
}
