package com.example.service;

import com.example.model.UserAuditEvent;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAuditService {

  private final CqlSession session;

  public void saveAuditEvent(UserAuditEvent event) {
    PreparedStatement ps = session.prepare("""
            INSERT INTO my_keyspace.user_audit 
            (user_id, event_time, event_type, event_details)
            VALUES (?, ?, ?, ?)
        """);

    BoundStatement bs = ps.bind(
        event.getUserId(),
        event.getEventTime(),
        event.getEventType(),
        event.getEventDetails()
    );

    session.execute(bs);
  }

  public List<UserAuditEvent> getAuditEventsForUser(UUID userId) {
    PreparedStatement ps = session.prepare("""
            SELECT * FROM my_keyspace.user_audit 
            WHERE user_id = ?
        """);

    ResultSet rs = session.execute(ps.bind(userId));

    List<UserAuditEvent> result = new ArrayList<>();
    for (Row row : rs) {
      result.add(UserAuditEvent.builder()
          .userId(row.getUuid("user_id"))
          .eventTime(row.getInstant("event_time"))
          .eventType(row.getString("event_type"))
          .eventDetails(row.getString("event_details"))
          .build());
    }

    return result;
  }
}
