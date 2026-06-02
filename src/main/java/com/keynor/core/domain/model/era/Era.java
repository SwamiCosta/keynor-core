package com.keynor.core.domain.model.era;

import com.keynor.core.domain.model.place.MapType;

public class Era {

    private final String id;
    private final String name;
    private final int eraOrder;
    private final String period;
    private final String summary;
    private final MapType mapType;
    private final String defaultMap;
    private final String color;

    public Era(String id, String name, int eraOrder, String period, String summary,
               MapType mapType, String defaultMap, String color) {
        this.id = id;
        this.name = name;
        this.eraOrder = eraOrder;
        this.period = period;
        this.summary = summary;
        this.mapType = mapType;
        this.defaultMap = defaultMap;
        this.color = color;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getEraOrder() { return eraOrder; }
    public String getPeriod() { return period; }
    public String getSummary() { return summary; }
    public MapType getMapType() { return mapType; }
    public String getDefaultMap() { return defaultMap; }
    public String getColor() { return color; }
}
