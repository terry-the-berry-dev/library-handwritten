package com.lighthouse.library.io.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serializable;

@Entity(name = "AppUser")
@Table(name = "appUsers")
@NoArgsConstructor
@Getter
@Setter
public class AppUserEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NonNull
    private String username;

    @Column(nullable = false)
    @NonNull
    private String password;

    @Column(nullable = false)
    @NonNull
    private Boolean deleted = false;

    @Column(nullable = false)
    private String role = "USER";

    public AppUserEntity(@NonNull String username, @NonNull String password) {
        this.username = username;
        this.password = password;
    }

    public AppUserEntity(@NonNull String username, @NonNull String password, @NonNull String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
