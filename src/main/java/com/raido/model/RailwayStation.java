package com.raido.raido.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "railwaystations")
public class RailwayStation extends BaseModel {
    private String type;
    @ManyToOne
    private RailwayRegion railwayRegion;
    private String direction;
    private Long emr;
    private Long express_3;
    private String city;

    public RailwayStation() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RailwayRegion getRailwayRegion() {
        return railwayRegion;
    }

    public void setRailwayRegion(RailwayRegion railwayRegion) {
        this.railwayRegion = railwayRegion;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Long getEmr() {
        return emr;
    }

    public void setEmr(Long emr) {
        this.emr = emr;
    }

    public Long getExpress_3() {
        return express_3;
    }

    public void setExpress_3(Long express_3) {
        this.express_3 = express_3;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
