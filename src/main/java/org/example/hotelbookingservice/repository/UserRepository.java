package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Query(value = """
            SELECT DISTINCT u.*
            FROM "user" u
            LEFT JOIN user_role ur ON ur.user_id = u.id
            LEFT JOIN role r ON r.id = ur.role_id
            WHERE u.email = :email
            """, nativeQuery = true)
    Optional<User> findByEmail(@Param("email") String email);


}
