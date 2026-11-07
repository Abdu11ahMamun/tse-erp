package com.tse.erp.security.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tse.erp.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Service
public class CaptchaService {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${captcha.enabled:false}")
    private boolean enabled;
    @Value("${captcha.secret:}")
    private String secret;
    @Value("${captcha.verify-url}")
    private String verifyUrl;

    public boolean isEnabled() {
        return enabled;
    }

    public void verify(String token, String ip) {
        if (!enabled) {
            return;
        }
        if (token == null || token.isBlank()) {
            throw new CaptchaRequiredException("Captcha is required");
        }
        try {
            String body = "secret=" + enc(secret)
                    + "&response=" + enc(token)
                    + (ip != null ? "&remoteip=" + enc(ip) : "");
            HttpRequest req = HttpRequest.newBuilder(URI.create(verifyUrl))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> res =
                    client.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode json = mapper.readTree(res.body());
            if (!json.path("success").asBoolean(false)) {
                throw new BadRequestException("Captcha verification failed");
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            // Provider down hole fail-closed
            log.warn("Captcha verify error: {}", e.getClass().getSimpleName());
            throw new BadRequestException("Captcha verification failed");
        }
    }

    private String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public static class CaptchaRequiredException extends RuntimeException {
        public CaptchaRequiredException(String m) {
            super(m);
        }
    }
}