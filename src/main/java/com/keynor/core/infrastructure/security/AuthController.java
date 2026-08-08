package com.keynor.core.infrastructure.security;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

/**
 * Public self-registration (2026-08-05, `/api/public/**` — {@code permitAll}, same convention as
 * every other public controller) — decided with the user specifically for player accounts;
 * {@code ADMIN} is never self-registered (manually inserted, see {@code security-model.md}).
 *
 * <p>Kept flat in {@code infrastructure.security} rather than following the {@code
 * domain}/{@code application}/{@code infrastructure} hexagonal split every {@code UniverseEntity}
 * uses — {@code User} has no domain layer anywhere in this project ({@link UserEntity}, {@link
 * UserJpaRepository}, {@link UserDetailsServiceImpl} are all infrastructure-only already); this
 * follows that existing precedent rather than introducing a new one.
 */
@RestController
@RequestMapping("/api/public/v1/auth")
public class AuthController {

    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserJpaRepository userJpaRepository, PasswordEncoder passwordEncoder) {
        this.userJpaRepository = userJpaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        if (userJpaRepository.findByUsername(request.username()).isPresent()) {
            throw new DuplicateEntityNameException("User", request.username());
        }
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.DEFAULT);
        user.setEnabled(true);
        Instant now = Instant.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userJpaRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
