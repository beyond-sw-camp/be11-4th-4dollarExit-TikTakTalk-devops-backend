package com.TTT.TTT.User.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 10, nullable = false)
    private String name;
    @Column(length = 20, nullable = false)
    private String password;
    @Column(length = 20, nullable = false,unique = true)
    private String email;
    @Column(length = 11, nullable = false)
    private String phoneNumber;
    @Column(length = 10, nullable = false)
    private String nickname;
    @Column(nullable = false)
    private String delYN;
    @Column(nullable = false)
    private String adminYN;
    @Column(length = 5, nullable = false)
    private int batch;
    @Column(length = 20, nullable = false)
    private String blogLink;

    private String createdTime;
}
