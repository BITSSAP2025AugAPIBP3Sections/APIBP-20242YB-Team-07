package com.cooknect.recipe_service.event;

import com.cooknect.common.events.RecipeEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipeEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "recipe-topic";

    public void sendRecipeEvent(RecipeEvent event) {
        kafkaTemplate.send(TOPIC, event);
        System.out.println("✅ Sent event to Kafka topic [" + TOPIC + "]: " + event);
    }
}
