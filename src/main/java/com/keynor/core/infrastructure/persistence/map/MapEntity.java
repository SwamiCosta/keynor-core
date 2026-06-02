package com.keynor.core.infrastructure.persistence.map;

import com.keynor.core.domain.model.place.MapType;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "maps")
public class MapEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MapType mapType;

    @Column(columnDefinition = "TEXT")
    private String image;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "map_eras", joinColumns = @JoinColumn(name = "map_id"))
    @Column(name = "era_id")
    private List<String> eraIds = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public MapType getMapType() { return mapType; }
    public void setMapType(MapType mapType) { this.mapType = mapType; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public List<String> getEraIds() { return eraIds; }
    public void setEraIds(List<String> eraIds) { this.eraIds = eraIds; }
}
