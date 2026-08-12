package com.keynor.core.domain.model.shared;

import com.keynor.core.domain.exception.InvalidStatusTransitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class UniverseEntity {

    private static final Logger log = LoggerFactory.getLogger(UniverseEntity.class);

    private final UUID id;
    private String name;
    private String summary;
    private String body;
    private List<String> images;
    private EntityStatus status;
    private Timeline timeline;
    private final Instant createdAt;
    private Instant updatedAt;
    private final Language language;
    private final UUID translationGroupId;
    private final UUID versionGroupId;
    private boolean hidden;
    private boolean common;

    protected UniverseEntity(
            UUID id,
            String name,
            String summary,
            String body,
            List<String> images,
            EntityStatus status,
            Timeline timeline,
            Instant createdAt,
            Instant updatedAt,
            Language language,
            UUID translationGroupId,
            UUID versionGroupId,
            boolean hidden,
            boolean common) {
        if (hidden && status != EntityStatus.CANON) {
            throw new IllegalArgumentException("Hidden content must always have status CANON");
        }
        this.id = id;
        this.name = name;
        this.summary = summary;
        this.body = body;
        this.images = new ArrayList<>(images);
        this.status = status;
        this.timeline = timeline;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.language = language;
        this.translationGroupId = translationGroupId;
        this.versionGroupId = versionGroupId;
        this.hidden = hidden;
        this.common = common;
    }

    public void changeStatus(EntityStatus newStatus) {
        if (!isValidTransition(this.status, newStatus)) {
            throw new InvalidStatusTransitionException(this.status, newStatus);
        }
        EntityStatus previousStatus = this.status;
        this.status = newStatus;
        this.updatedAt = Instant.now();
        log.info("Status changed: entity={} from={} to={}", this.id, previousStatus, newStatus);
    }

    protected void updateBaseFields(String name, String summary, String body, List<String> images, Timeline timeline) {
        this.name = name;
        this.summary = summary;
        this.body = body;
        this.images = new ArrayList<>(images);
        this.timeline = timeline;
        this.updatedAt = Instant.now();
    }

    /**
     * Toggles hidden status on an existing entity -- e.g. marking a
     * previously-visible Lore entry hidden after the fact. Same invariant as
     * the constructor: hidden is only valid when status is already CANON.
     * The caller (Service) is responsible for also creating/refreshing the
     * HiddenContentLock when turning hidden on -- this method only flips the
     * flag, it does not know about riddles or passwords.
     */
    public void setHidden(boolean hidden) {
        if (hidden && this.status != EntityStatus.CANON) {
            throw new IllegalArgumentException("Hidden content must always have status CANON");
        }
        this.hidden = hidden;
        this.updatedAt = Instant.now();
    }

    /**
     * Toggles whether this entity is "common" -- excluded from every public
     * list/browse endpoint and from map pin rendering, but still reachable
     * via findById and via another entity's resolved links (no lock, unlike
     * hidden). No status invariant, unlike hidden: a common entity may be
     * DRAFT, CANON, or DEPRECATED.
     */
    public void setCommon(boolean common) {
        this.common = common;
        this.updatedAt = Instant.now();
    }

    private boolean isValidTransition(EntityStatus from, EntityStatus to) {
        return switch (from) {
            case DRAFT -> to == EntityStatus.CANON || to == EntityStatus.DEPRECATED;
            case CANON -> to == EntityStatus.DRAFT || to == EntityStatus.DEPRECATED;
            case DEPRECATED -> to == EntityStatus.DRAFT;
        };
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSummary() { return summary; }
    public String getBody() { return body; }
    public List<String> getImages() { return List.copyOf(images); }
    public EntityStatus getStatus() { return status; }
    public Timeline getTimeline() { return timeline; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Language getLanguage() { return language; }
    public UUID getTranslationGroupId() { return translationGroupId; }
    public UUID getVersionGroupId() { return versionGroupId; }
    public boolean isHidden() { return hidden; }
    public boolean isCommon() { return common; }
}
