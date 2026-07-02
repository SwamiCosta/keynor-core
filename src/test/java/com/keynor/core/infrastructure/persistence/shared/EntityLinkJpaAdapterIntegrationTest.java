package com.keynor.core.infrastructure.persistence.shared;

import com.keynor.core.domain.model.shared.EntityLink;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.port.out.EntityLinkRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EntityLinkJpaAdapterIntegrationTest {

    @Autowired
    private EntityLinkRepository entityLinkRepository;

    @Test
    void replaceLinks_shouldNotThrow_whenResubmittingUnchangedLinks() {
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        List<EntityLinkRef> links = List.of(new EntityLinkRef(EntityType.CHARACTER, targetId));

        entityLinkRepository.replaceLinks(EntityType.LORE, sourceId, links);

        assertThatCode(() -> entityLinkRepository.replaceLinks(EntityType.LORE, sourceId, links))
                .doesNotThrowAnyException();

        List<EntityLink> result = entityLinkRepository.findBySource(EntityType.LORE, sourceId);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetId()).isEqualTo(targetId);
    }

    @Test
    void replaceLinks_shouldNotThrow_whenResubmittingPartiallyOverlappingSet() {
        UUID sourceId = UUID.randomUUID();
        UUID existingTargetId = UUID.randomUUID();
        UUID newTargetId = UUID.randomUUID();
        List<EntityLinkRef> initialLinks = List.of(new EntityLinkRef(EntityType.CHARACTER, existingTargetId));

        entityLinkRepository.replaceLinks(EntityType.LORE, sourceId, initialLinks);

        List<EntityLinkRef> overlappingLinks = List.of(
                new EntityLinkRef(EntityType.CHARACTER, existingTargetId),
                new EntityLinkRef(EntityType.CHARACTER, newTargetId));

        assertThatCode(() -> entityLinkRepository.replaceLinks(EntityType.LORE, sourceId, overlappingLinks))
                .doesNotThrowAnyException();

        List<EntityLink> result = entityLinkRepository.findBySource(EntityType.LORE, sourceId);
        assertThat(result).extracting(EntityLink::getTargetId)
                .containsExactlyInAnyOrder(existingTargetId, newTargetId);
    }
}
