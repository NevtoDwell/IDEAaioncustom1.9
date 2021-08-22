/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.drop;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Collections;
import java.util.List;

import com.ne.gs.model.Race;
import com.ne.gs.model.templates.npc.NpcRating;

/**
 * @author Kolobrodik
 */
@XmlRootElement(name = "drop_data")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "globalDrop", propOrder = {"dropGroup"})
public class GlobalDrop {

    @XmlElement(name = "group")
    protected List<DropGroup> dropGroup;

    @XmlAttribute(name = "level", required = true)
    protected int level;

    @XmlAttribute(name = "npc_race")
    protected Race npcRace = Race.NONE;

    @XmlAttribute(name = "npc_rating")
    protected NpcRating npcRating = NpcRating.NORMAL;

    public List<DropGroup> getDropGroup() {
        if (dropGroup == null) {
            return Collections.emptyList();
        }
        return dropGroup;
    }

    public int getLevel() {
        return level;
    }

    public Race getNpcRace() {
        return npcRace;
    }

    public NpcRating getNpcRating() {
        return npcRating;
    }
}
