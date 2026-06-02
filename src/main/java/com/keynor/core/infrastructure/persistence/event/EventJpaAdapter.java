package com.keynor.core.infrastructure.persistence.event;

import com.keynor.core.domain.model.event.Event;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.out.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class EventJpaAdapter implements EventRepository {

    private final EventJpaRepository jpaRepository;
    private final EventMapper mapper;

    public EventJpaAdapter(EventJpaRepository jpaRepository, EventMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Event save(Event event) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(event)));
    }

    @Override
    public Optional<Event> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(UUID id) { return jpaRepository.existsById(id); }

    @Override
    public boolean existsByName(String name) { return jpaRepository.existsByName(name); }

    @Override
    public void deleteById(UUID id) { jpaRepository.deleteById(id); }

    @Override
    public PageResult<Event> findAll(EntityFilter filter, com.keynor.core.domain.model.shared.PageRequest pageRequest) {
        Page<EventEntity> page = jpaRepository.findAll(
                EventSpecifications.fromFilter(filter),
                PageRequest.of(pageRequest.page(), pageRequest.size()));
        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
