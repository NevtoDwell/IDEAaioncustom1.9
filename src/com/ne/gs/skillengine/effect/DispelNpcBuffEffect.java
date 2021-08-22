/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.skillengine.model.DispelCategoryType;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.SkillTargetSlot;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DispelNpcBuffEffect")
public class DispelNpcBuffEffect extends AbstractDispelEffect {

    public void applyEffect(Effect effect) {
        super.applyEffect(effect, DispelCategoryType.NPC_BUFF, SkillTargetSlot.BUFF);
    }
}
