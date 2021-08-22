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
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.skillengine.model.Skill;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrowCheckCondition")
public class ArrowCheckCondition extends Condition {

    @Override
    public boolean validate(Skill skill) {
        if (skill.getEffector() instanceof Player) {
            Player player = (Player) skill.getEffector();
            if (player.getEquipment().isArrowEquipped()) {
                return true;
            }

            player.sendPck(SM_SYSTEM_MESSAGE.STR_CANT_ATTACK_NO_ARROW);
            return false;
        } else {
            return true;
        }
    }
}
