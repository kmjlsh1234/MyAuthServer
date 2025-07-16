package com.example.my_auth_server.config.redis.repository;

import com.example.my_auth_server.config.redis.LoginAttempt;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginAttemptRepository extends CrudRepository<LoginAttempt,String> {
}
