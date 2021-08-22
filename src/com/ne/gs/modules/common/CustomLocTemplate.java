/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.utils.xml.ElementList;

/**
 * @author hex1r0
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "CustomLocTemplate")
public class CustomLocTemplate extends ElementList<NpcSpawnList> {

    @XmlAttribute(name = "id", required = true)
    protected String _id;

    @XmlAttribute(name = "mapId", required = true)
    protected Integer _mapId;

    @XmlAttribute(name = "handler")
    protected String _handler;

    @XmlElement(name = "properties")
    protected PropertyList _propertyList = new PropertyList();

    @XmlElement(name = "times")
    protected TimeList _timeList = new TimeList();

    @XmlAttribute(name = "race_restriction")
    protected int race_restriction = -1;

    @XmlAttribute(name = "max_players")
    protected int max_players;

    @XmlElement(name = "revive_positions", required = true)
    protected PosList _revivePositions = new PosList();

    @XmlElement(name = "spawns")
    public List<NpcSpawnList> getSpawnLists() {
        return getElements();
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    public Integer getMapId() {
        return _mapId;
    }

    public void setMapId(Integer mapId) {
        _mapId = mapId;
    }

    public String getHandler() {
        return _handler;
    }

    public void setHandler(String handler) {
        _handler = handler;
    }

    @NotNull
    public PropertyList getPropertyList() {
        return _propertyList;
    }

    public void setPropertyList(@NotNull PropertyList propertyList) {
        _propertyList = propertyList;
    }

    public PosList getRevivePositions() {
        return _revivePositions;
    }

    @NotNull
    public TimeList getTimeList() {
        return _timeList;
    }

    public void setTimeList(@NotNull TimeList value) {
        _timeList = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomLocTemplate that = (CustomLocTemplate) o;

        return _id.equals(that._id);

    }

    @Override
    public int hashCode() {
        return _id.hashCode();
    }
}
