package com.keynor.core.infrastructure.persistence.sign;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SignJpaRepository extends JpaRepository<SignEntity, UUID> {

    List<SignEntity> findAllByOrderBySignOrderAsc();
}
