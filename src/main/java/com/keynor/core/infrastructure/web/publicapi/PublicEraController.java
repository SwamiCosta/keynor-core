package com.keynor.core.infrastructure.web.publicapi;

import com.keynor.core.application.dto.era.EraResponse;
import com.keynor.core.domain.port.in.era.FindAllErasUseCase;
import com.keynor.core.domain.port.in.era.FindEraByIdUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/v1/eras")
public class PublicEraController {

    private final FindAllErasUseCase findAllErasUseCase;
    private final FindEraByIdUseCase findEraByIdUseCase;

    public PublicEraController(
            FindAllErasUseCase findAllErasUseCase,
            FindEraByIdUseCase findEraByIdUseCase) {
        this.findAllErasUseCase = findAllErasUseCase;
        this.findEraByIdUseCase = findEraByIdUseCase;
    }

    @GetMapping
    public ResponseEntity<List<EraResponse>> findAll() {
        return ResponseEntity.ok(findAllErasUseCase.findAll().stream().map(EraResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EraResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(EraResponse.from(findEraByIdUseCase.findById(id)));
    }
}
