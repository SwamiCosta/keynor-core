package com.keynor.core.infrastructure.persistence.shared;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "hidden_content_lock")
public class HiddenContentLockEntity {

    @EmbeddedId
    private HiddenContentLockId id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String riddleText;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public HiddenContentLockId getId() { return id; }
    public void setId(HiddenContentLockId id) { this.id = id; }
    public String getRiddleText() { return riddleText; }
    public void setRiddleText(String riddleText) { this.riddleText = riddleText; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
