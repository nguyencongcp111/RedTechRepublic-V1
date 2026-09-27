package com.example.auth_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "system_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(length = 36)
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @com.fasterxml.jackson.annotation.JsonIgnore
    private String password;

    private String role;
}
