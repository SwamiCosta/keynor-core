package com.keynor.core.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keynor.core.domain.model.shared.HiddenUnlockToken;
import com.keynor.core.domain.port.out.HiddenUnlockTokenSigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;

/**
 * Signs and verifies the stateless hidden-content unlock token. Deliberately
 * decoupled from the OAuth2/PKCE authorization flow (AuthorizationServerConfig)
 * -- that flow is tied to real content-authoring accounts, not anonymous
 * public browsing. See root ARCHITECTURE.md -- "Cross-Project Feature:
 * Hidden Content & Black Pins".
 *
 * The HMAC key is generated once per application boot and kept only in
 * memory: a restart invalidates every previously issued token. That is an
 * acceptable trade-off for a puzzle mechanic (worst case, a visitor
 * re-solves a riddle) and avoids adding a new dependency or a new
 * application.yml entry for a persisted signing key -- both are protected
 * actions this agent may not take without separate authorization.
 */
@Component
public class HmacHiddenUnlockTokenSigner implements HiddenUnlockTokenSigner {

    private static final Logger log = LoggerFactory.getLogger(HmacHiddenUnlockTokenSigner.class);
    private static final String ALGORITHM = "HmacSHA256";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecretKeySpec key;

    public HmacHiddenUnlockTokenSigner() {
        byte[] secret = new byte[32];
        new SecureRandom().nextBytes(secret);
        this.key = new SecretKeySpec(secret, ALGORITHM);
    }

    @Override
    public String issue(HiddenUnlockToken token) {
        try {
            Payload payload = new Payload(token.unlockedKeys(), token.all(), token.expiresAt().toEpochMilli());
            byte[] payloadBytes = objectMapper.writeValueAsBytes(payload);
            String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadBytes);
            return encodedPayload + "." + sign(encodedPayload);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to issue hidden unlock token", e);
        }
    }

    @Override
    public Optional<HiddenUnlockToken> verify(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            int separator = token.indexOf('.');
            if (separator < 0) {
                return Optional.empty();
            }
            String encodedPayload = token.substring(0, separator);
            String signature = token.substring(separator + 1);
            if (!constantTimeEquals(signature, sign(encodedPayload))) {
                return Optional.empty();
            }
            byte[] payloadBytes = Base64.getUrlDecoder().decode(encodedPayload);
            Payload payload = objectMapper.readValue(payloadBytes, Payload.class);
            return Optional.of(new HiddenUnlockToken(
                    payload.unlockedKeys(), payload.all(), Instant.ofEpochMilli(payload.expiresAtEpochMilli())));
        } catch (Exception e) {
            log.warn("Rejected malformed hidden unlock token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String sign(String encodedPayload) throws Exception {
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(key);
        byte[] signature = mac.doFinal(encodedPayload.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    private record Payload(Set<String> unlockedKeys, boolean all, long expiresAtEpochMilli) {}
}
