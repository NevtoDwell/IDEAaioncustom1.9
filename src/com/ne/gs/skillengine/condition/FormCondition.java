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

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.skillengine.model.TransformType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FormCondition")
public class FormCondition extends Condition {

    @XmlAttribute(required = true)
    protected TransformType value;

    public boolean validate(Skill env) {
        if ((env.getEffector() instanceof Player)) {
            if ((env.getEffector().getTransformModel().isActive()) && (env.getEffector().getTransformModel().getType() == value)) {
                return true;
            }
            ((Player) env.getEffector()).sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_CAST_IN_THIS_FORM);
            return false;
        }

        return true;
    }
}
