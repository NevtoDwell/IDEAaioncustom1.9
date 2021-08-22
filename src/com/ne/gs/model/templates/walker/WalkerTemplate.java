/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.walker;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

import com.ne.gs.spawnengine.WalkerGroupType;

/**
 * @author KKnD
 */
@XmlRootElement(name = "walker_template")
@XmlAccessorType(XmlAccessType.FIELD)
public class WalkerTemplate {

    @XmlAttribute(name = "reversed")
    private Boolean isReversed = false;

    @XmlAttribute(name = "pool", required = true)
    private int pool = 1;

    @XmlAttribute(name = "route_id", required = true)
    private String routeId;

    @XmlAttribute(name = "formation")
    private WalkerGroupType formation = WalkerGroupType.POINT;

    @XmlAttribute(name = "rows")
    private String rowValues;

    @XmlElement(name = "routestep")
    private List<RouteStep> routeStepList;

    @XmlTransient
    private int[] rows;

    public WalkerTemplate() {
    }

    public WalkerTemplate(String routeId) {
        this.routeId = routeId;
    }

    void beforeMarshal(Marshaller marshaller) {
        if (isReversed == false) {
            isReversed = null;
        }
        if (formation == WalkerGroupType.POINT) {
            formation = null;
        }
    }

    void afterMarshal(Marshaller marshaller) {
        if (isReversed == null) {
            isReversed = false;
        }
        if (formation == null) {
            formation = WalkerGroupType.POINT;
        }
    }

    /**
     * @param u
     * @param parent
     */
    void afterUnmarshal(Unmarshaller u, Object parent) {
        if (isReversed) {
            for (int i = routeStepList.size() - 2; i > 0; i--) {
                RouteStep step = routeStepList.get(i);
                routeStepList.add(new RouteStep(step.getX(), step.getY(), step.getZ(), step.getRestTime()));
            }
        }
        for (int i = 0; i < routeStepList.size() - 1; i++) {
            routeStepList.get(i).setNextStep(routeStepList.get(i + 1));
            routeStepList.get(i).setRouteStep(i + 1);
        }
        routeStepList.get(routeStepList.size() - 1).setRouteStep(routeStepList.size());
        routeStepList.get(routeStepList.size() - 1).setNextStep(routeStepList.get(0));

        if (pool == 2) {
            formation = WalkerGroupType.SQUARE;
            rows = new int[1];
            rows[0] = 2;
        } else if (formation == WalkerGroupType.SQUARE) {
            if (rowValues != null) {
                String[] values = rowValues.split(",");
                rows = new int[values.length];
                for (int i = 0; i < values.length; i++) {
                    rows[i] = Integer.parseInt(values[i]);
                }
            } else {
                formation = WalkerGroupType.POINT;
            }
        }
        rowValues = null;
    }

    public List<RouteStep> getRouteSteps() {
        return routeStepList;
    }

    public RouteStep getRouteStep(int value) {
        return routeStepList.get(value - 1);
    }

    public String getRouteId() {
        return routeId;
    }

    public int getPool() {
        return pool;
    }

    public void setPool(int pool) {
        this.pool = pool;
    }

    public void setRouteSteps(ArrayList<RouteStep> newSteps) {
        routeStepList = newSteps;
    }

    public boolean isReversed() {
        return isReversed;
    }

    public void setIsReversed(boolean value) {
        isReversed = value;
    }

    public WalkerGroupType getType() {
        return formation;
    }

    /**
     * @return the rows
     */
    public int[] getRows() {
        return rows;
    }

}
