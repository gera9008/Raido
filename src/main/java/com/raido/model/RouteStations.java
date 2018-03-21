package com.raido.raido.model;

import javax.persistence.*;

@Entity
@Table(name = "routeStations")
public class RouteStations extends BaseModel {

    private Integer number;
    @PrimaryKeyJoinColumn
    @OneToOne(cascade = CascadeType.ALL)
    private Route route;
    @OneToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private RailwayStation railwayStation;


    public RouteStations() {
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
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
}
