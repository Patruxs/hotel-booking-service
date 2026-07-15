package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.ApiAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApiActionRepository extends JpaRepository<ApiAction, UUID> {
    Optional<ApiAction> findByKey(String key);
}
