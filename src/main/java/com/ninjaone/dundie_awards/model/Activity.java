package com.ninjaone.dundie_awards.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
@lombok.Data
@lombok.EqualsAndHashCode(of = {"id", "version", "event"})
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.Builder(builderClassName = "Builder")
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Version
    private Integer version;

    @Column(name = "occured_at")
    private LocalDateTime occuredAt;

    @Column(name = "event")
    private String event;
}
