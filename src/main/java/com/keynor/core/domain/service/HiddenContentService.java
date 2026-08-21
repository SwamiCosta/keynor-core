package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.exception.InvalidHiddenContentPasswordException;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.HiddenContentLock;
import com.keynor.core.domain.model.shared.HiddenUnlockToken;
import com.keynor.core.domain.port.in.shared.CreateHiddenContentLockUseCase;
import com.keynor.core.domain.port.in.shared.HiddenContentAccessUseCase;
import com.keynor.core.domain.port.out.HiddenContentLockRepository;
import com.keynor.core.domain.port.out.HiddenUnlockTokenSigner;
import com.keynor.core.domain.port.out.PasswordHasher;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HiddenContentService implements CreateHiddenContentLockUseCase, HiddenContentAccessUseCase {

    /**
     * Compared as plaintext against the request; not stored anywhere, so
     * hashing this constant would add no real protection. Supplied by the
     * user directly (workspace SKILLS.md Skill 14, Ask Before Inferring) --
     * not something an agent may invent.
     */
    private static final String MASTER_PASSWORD = "PunicEradisFnn";

    private static final Duration TOKEN_TTL = Duration.ofHours(2);

    private final HiddenContentLockRepository hiddenContentLockRepository;
    private final PasswordHasher passwordHasher;
    private final HiddenUnlockTokenSigner tokenSigner;

    public HiddenContentService(
            HiddenContentLockRepository hiddenContentLockRepository,
            PasswordHasher passwordHasher,
            HiddenUnlockTokenSigner tokenSigner) {
        this.hiddenContentLockRepository = hiddenContentLockRepository;
        this.passwordHasher = passwordHasher;
        this.tokenSigner = tokenSigner;
    }

    @Override
    public HiddenContentLock createOrReplace(EntityType type, UUID id, String riddleText, String rawPassword) {
        Instant now = Instant.now();
        Instant createdAt = hiddenContentLockRepository.findByEntity(type, id)
                .map(HiddenContentLock::createdAt)
                .orElse(now);
        HiddenContentLock lock = new HiddenContentLock(type, id, riddleText, passwordHasher.hash(rawPassword), createdAt, now);
        return hiddenContentLockRepository.save(lock);
    }

    @Override
    public UnlockResult unlock(EntityType type, UUID id, String password, String existingToken) {
        HiddenContentLock lock = hiddenContentLockRepository.findByEntity(type, id)
                .orElseThrow(() -> new EntityNotFoundException("HiddenContentLock", id));

        boolean isMaster = MASTER_PASSWORD.equals(password);
        if (!isMaster && !passwordHasher.matches(password, lock.passwordHash())) {
            throw new InvalidHiddenContentPasswordException();
        }

        Set<String> mergedKeys = new HashSet<>(currentUnlockedKeys(existingToken));
        mergedKeys.add(HiddenUnlockToken.key(type, id));
        boolean all = isMaster || wasAlreadyAll(existingToken);

        Instant expiresAt = Instant.now().plus(TOKEN_TTL);
        String signed = tokenSigner.issue(new HiddenUnlockToken(mergedKeys, all, expiresAt));
        return new UnlockResult(signed, all, expiresAt);
    }

    @Override
    public boolean hasAccess(String token, EntityType type, UUID id) {
        return tokenSigner.verify(token)
                .filter(t -> !t.isExpired())
                .map(t -> t.grantsAccess(type, id))
                .orElse(false);
    }

    @Override
    public String findRiddle(EntityType type, UUID id) {
        return hiddenContentLockRepository.findByEntity(type, id)
                .orElseThrow(() -> new EntityNotFoundException("HiddenContentLock", id))
                .riddleText();
    }

    private Set<String> currentUnlockedKeys(String existingToken) {
        return validToken(existingToken).map(HiddenUnlockToken::unlockedKeys).orElse(Set.of());
    }

    private boolean wasAlreadyAll(String existingToken) {
        return validToken(existingToken).map(HiddenUnlockToken::all).orElse(false);
    }

    private java.util.Optional<HiddenUnlockToken> validToken(String token) {
        if (token == null || token.isBlank()) {
            return java.util.Optional.empty();
        }
        return tokenSigner.verify(token).filter(t -> !t.isExpired());
    }
}
