package com.cooknect.challenge_service.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.cooknect.common.events.ChallengeEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChallengeEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "challenge-topic";

    public void sendChallengeEvent(ChallengeEvent event) {
        kafkaTemplate.send(TOPIC, event);
        System.out.println("✅ Sent event to Kafka topic [" + TOPIC + "]: " + event);
    }
}
