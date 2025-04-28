package com.example.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Value("${topic-to-send-message}")
  private String topic;

  /**
   * Отправляет DtoMessage как JSON в Kafka.
   */
  public void sendMessage(DtoMessage message) {
    try {
      String payload = objectMapper.writeValueAsString(message);
      kafkaTemplate.send(topic, payload);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize message", e);
    }
  }
}
