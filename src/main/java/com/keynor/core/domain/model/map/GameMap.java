package com.keynor.core.domain.model.map;

import com.keynor.core.domain.model.place.MapType;

import java.util.List;

public class GameMap {

    private final String id;
    private final String name;
    private final MapType mapType;
    private final String image;
    private final List<String> eraIds;

    public GameMap(String id, String name, MapType mapType, String image, List<String> eraIds) {
        this.id = id;
        this.name = name;
        this.mapType = mapType;
        this.image = image;
        this.eraIds = List.copyOf(eraIds);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public MapType getMapType() { return mapType; }
    public String getImage() { return image; }
    public List<String> getEraIds() { return eraIds; }
}
