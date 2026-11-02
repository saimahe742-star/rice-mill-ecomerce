package com.modernricemill.repository;

import com.modernricemill.entity.Order;
import com.modernricemill.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    long countByUser(User user);

    @EntityGraph(attributePaths = {"items", "items.product", "user"})
    @Query("select o from CustomerOrder o order by o.createdAt desc")
    List<Order> findAllOrdersNewestFirst();

    @EntityGraph(attributePaths = {"items", "items.product", "user"})
    @Query("select o from CustomerOrder o where o.user = ?1 order by o.createdAt desc")
    List<Order> findByUserNewestFirst(User user);

    @EntityGraph(attributePaths = {"items", "items.product", "user"})
    Optional<Order> findByIdAndUser(Long id, User user);

    @EntityGraph(attributePaths = {"items", "items.product", "user"})
    Optional<Order> findDetailedById(Long id);
}
