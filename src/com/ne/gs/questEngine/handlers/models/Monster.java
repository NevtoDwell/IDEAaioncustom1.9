/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * @author MrPoke
 * @reworked vlog
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Monster")
public class Monster {

    @XmlAttribute(name = "var", required = true)
    protected int var;
    @XmlAttribute(name = "start_var")
    protected Integer startVar;
    @XmlAttribute(name = "end_var", required = true)
    protected int endVar;
    @XmlAttribute(name = "npc_ids", required = true)
    protected List<Integer> npcIds;

    public int getVar() {
        return var;
    }

    public Integer getStartVar() {
        return startVar;
    }

    public int getEndVar() {
        return endVar;
    }

    public List<Integer> getNpcIds() {
        return npcIds;
    }
}
