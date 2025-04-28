package com.example.service;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.example.model.UserAuditEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;

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
            SELECT user_id, event_time, event_type, event_details
            FROM my_keyspace.user_audit
            WHERE user_id = ?
        """);

    BoundStatement bs = ps.bind(userId);
    ResultSet rs = session.execute(bs);

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
