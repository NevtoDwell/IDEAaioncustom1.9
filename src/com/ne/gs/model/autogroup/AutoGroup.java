/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.autogroup;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.Collections;
import java.util.List;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AutoGroup")
public class AutoGroup {

    @XmlAttribute(required = true)
    protected byte id;
    @XmlAttribute(required = true)
    protected int instanceId;
    @XmlAttribute(name = "name_id")
    protected Integer nameId;
    @XmlAttribute(name = "title_id")
    protected Integer titleId;
    @XmlAttribute(name = "min_lvl")
    protected Integer minLvl;
    @XmlAttribute(name = "max_lvl")
    protected Integer maxLvl;
    @XmlAttribute(name = "register_quick")
    protected Boolean registerQuick;
    @XmlAttribute(name = "register_group")
    protected Boolean registerGroup;
    @XmlAttribute(name = "register_new")
    protected Boolean registerNew;
    @XmlAttribute(name = "npc_ids")
    protected List<Integer> npcIds;

    public byte getId() {
        return id;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public int getNameId() {
        return nameId;
    }

    public int getTitleId() {
        return titleId;
    }

    public int getMinLvl() {
        return minLvl;
    }

    public int getMaxLvl() {
        return maxLvl;
    }

    public Boolean hasRegisterQuick() {
        return registerQuick;
    }

    public Boolean hasRegisterGroup() {
        return registerGroup;
    }

    public Boolean hasRegisterNew() {
        return registerNew;
    }

    public List<Integer> getNpcIds() {
        if (npcIds == null) {
            npcIds = Collections.emptyList();
        }
        return npcIds;
    }
}
