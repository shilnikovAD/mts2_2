package com.example.audit.service;

import com.example.Application;
import com.example.model.UserAuditEvent;
import com.example.service.UserAuditService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest(classes = Application.class)
public class UserAuditServiceTest {

  @Container
  static CassandraContainer<?> cassandraContainer = new CassandraContainer<>("cassandra:4.1");

  @Autowired
  UserAuditService auditService;

  @BeforeAll
  static void setup() {
    System.setProperty("spring.cassandra.port",
        String.valueOf(cassandraContainer.getMappedPort(9042)));
  }

  @Test
  void testInsertAndRetrieveAudit() {
    UUID userId = UUID.randomUUID();
    UserAuditEvent event = new UserAuditEvent(
        userId,
        Instant.now(),
        "DELETE",
        "User deleted something suspicious"
    );

    auditService.saveAuditEvent(event);
    var result = auditService.getAuditEventsForUser(userId);

    assertFalse(result.isEmpty());
    assertEquals("DELETE", result.get(0).getEventType());
  }

  @Test
  void testRetrieveEmptyAuditList() {
    UUID nonexistentUserId = UUID.randomUUID();
    var result = auditService.getAuditEventsForUser(nonexistentUserId);
    assertTrue(result.isEmpty());
  }
}
