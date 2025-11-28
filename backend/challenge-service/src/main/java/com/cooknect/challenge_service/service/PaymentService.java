package com.cooknect.challenge_service.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${razorpay_api_id}")
    private String razorpayKeyId;

    @Value("${razorpay_api_key}")
    private String razorpayKeySecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, String> createPaymentLink(Long challengeId, Long userId, String userEmail, String userName, Double amount) {
        try{
            String url = "https://api.razorpay.com/v1/payment_links";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("amount", (int)(amount * 100)); // Amount in paise
            requestBody.put("currency", "INR");
            requestBody.put("description", "Payment for Challenge ID: " + challengeId + ", User ID: " + userId);

            Map<String, String> customer = new HashMap<>();
            customer.put("name", userName);
            customer.put("email", userEmail);
            requestBody.put("customer", customer);

            String auth = razorpayKeyId + ":" + razorpayKeySecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedAuth);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if(response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("short_url") && responseBody.containsKey("id")) {
                    Map<String, String> result = new HashMap<>();
                    result.put("short_url", responseBody.get("short_url").toString());
                    result.put("id", responseBody.get("id").toString());
                    return result;
                }
            }

            throw new RuntimeException("Failed to create payment link");

        } catch (Exception e) {
            throw new RuntimeException("Error while creating payment link: " + e.getMessage(), e);
        }
    }

    public boolean verifyPayment(String paymentLinkId) {
        try {
            String url = "https://api.razorpay.com/v1/payment_links/" + paymentLinkId;

            String auth = razorpayKeyId + ":" + razorpayKeySecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedAuth);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Map.class
            );
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("status")) {
                    return "paid".equalsIgnoreCase(String.valueOf(responseBody.get("status")));
                }
            }
            return false;
        } catch (RestClientException e) {
            throw new RuntimeException(e);
        }
    }
}
