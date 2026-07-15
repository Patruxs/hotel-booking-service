package org.example.hotelbookingservice.security.vnpay;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VnpaySignerTest {

    @Test
    void canonicalizesByEncodedKeyAndExcludesSecureHashFields() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_OrderInfo", "Payment for booking 123");
        params.put("vnp_TxnRef", "BK_123_456");
        params.put("vnp_Amount", "18000");
        params.put("vnp_SecureHashType", "HMACSHA512");
        params.put("vnp_SecureHash", "ignored");
        params.put("vnp_Optional", null);

        String canonical = VnpaySigner.canonicalize(params);

        assertThat(canonical).isEqualTo("vnp_Amount=18000&vnp_OrderInfo=Payment+for+booking+123&vnp_TxnRef=BK_123_456");
    }

    @Test
    void verifiesHashCaseInsensitively() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Amount", "18000");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TxnRef", "BK_123_456");
        params.put("vnp_SecureHash", VnpaySigner.hmacSha512("secret", VnpaySigner.canonicalize(params)).toUpperCase());

        assertThat(VnpaySigner.verify(params, "secret")).isTrue();
    }
}
