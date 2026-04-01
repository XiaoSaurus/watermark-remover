package com.watermark.repository;

import com.watermark.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByUsername(String username);
    Optional<User> findByWxOpenId(String wxOpenId);
    Optional<User> findByWxUnionId(String wxUnionId);
    Optional<User> findByWxWebOpenId(String wxWebOpenId);
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);
}
