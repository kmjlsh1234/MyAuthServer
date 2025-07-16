package com.example.my_auth_server.user.repository;

import com.example.my_auth_server.user.model.JwtRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JwtRecordRepository extends JpaRepository<JwtRecord, Long> {
}
