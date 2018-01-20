package com.raido.raido.model;

import javax.persistence.*;

@Entity
@Table(name = "routeStationTime")
public class RouteStationTime extends BaseModel {

    @PrimaryKeyJoinColumn
    @OneToOne(cascade = CascadeType.ALL)
    private Route route;
    @PrimaryKeyJoinColumn
    @OneToOne(cascade = CascadeType.ALL)
    private RailwayStation railwayStation;
    private String time;

    public RouteStationTime() {
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public RailwayStation getRailwayStation() {
        return railwayStation;
    }

    public void setRailwayStation(RailwayStation railwayStation) {
        this.railwayStation = railwayStation;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
