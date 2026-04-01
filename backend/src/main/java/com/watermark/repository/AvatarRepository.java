package com.watermark.repository;

import com.watermark.model.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvatarRepository extends JpaRepository<Avatar, Long> {

    List<Avatar> findByCategory(String category);

    @Query(value = "SELECT * FROM avatar ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Avatar> findRandomAvatar();

    @Query(value = "SELECT * FROM avatar WHERE category = :category ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Avatar> findRandomAvatarByCategory(String category);
}
