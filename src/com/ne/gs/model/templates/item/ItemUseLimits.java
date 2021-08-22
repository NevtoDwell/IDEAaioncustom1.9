/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.Gender;
import com.ne.gs.utils.stats.AbyssRankEnum;
import com.ne.gs.world.zone.ZoneName;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UseLimits")
public class ItemUseLimits {

    @XmlAttribute(name = "usedelay")
    private int useDelay;

    @XmlAttribute(name = "usedelayid")
    private int useDelayId;

    @XmlAttribute(name = "ownership_world")
    private int ownershipWorldId;

    @XmlAttribute
    private String usearea;

    @XmlAttribute(name = "gender")
    private Gender genderPermitted;

    @XmlAttribute(name = "rank_min")
    private int minRank;

    @XmlAttribute(name = "rank_max")
    private int maxRank = AbyssRankEnum.SUPREME_COMMANDER.getId();

    public int getDelayId() {
        return useDelayId;
    }

    public void setDelayId(int delayId) {
        useDelayId = delayId;
    }

    public int getDelayTime() {
        return useDelay;
    }

    public void setDelayTime(int useDelay) {
        this.useDelay = useDelay;
    }

    public ZoneName getUseArea() {
        if (usearea == null) {
            return null;
        }
        try {
            return ZoneName.get(usearea);
        } catch (Exception e) {
        }
        return null;
    }

    public int getOwnershipWorld() {
        return ownershipWorldId;
    }

    public Gender getGenderPermitted() {
        return genderPermitted;
    }

    public int getMinRank() {
        return minRank;
    }

    public int getMaxRank() {
        return maxRank;
    }

    public boolean verifyRank(int rank) {
        return (minRank <= rank) && (maxRank >= rank);
    }
}
