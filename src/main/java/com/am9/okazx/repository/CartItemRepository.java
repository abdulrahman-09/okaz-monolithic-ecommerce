package com.am9.okazx.repository;

import com.am9.okazx.model.entity.CartItem;
import com.am9.okazx.model.entity.Product;
import com.am9.okazx.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    void deleteByUserIdAndProductId(Long userId, Long productId);
    List<CartItem> findByUser(User user);
}

