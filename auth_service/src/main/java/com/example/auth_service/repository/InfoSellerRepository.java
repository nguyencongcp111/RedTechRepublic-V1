package com.example.auth_service.repository;

import com.example.auth_service.entity.InfoSeller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InfoSellerRepository extends JpaRepository<InfoSeller, UUID> {
    @EntityGraph(attributePaths = "user")
    Optional<InfoSeller> findByUserId(UUID userId);

    @EntityGraph(attributePaths = "user")
    Optional<InfoSeller> findWithUserById(UUID id);

    @EntityGraph(attributePaths = "user")
    Page<InfoSeller> findByStatus(InfoSeller.Status status, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    List<InfoSeller> findByUserIdIn(Collection<UUID> userIds);

    long countByStatus(InfoSeller.Status status);
}
