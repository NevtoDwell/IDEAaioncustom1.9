/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.ai;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BombTemplate")
public class BombTemplate {

    @XmlAttribute(name = "skillId")
    private int SkillId = 0;
    @XmlAttribute(name = "cd")
    private int cd = 0;

    public int getCd() {
        return cd;
    }

    public int getSkillId() {
        return SkillId;
    }
}
