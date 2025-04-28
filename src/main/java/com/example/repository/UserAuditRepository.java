package com.example.repository;

import com.example.model.UserAuditEvent;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import java.util.List;
import java.util.UUID;

public interface UserAuditRepository extends CassandraRepository<UserAuditEvent, UUID> {

  @AllowFiltering
  List<UserAuditEvent> findByUserId(UUID userId);

  @Query("SELECT * FROM user_audit WHERE user_id = ?0 ORDER BY event_time DESC LIMIT ?1")
  List<UserAuditEvent> findLatestByUserId(UUID userId, int limit);
}
