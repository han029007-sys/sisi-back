package com.skinforge.upgrader.bll.service.balance;

import com.skinforge.upgrader.bll.service.config.AppConfigService;
import com.skinforge.upgrader.model.AppConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CurrencyPriceService {

    private final AppConfigService appConfigService;

    public BigDecimal skinUsdToRub(BigDecimal priceUsd) {
        var config = appConfigService.get();

        return priceUsd
                .multiply(config.getUsdRubRate())
                .multiply(config.getSkinMarkup())
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal skinRubToUsd(BigDecimal priceRub) {
        var config = appConfigService.get();

        return priceRub.divide(
                config.getUsdRubRate()
                        .multiply(config.getSkinMarkup()),
                4,
                RoundingMode.HALF_UP
        );
    }
}