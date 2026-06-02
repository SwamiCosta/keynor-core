package com.keynor.core.infrastructure.persistence.map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MapJpaRepository extends JpaRepository<MapEntity, String> {

    @Query("SELECT m FROM MapEntity m JOIN m.eraIds e WHERE e = :eraId")
    List<MapEntity> findByEraId(@Param("eraId") String eraId);
}
