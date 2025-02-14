package com.ninjaone.dundie_awards.model;

import jakarta.persistence.*;

@Entity
@Table(name = "organizations")
@lombok.Data
@lombok.EqualsAndHashCode(of = {"id", "version", "name"})
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.Builder(builderClassName = "Builder")
public class Organization {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  @Column(name = "version")
  private Long version;

  @Column(name = "name")
  private String name;
}
