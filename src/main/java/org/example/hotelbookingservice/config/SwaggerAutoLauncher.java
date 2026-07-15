package org.example.hotelbookingservice.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@Slf4j
public class SwaggerAutoLauncher implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${server.port:8080}")
    private int port;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String url = String.format("http://localhost:%d%s/swagger-ui.html", port, contextPath);
        log.info("Application started. Opening Swagger UI: {}", url);

        browse(url);
    }

    private void browse(String url) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                log.error("Failed to open browser via AWT", e);
                openBrowserFallback(url);
            }
        } else {
            openBrowserFallback(url);
        }
    }

    private void openBrowserFallback(String url) {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", url).start();
            } else if (os.contains("nix") || os.contains("nux")) {
                String[] browsers = {"google-chrome", "firefox", "mozilla", "epiphany", "konqueror",
                        "netscape", "opera", "links", "lynx"};

                StringBuilder cmd = new StringBuilder();
                for (int i = 0; i < browsers.length; i++) {
                    if (i == 0)
                        cmd.append(String.format("%s \"%s\"", browsers[i], url));
                    else
                        cmd.append(String.format(" || %s \"%s\"", browsers[i], url));
                }
                new ProcessBuilder("sh", "-c", "xdg-open \"" + url + "\" || " + cmd).start();
            }
        } catch (IOException e) {
            log.error("Failed to open browser via fallback launcher", e);
        }
    }
}
