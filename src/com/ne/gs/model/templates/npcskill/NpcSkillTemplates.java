/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.npcskill;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * @author AionChs Master
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "npcskills")
public class NpcSkillTemplates {

    @XmlAttribute(name = "npcid")
    protected int npcId;
    @XmlElement(name = "npcskill")
    protected List<NpcSkillTemplate> npcSkills;

    public int getNpcId() {
        return npcId;
    }

    public List<NpcSkillTemplate> getNpcSkills() {
        return npcSkills;
    }

}
