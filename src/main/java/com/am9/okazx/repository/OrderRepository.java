package com.am9.okazx.repository;

import com.am9.okazx.model.entity.Order;
import com.am9.okazx.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUser(User user);
    @Modifying
    @Transactional
    @Query("UPDATE orders o SET o.user = null WHERE o.user.id = :userId")
    void detachFromUser(@Param("userId") Long id);
}
