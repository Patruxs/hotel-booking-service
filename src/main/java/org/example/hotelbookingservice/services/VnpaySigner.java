package org.example.hotelbookingservice.services;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public final class VnpaySigner {
    private VnpaySigner() {
    }

    public static String buildPaymentUrl(String payUrl, Map<String, String> params, String secret) {
        String hashData = canonicalize(params);
        String secureHash = hmacSha512(secret, hashData);
        return payUrl + "?" + hashData + "&vnp_SecureHash=" + secureHash;
    }

    public static boolean verify(Map<String, String> params, String secret) {
        String expected = hmacSha512(secret, canonicalize(params));
        String actual = params.get("vnp_SecureHash");
        return actual != null && expected.equalsIgnoreCase(actual);
    }

    public static String canonicalize(Map<String, String> params) {
        return params.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> !entry.getKey().equals("vnp_SecureHash"))
                .filter(entry -> !entry.getKey().equals("vnp_SecureHashType"))
                .sorted(Comparator.comparing(entry -> encode(entry.getKey())))
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    public static String hmacSha512(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                hash.append(String.format("%02x", value));
            }
            return hash.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign VNPAY payload", exception);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
