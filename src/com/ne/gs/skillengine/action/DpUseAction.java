/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.action;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.skillengine.model.Skill;

/**
 * @author ATracer Effector: Player only
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DpUseAction")
public class DpUseAction extends Action {

    @XmlAttribute(required = true)
    protected int value;

    @Override
    public void act(Skill skill) {
        Player effector = (Player) skill.getEffector();
        int currentDp = effector.getLifeStats().getCurrentDp();

        if (currentDp <= 0 || currentDp < value) {
            return;
        }

        effector.getLifeStats().setCurrentDp(currentDp - value);
    }
}
