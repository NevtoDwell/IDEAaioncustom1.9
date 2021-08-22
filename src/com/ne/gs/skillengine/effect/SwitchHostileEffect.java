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

import com.ne.gs.controllers.attack.AggroList;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.skillengine.model.Effect;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SwitchHostileEffect")
public class SwitchHostileEffect extends EffectTemplate {

    @Override
    public void applyEffect(Effect effect) {

        Creature effected = effect.getEffected();
        Creature effector = effect.getEffector();
        AggroList aggroList = effected.getAggroList();

        if (((Player) effector).getSummon() != null) {
            Creature summon = ((Player) effector).getSummon();
            int playerHate = aggroList.getAggroInfo(effector).getHate();           
            //lonefoxx
            aggroList.stopHating(effector);
            aggroList.addHate(summon, playerHate);     
        }
    }   
}
