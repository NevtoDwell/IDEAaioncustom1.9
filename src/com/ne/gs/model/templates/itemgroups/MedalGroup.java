/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.itemgroups;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import com.ne.gs.model.templates.rewards.MedalItem;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MedalGroup")
public class MedalGroup extends BonusItemGroup {

    @XmlElement(name = "item")
    protected List<MedalItem> items;

    public List<MedalItem> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    @Override
    public ItemRaceEntry[] getRewards() {
        return getItems().toArray(new ItemRaceEntry[0]);
    }
}
