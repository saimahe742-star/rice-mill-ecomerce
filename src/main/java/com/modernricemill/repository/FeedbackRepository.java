package com.modernricemill.repository;

import com.modernricemill.entity.Feedback;
import com.modernricemill.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    void deleteAllByOrder_Id(Long orderId);

    long countByUser(User user);
}
