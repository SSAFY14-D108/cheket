package com.ssafy.cheket.entity.host;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "hosts")
public class Host {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false, unique = false, length = 50)
    private String companyName;

    @Column(name = "business_no", nullable = false, unique = true, length = 50)
    private String businessNo;

    @Column(nullable = false, unique = false, length = 50)
    private String email;

    @Column(nullable = false, unique = false, length = 100)
    private String password;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, updatable = true)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;

    // TODO: Wallet 넣기

}
