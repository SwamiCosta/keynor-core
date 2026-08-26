package com.keynor.core.infrastructure.persistence.map;

import com.keynor.core.domain.model.map.MapPin;
import com.keynor.core.domain.model.map.PinShape;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.port.out.MapPinRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MapPinJpaAdapterIntegrationTest {

    @Autowired
    private MapPinRepository mapPinRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String MAP_ID = "test-map";

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO maps (id, name, map_type, image) VALUES (?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                MAP_ID, "Test Map", "NAVIGABLE", "test.png");
    }

    @Test
    void save_shouldPersistAndBeFoundByMapId() {
        UUID entityId = UUID.randomUUID();
        MapPin pin = new MapPin(UUID.randomUUID(), MAP_ID, EntityType.CHARACTER, entityId, null, PinShape.DEFAULT, 0.25, 0.75, Instant.now());

        mapPinRepository.save(pin);

        assertThat(mapPinRepository.findByMapId(MAP_ID))
                .extracting(MapPin::getEntityId)
                .containsExactly(entityId);
        assertThat(mapPinRepository.existsByMapIdAndEntityTypeAndEntityId(MAP_ID, EntityType.CHARACTER, entityId)).isTrue();
    }

    @Test
    void save_shouldPersistAndReloadShape() {
        UUID entityId = UUID.randomUUID();
        MapPin pin = new MapPin(UUID.randomUUID(), MAP_ID, EntityType.CHARACTER, entityId, null, PinShape.STAR, 0.25, 0.75, Instant.now());

        MapPin saved = mapPinRepository.save(pin);

        assertThat(mapPinRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(MapPin::getShape)
                .isEqualTo(PinShape.STAR);
    }

    @Test
    void deleteById_shouldRemovePin() {
        UUID entityId = UUID.randomUUID();
        MapPin pin = mapPinRepository.save(
                new MapPin(UUID.randomUUID(), MAP_ID, EntityType.PLACE, entityId, null, PinShape.DEFAULT, 0.1, 0.9, Instant.now()));

        mapPinRepository.deleteById(pin.getId());

        assertThat(mapPinRepository.findById(pin.getId())).isEmpty();
    }

    @Test
    void save_shouldPersistPinWithNoEntityAndCustomName() {
        MapPin pin = new MapPin(UUID.randomUUID(), MAP_ID, null, null, "Uncharted Ruins", PinShape.DEFAULT, 0.4, 0.6, Instant.now());

        MapPin saved = mapPinRepository.save(pin);

        assertThat(mapPinRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getEntityType()).isNull();
                    assertThat(found.getEntityId()).isNull();
                    assertThat(found.getName()).isEqualTo("Uncharted Ruins");
                });
    }
}
