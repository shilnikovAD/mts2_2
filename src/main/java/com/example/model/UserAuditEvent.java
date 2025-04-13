package com.example.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.UUID;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAuditEvent {
  private UUID userId;
  private Instant eventTime;
  private String eventType;
  private String eventDetails;
}
