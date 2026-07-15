package org.example.hotelbookingservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.frontend")
public record FrontendProperties(String paymentResultUrl) {
    public FrontendProperties {
        paymentResultUrl = paymentResultUrl == null || paymentResultUrl.isBlank()
                ? "http://localhost:5173/payment-result"
                : paymentResultUrl;
    }
}
