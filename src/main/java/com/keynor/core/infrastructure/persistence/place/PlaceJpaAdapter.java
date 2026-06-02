package com.keynor.core.infrastructure.persistence.place;

import com.keynor.core.domain.model.place.Place;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.out.PlaceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PlaceJpaAdapter implements PlaceRepository {

    private final PlaceJpaRepository jpaRepository;
    private final PlaceMapper mapper;

    public PlaceJpaAdapter(PlaceJpaRepository jpaRepository, PlaceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Place save(Place place) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(place)));
    }

    @Override
    public Optional<Place> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public PageResult<Place> findAll(EntityFilter filter, com.keynor.core.domain.model.shared.PageRequest pageRequest) {
        Specification<PlaceEntity> spec = PlaceSpecifications.fromFilter(filter);
        PageRequest springPage = PageRequest.of(pageRequest.page(), pageRequest.size());
        Page<PlaceEntity> page = jpaRepository.findAll(spec, springPage);
        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements());
    }
}
