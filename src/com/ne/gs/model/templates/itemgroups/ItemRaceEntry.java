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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.Race;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.model.templates.rewards.IdLevelReward;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemRaceEntry")
@XmlSeeAlso({IdLevelReward.class})
public class ItemRaceEntry {

    @XmlAttribute(name = "id", required = true)
    protected int id;

    @XmlAttribute(name = "race")
    protected Race race;

    public int getId() {
        return id;
    }

    public Race getRace() {
        return race;
    }

    public boolean checkRace(Race playerRace) {
        ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(id);
        return (template.getRace() == Race.PC_ALL && (race == null || race == playerRace)) || (template.getRace() != Race.PC_ALL && template.getRace() == playerRace);
    }
}
