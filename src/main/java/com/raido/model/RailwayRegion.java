package com.raido.raido.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "railwayRegion")
public class RailwayRegion extends BaseModel {

    private String name;

    @OneToMany(mappedBy = "railwayRegion", fetch = FetchType.LAZY)
    private List<RailwayStation> railwayStations;

    public RailwayRegion() {
    }

    public List<RailwayStation> getRailwayStations() {
        return railwayStations;
    }

    public void setRailwayStations(List<RailwayStation> railwayStations) {
        this.railwayStations = railwayStations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
