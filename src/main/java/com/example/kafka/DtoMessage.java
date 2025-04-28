package com.example.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoMessage {
  private String id;
  private String action;
  private String userId;
  private String eventType;
  private String details;

  public DtoMessage(String id, String action) {
    this.id = id;
    this.action = action;
  }
}
