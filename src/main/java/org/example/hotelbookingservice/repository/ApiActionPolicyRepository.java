package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.ApiActionPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApiActionPolicyRepository extends JpaRepository<ApiActionPolicy, UUID> {
    List<ApiActionPolicy> findByActionId(UUID actionId);
}
