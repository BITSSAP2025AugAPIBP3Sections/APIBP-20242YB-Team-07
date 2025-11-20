package com.cooknect.user_service.event;

import com.cooknect.common.events.UserEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "user-topic";

    public void sendUserEvent(UserEvent event) {
        kafkaTemplate.send(TOPIC, event);
        System.out.println("✅ Sent event to Kafka topic [" + TOPIC + "]: " + event);
    }
}
 