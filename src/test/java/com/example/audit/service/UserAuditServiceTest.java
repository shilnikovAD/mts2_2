package com.example.audit.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.service.UserAuditService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.example.Application;
import com.example.model.UserAuditEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(classes = Application.class)
class UserAuditServiceTest {

  @Container
  static final CassandraContainer<?> cassandra = new CassandraContainer<>("cassandra:4.1");

  @DynamicPropertySource
  static void cassandraProps(DynamicPropertyRegistry reg) {
    reg.add("spring.cassandra.contact-points", cassandra::getHost);
    reg.add("spring.cassandra.port", () -> cassandra.getMappedPort(9042));
    reg.add("spring.cassandra.local-datacenter", () -> "datacenter1");
  }

  @Autowired
  private UserAuditService auditService;

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

    List<UserAuditEvent> result = auditService.getAuditEventsForUser(userId);
    assertFalse(result.isEmpty());
    assertEquals("DELETE", result.get(0).getEventType());
  }

  @Test
  void testRetrieveEmptyAuditList() {
    UUID nonexistentUser = UUID.randomUUID();
    List<UserAuditEvent> empty = auditService.getAuditEventsForUser(nonexistentUser);
    assertTrue(empty.isEmpty());
  }
}
