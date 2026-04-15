package com.am9.okazx.model.entity;

import ch.qos.logback.core.model.INamedModel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity()
@Data
@NoArgsConstructor
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    private Integer quantity;
    private BigDecimal price;
    @CreationTimestamp
    private LocalDateTime creationTime;
    @UpdateTimestamp
    private LocalDateTime updateTime;


}
