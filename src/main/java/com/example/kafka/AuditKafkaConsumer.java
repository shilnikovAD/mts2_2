package com.example.kafka;

import com.example.model.UserAuditEvent;
import com.example.service.UserAuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditKafkaConsumer {

  private final UserAuditService auditService;
  private final ObjectMapper objectMapper;

  @KafkaListener(
      topics = "${topic-to-consume-message}",
      groupId = "${spring.kafka.consumer.group-id}"
  )
  public void consume(String message) {
    try {
      log.info("Получено сообщение из Kafka: {}", message);
      DtoMessage dto = objectMapper.readValue(message, DtoMessage.class);

      UserAuditEvent event = UserAuditEvent.builder()
          .userId(UUID.fromString(dto.getUserId()))
          .eventTime(Instant.now())
          .eventType(dto.getAction())
          .eventDetails(dto.getDetails())
          .build();

      auditService.saveAuditEvent(event);
      log.info("Событие успешно сохранено в Cassandra: {}", event);
    } catch (Exception e) {
      log.error("Ошибка при обработке Kafka-сообщения", e);
    }
  }
}
