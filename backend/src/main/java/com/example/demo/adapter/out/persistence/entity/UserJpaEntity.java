package com.example.demo.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA Entity for User/Customer persistence
 * Uses standard JPA annotations - database agnostic
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "role", nullable = false, length = 20)
    private String role;
}
