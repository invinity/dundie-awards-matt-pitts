package com.ninjaone.dundie_awards.model;

import jakarta.persistence.*;

@Entity
@Table(name = "employees")
@lombok.Data
@lombok.EqualsAndHashCode(of = {"id", "version", "firstName", "lastName"})
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.Builder(builderClassName = "Builder")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "dundie_awards")
    private int dundieAwards;

    @ManyToOne
    private Organization organization;
}