package com.kidsfinance.simulation.main;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MainSimulationEventType {

    STOCK_MARKET_DROP(
            "Fond bazarı -15%",
            "İnvestisiya hissəsinin dəyəri 15% azaldı."
    ),

    UNEXPECTED_EXPENSE(
            "Gözlənilməz xərc -200 AZN",
            "Gözlənilməz xərc portfelin dəyərindən çıxıldı."
    ),

    DEPOSIT_INTEREST(
            "Depozit faizi +6%",
            "Depozit hissəsinə 6% gəlir əlavə edildi."
    ),

    BUSINESS_PROFIT(
            "Biznes gəliri +20%",
            "Biznesə yönəldilən vəsait 20% gəlir gətirdi."
    );

    private final String title;
    private final String description;
}