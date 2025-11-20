package com.cooknect.nutrition_service.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder; // Import Encoder
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SolarWindsHttpAppender extends AppenderBase<ILoggingEvent> {

    private String url;
    private String token;
    private HttpClient httpClient;
    private Encoder<ILoggingEvent> encoder; // Add encoder field

    // Setters
    public void setUrl(String url) { this.url = url; }
    public void setToken(String token) { this.token = token; }
    public void setEncoder(Encoder<ILoggingEvent> encoder) { this.encoder = encoder; } // Setter for encoder

    @Override
    public void start() {
        // ... (url and token checks remain the same)
        if (this.encoder == null) {
            addError("Encoder is not set for SolarWindsHttpAppender.");
            return;
        }
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        super.start();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!isStarted()) return;

        try {
            // Use the encoder to get the JSON bytes
            byte[] payloadBytes = this.encoder.encode(eventObject);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(this.url))
                    // Set Content-Type back to application/json
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + this.token)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(payloadBytes))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                addError("SolarWinds returned non-successful status code: " + response.statusCode() + " Body: " + response.body());
            }
        } catch (Exception e) {
            addError("Error sending log to SolarWinds: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void stop() {
        addInfo("SolarWindsHttpAppender stopped.");
        super.stop();
    }
}