package com.example.my_auth_server.user.repository;

import com.example.my_auth_server.user.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);
    Optional<Users> findByMobile(String mobile);
    Optional<Users> findByLoginId(String loginId);

    Optional<Users> findByUserId(long userId);
}
