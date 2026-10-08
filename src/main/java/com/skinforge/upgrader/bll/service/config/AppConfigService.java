package com.skinforge.upgrader.bll.service.config;

import com.skinforge.upgrader.model.AppConfig;
import com.skinforge.upgrader.repository.AppConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AppConfigService {

    private static final long CONFIG_ID = 1L;

    private final AppConfigRepository repository;

    public AppConfig get() {
        return repository.findById(CONFIG_ID)
                .orElseThrow(() ->
                        new IllegalStateException("App config not found"));
    }

    @Transactional
    public AppConfig update(
            BigDecimal usdRubRate,
            BigDecimal skinMarkup
    ) {
        AppConfig config = get();

        config.setUsdRubRate(usdRubRate);
        config.setSkinMarkup(skinMarkup);
        config.setUpdatedAt(LocalDateTime.now());

        return config;
    }
}
