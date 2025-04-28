package com.example.audit.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.example.kafka.DtoMessage;
import com.example.kafka.KafkaProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.apache.kafka.common.serialization.StringDeserializer;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

@SpringBootTest(classes = KafkaProducerService.class)
@Import({KafkaAutoConfiguration.class, KafkaProducerServiceTest.ObjectMapperConfig.class})
@Testcontainers
class KafkaProducerServiceTest {

  @TestConfiguration
  static class ObjectMapperConfig {
    @Bean
    public ObjectMapper objectMapper() {
      return new ObjectMapper();
    }
  }

  static final KafkaContainer kafka =
      new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.1"))
          .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true");

  @DynamicPropertySource
  static void kafkaProps(DynamicPropertyRegistry registry) {
    if (!kafka.isRunning()) {
      kafka.start(); // Явно запустить контейнер
    }
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    registry.add("topic-to-send-message", () -> "test-topic");
  }

  @Autowired
  private KafkaProducerService producer;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void shouldSendMessageToKafkaSuccessfully() throws Exception {
    DtoMessage msg = new DtoMessage("123", "CREATE");
    assertDoesNotThrow(() -> producer.sendMessage(msg));

    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
      consumer.subscribe(List.of("test-topic"));
      ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
      assertEquals(1, records.count());

      for (ConsumerRecord<String, String> record : records) {
        DtoMessage received = objectMapper.readValue(record.value(), DtoMessage.class);
        assertEquals(msg, received);
      }
    }
  }
}
