package com.keynor.core.infrastructure.persistence.era;

import com.keynor.core.domain.model.place.MapType;
import jakarta.persistence.*;

@Entity
@Table(name = "eras")
public class EraEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int eraOrder;

    private String period;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MapType mapType;

    private String defaultMap;

    private String color;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getEraOrder() { return eraOrder; }
    public void setEraOrder(int eraOrder) { this.eraOrder = eraOrder; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public MapType getMapType() { return mapType; }
    public void setMapType(MapType mapType) { this.mapType = mapType; }
    public String getDefaultMap() { return defaultMap; }
    public void setDefaultMap(String defaultMap) { this.defaultMap = defaultMap; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
