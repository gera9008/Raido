package com.raido.raido.model;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "route")
public class Route extends BaseModel {

    private String name;

    public Route() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
