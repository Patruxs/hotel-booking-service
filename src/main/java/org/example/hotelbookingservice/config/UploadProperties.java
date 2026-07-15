package org.example.hotelbookingservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.upload")
public record UploadProperties(
        String mode,
        int maxImageCount
) {
    public UploadProperties {
        mode = mode == null || mode.isBlank() ? "LOCAL" : mode;
        maxImageCount = maxImageCount <= 0 ? 12 : maxImageCount;
    }
}
