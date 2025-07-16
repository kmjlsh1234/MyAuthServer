package com.example.my_auth_server.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "jwt_record")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class JwtRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long issueNo;

    @Column(name = "user_id")
    private long userId;

    @Column(name = "refresh_token_id")
    private long refreshTokenId;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime expireDatetime;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime logoutAt;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
