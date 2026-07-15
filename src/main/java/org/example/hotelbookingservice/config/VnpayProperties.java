package org.example.hotelbookingservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.vnpay")
public record VnpayProperties(
        String payUrl,
        String tmnCode,
        String hashSecret,
        String returnUrl,
        boolean enabled
) {
    public VnpayProperties {
        payUrl = defaultValue(payUrl, "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        tmnCode = defaultValue(tmnCode, "DEMO");
        hashSecret = defaultValue(hashSecret, "DEMO_SECRET");
        returnUrl = defaultValue(returnUrl, "http://localhost:8080/api/v1/payments/vnpay/return");
    }

    private static String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
