/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.customrifts;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.modules.common.ItemList;
import com.ne.gs.modules.common.PosList;
import com.ne.gs.modules.common.Time;

/**
 * @author hex1r0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RiftSpawn")
public class RiftSpawn {

    @XmlAttribute(name = "id", required = true)
    protected Integer _id;

    @XmlElement(name = "races", required = true)
    protected RaceList _raceList;

    @XmlElement(name = "sources", required = true)
    protected PosList _sourceList;

    @XmlElement(name = "targets", required = true)
    protected RiftPosList _targetList;

    @XmlElement(name = "items")
    protected ItemList _itemList;

    @XmlElement(name = "time")
    protected Time _time;

    @XmlElement(name = "level")
    protected Level _level;

    @XmlAttribute(name = "info")
    protected String _info;

    public Integer getId() {
        return _id;
    }

    public void setId(Integer id) {
        _id = id;
    }

    public RaceList getRaceList() {
        return _raceList;
    }

    public void setRaceList(RaceList value) {
        _raceList = value;
    }

    public PosList getSourceList() {
        return _sourceList;
    }

    public void setSourceList(PosList sourceList) {
        _sourceList = sourceList;
    }

    public RiftPosList getTargetList() {
        return _targetList;
    }

    public void setTargetList(RiftPosList value) {
        _targetList = value;
    }

    public ItemList getItemList() {
        return _itemList;
    }

    public void setItemList(ItemList value) {
        _itemList = value;
    }

    public Time getTime() {
        return _time;
    }

    public void setTime(Time value) {
        _time = value;
    }

    public Level getLevel() {
        return _level;
    }

    public void setLevel(Level level) {
        _level = level;
    }


}
