package com.ninjaone.dundie_awards.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents an activity that has occurred in the system (e.g. an audit log)
 */
@Entity
@Table(name = "activities")
@lombok.Data
@lombok.EqualsAndHashCode(of = { "id", "version", "event", "occurredInThread", "createdInThread" })
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.Builder(builderClassName = "Builder")
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Version
    private Integer version;

    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;

    @Column(name = "event")
    private String event;

    @Column(name = "occurred_in_thread")
    private String occurredInThread;

    @Column(name = "created_in_thread")
    private String createdInThread;

    /**
     * JPA lifecycle callback method that is called before the entity is persisted to:
     * <ul>
     * <li>Set the {@link #occuredAt} field to the current date and time if it is null</li>
     * <li>Set the {@link #createdInThread} field to the current thread name if it is null</li>
     * </ul>
     */
    @PrePersist
    void prePersist() {
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
        if (createdInThread == null) {
            createdInThread = Thread.currentThread().getName();
        }
    }
}
