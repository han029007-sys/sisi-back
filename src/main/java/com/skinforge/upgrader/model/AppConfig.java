package com.skinforge.upgrader.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppConfig {

    @Id
    private Long id;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal usdRubRate;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal skinMarkup;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
