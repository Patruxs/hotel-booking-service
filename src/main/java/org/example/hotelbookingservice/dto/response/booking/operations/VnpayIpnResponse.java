package org.example.hotelbookingservice.dto.response.booking.operations;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VnpayIpnResponse(
        @JsonProperty("RspCode")
        String RspCode,
        @JsonProperty("Message")
        String Message
) {
}
