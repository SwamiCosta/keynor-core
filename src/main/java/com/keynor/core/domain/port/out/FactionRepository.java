package com.keynor.core.domain.port.out;

import com.keynor.core.domain.model.faction.Faction;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface FactionRepository {
    Faction save(Faction faction);
    Optional<Faction> findById(UUID id);
    boolean existsById(UUID id);
    boolean existsByNameAndLanguage(String name, Language language);
    void deleteById(UUID id);
    PageResult<Faction> findAll(EntityFilter filter, PageRequest pageRequest);
}
