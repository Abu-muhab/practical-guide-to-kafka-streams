package dev.abumuhab.frauddetection.common;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "outbox_events")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class OutboxEvent {
    @Id
    private String id;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private String topic;

    private String key;

    private boolean published;
}
