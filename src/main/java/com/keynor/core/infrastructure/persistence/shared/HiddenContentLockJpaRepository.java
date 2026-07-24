package com.keynor.core.infrastructure.persistence.shared;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HiddenContentLockJpaRepository extends JpaRepository<HiddenContentLockEntity, HiddenContentLockId> {
}
