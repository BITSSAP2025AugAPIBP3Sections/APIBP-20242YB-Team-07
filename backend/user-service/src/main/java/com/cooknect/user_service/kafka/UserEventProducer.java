package com.cooknect.user_service.kafka;

import com.cooknect.user_service.events.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "user-created-topic";

    public void sendUserCreatedEvent(UserCreatedEvent event) {
        kafkaTemplate.send(TOPIC, event);
        System.out.println("✅ Sent event to Kafka topic [" + TOPIC + "]: " + event);
    }
}
