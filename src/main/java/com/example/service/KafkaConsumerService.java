package com.example.service;

import com.example.kafka.DtoMessage;
import com.example.model.UserAuditEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class KafkaConsumerService {
  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerService.class);

  private final ObjectMapper objectMapper;
  private final UserAuditService userAuditService;

  public KafkaConsumerService(ObjectMapper objectMapper, UserAuditService userAuditService) {
    this.objectMapper = objectMapper;
    this.userAuditService = userAuditService;
  }

  @KafkaListener(topics = "${topic-to-consume-message}")
  public void consumeMessage(String message) {
    try {
      DtoMessage parsedMessage = objectMapper.readValue(message, DtoMessage.class);
      LOGGER.info("Retrieved message: {}", parsedMessage);

      // Преобразование в UserAuditEvent и сохранение
      UserAuditEvent event = UserAuditEvent.builder()
          .userId(UUID.fromString(parsedMessage.getUserId()))
          .eventTime(Instant.now())
          .eventType(parsedMessage.getEventType())
          .eventDetails(parsedMessage.getDetails())
          .build();

      userAuditService.saveAuditEvent(event);
      LOGGER.info("Audit event saved successfully for user: {}", parsedMessage.getUserId());

    } catch (JsonProcessingException e) {
      LOGGER.error("Failed to parse message: {}", message, e);
    } catch (Exception e) {
      LOGGER.error("Error processing audit event", e);
    }
  }
}