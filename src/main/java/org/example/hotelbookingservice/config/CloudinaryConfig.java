package org.example.hotelbookingservice.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig implements InitializingBean {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Value("${app.upload.mode:LOCAL}")
    private String uploadMode;

    @Override
    public void afterPropertiesSet() {
        boolean cloudinaryMode = "CLOUDINARY".equalsIgnoreCase(uploadMode);
        if (cloudinaryMode && (isBlank(cloudName) || isBlank(apiKey) || isBlank(apiSecret))) {
            throw new IllegalStateException("Cloudinary credentials are required when app.upload.mode=CLOUDINARY");
        }
    }

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        return new Cloudinary(config);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
