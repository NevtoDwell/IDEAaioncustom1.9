/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.housing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author hex1r0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
@XmlRootElement(name = "item")
public class HouseItemTemplate {

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType
    @XmlRootElement(name = "items")
    public static class List {

        @XmlElement(name = "item")
        private java.util.List<HouseItemTemplate> _houseItemTemplates;

        public java.util.List<HouseItemTemplate> getTemplates() {
            if (_houseItemTemplates == null) {
                _houseItemTemplates = new java.util.ArrayList<>(0);
            }
            return _houseItemTemplates;
        }

    }

    @XmlAttribute(required = true)
    int id;
    @XmlAttribute(required = true)
    int dscId;
    @XmlAttribute
    HouseItem.Type type = HouseItem.Type.NONE;
    @XmlAttribute
    int lifetime;
    @XmlAttribute
    short warmup;
    @XmlAttribute
    short cooldown;
    @XmlAttribute
    short usages;
    @XmlAttribute
    private byte daily;
    @XmlAttribute
    byte storageId;
    @XmlAttribute
    int npcId;
    @XmlAttribute
    int consumeItemId;
    @XmlAttribute
    byte consumeCount;
    @XmlAttribute
    int equippedItemId;
    @XmlAttribute
    int rewardId;
    @XmlAttribute
    int finalRewardId;

    boolean isDaily() {
        return daily == 1;
    }

    void setDaily(boolean value) {
        daily = (byte) (value ? 1 : 0);
    }

    boolean isConsumingItem() {
        return consumeItemId != 0;
    }

    boolean isEquipRequired() {
        return equippedItemId != 0;
    }

    boolean hasReward() {
        return rewardId != 0;
    }

    boolean hasFinalReward() {
        return finalRewardId != 0;
    }

    boolean isUseLimited() {
        return usages > 0;
    }

    public boolean isExpirable() {
        return lifetime > 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof HouseItemTemplate && id == ((HouseItemTemplate) obj).id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
