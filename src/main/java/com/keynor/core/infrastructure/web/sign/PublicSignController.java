package com.keynor.core.infrastructure.web.sign;

import com.keynor.core.application.dto.sign.SignResponse;
import com.keynor.core.domain.port.in.sign.FindAllSignsUseCase;
import com.keynor.core.domain.port.in.sign.FindSignByIdUseCase;
import com.keynor.core.infrastructure.web.shared.LanguageRequestParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/v1/signs")
public class PublicSignController {

    private final FindAllSignsUseCase findAllSignsUseCase;
    private final FindSignByIdUseCase findSignByIdUseCase;

    public PublicSignController(
            FindAllSignsUseCase findAllSignsUseCase,
            FindSignByIdUseCase findSignByIdUseCase) {
        this.findAllSignsUseCase = findAllSignsUseCase;
        this.findSignByIdUseCase = findSignByIdUseCase;
    }

    @GetMapping
    public ResponseEntity<List<SignResponse>> findAll(@RequestParam String language) {
        return ResponseEntity.ok(
                findAllSignsUseCase.findAll(LanguageRequestParser.parse(language)).stream().map(SignResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SignResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(SignResponse.from(findSignByIdUseCase.findById(id)));
    }
}
