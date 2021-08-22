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

/**
 * @author vlog
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestSkillData")
public class QuestSkillData {

    @XmlAttribute(name = "id", required = true)
    protected int skillId;
    @XmlAttribute(name = "start_var")
    protected int startVar = 0;
    @XmlAttribute(name = "end_var", required = true)
    protected int endVar;
    @XmlAttribute(name = "var_num")
    protected int varNum = 0;

    public int getSkillId() {
        return skillId;
    }

    public int getVarNum() {
        return varNum;
    }

    public int getStartVar() {
        return startVar;
    }

    public int getEndVar() {
        return endVar;
    }
}
