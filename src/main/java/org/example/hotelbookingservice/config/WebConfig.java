package org.example.hotelbookingservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.UUID;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new Converter<String, UUID>() {
            @Override
            public UUID convert(String source) {
                if (source.matches("-?\\d+")) {
                    return new UUID(0L, Long.parseLong(source));
                }
                try {
                    return UUID.fromString(source);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        });
    }
}
