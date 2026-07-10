package com.keynor.core.infrastructure.web.archetype;

import com.keynor.core.application.dto.archetype.ArchetypeResponse;
import com.keynor.core.domain.port.in.archetype.FindAllArchetypesUseCase;
import com.keynor.core.domain.port.in.archetype.FindArchetypeByIdUseCase;
import com.keynor.core.infrastructure.web.shared.LanguageRequestParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/v1/archetypes")
public class PublicArchetypeController {

    private final FindAllArchetypesUseCase findAllArchetypesUseCase;
    private final FindArchetypeByIdUseCase findArchetypeByIdUseCase;

    public PublicArchetypeController(
            FindAllArchetypesUseCase findAllArchetypesUseCase,
            FindArchetypeByIdUseCase findArchetypeByIdUseCase) {
        this.findAllArchetypesUseCase = findAllArchetypesUseCase;
        this.findArchetypeByIdUseCase = findArchetypeByIdUseCase;
    }

    @GetMapping
    public ResponseEntity<List<ArchetypeResponse>> findAll(@RequestParam String language) {
        return ResponseEntity.ok(
                findAllArchetypesUseCase.findAll(LanguageRequestParser.parse(language)).stream().map(ArchetypeResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArchetypeResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ArchetypeResponse.from(findArchetypeByIdUseCase.findById(id)));
    }
}
