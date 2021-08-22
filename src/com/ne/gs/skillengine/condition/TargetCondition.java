/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.skillengine.properties.FirstTargetAttribute;
import com.ne.gs.skillengine.properties.TargetRangeAttribute;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TargetCondition")
public class TargetCondition extends Condition {

    @XmlAttribute(required = true)
    protected TargetAttribute value;

    /**
     * Gets the value of the value property.
     *
     * @return possible object is {@link TargetAttribute }
     */
    public TargetAttribute getValue() {
        return value;
    }

    @Override
    public boolean validate(Skill skill) {
        if ((value == TargetAttribute.NONE) || (value == TargetAttribute.ALL)) {
            return true;
        }
        if (skill.getSkillTemplate().getProperties().getTargetType().equals(TargetRangeAttribute.AREA)) {
            return true;
        }
        if ((skill.getSkillTemplate().getProperties().getFirstTarget() != FirstTargetAttribute.TARGET) && (skill.getSkillTemplate().getProperties()
            .getFirstTarget() != FirstTargetAttribute.TARGETORME)) {
            return true;
        }
        if ((skill.getSkillTemplate().getProperties().getFirstTarget() == FirstTargetAttribute.TARGETORME) && (skill.getEffector() == skill.getFirstTarget())) {
            return true;
        }
        boolean result = false;
        switch (value) {
            case NPC:
                result = skill.getFirstTarget() instanceof Npc;
                break;
            case PC:
                result = skill.getFirstTarget() instanceof Player;
        }

        if ((!result) && ((skill.getEffector() instanceof Player))) {
            ((Player) skill.getEffector()).sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID);
        }
        return result;
    }
}
