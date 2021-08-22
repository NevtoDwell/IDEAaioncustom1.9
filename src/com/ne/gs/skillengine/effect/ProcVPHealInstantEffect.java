/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.effect;

import javax.xml.bind.annotation.XmlAttribute;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.network.aion.serverpackets.SM_STATUPDATE_EXP;
import com.ne.gs.skillengine.model.Effect;

public class ProcVPHealInstantEffect extends EffectTemplate {

    @XmlAttribute(required = true)
    protected int value2;

    @XmlAttribute
    protected boolean percent;

    public void applyEffect(Effect effect) {
        if ((effect.getEffected() instanceof Player)) {
            Player player = (Player) effect.getEffected();
            PlayerCommonData pcd = player.getCommonData();

            long cap = pcd.getMaxReposteEnergy() * value2 / 100L;

            if (pcd.getCurrentReposteEnergy() < cap) {
                int valueWithDelta = value + delta * effect.getSkillLevel();
                long addEnergy = 0L;
                if (percent) {
                    addEnergy = (int) (pcd.getMaxReposteEnergy() * valueWithDelta * 0.001D);
                } else {
                    addEnergy = valueWithDelta;
                }
                pcd.addReposteEnergy(addEnergy);
                player.sendPck(new SM_STATUPDATE_EXP(pcd.getExpShown(), pcd.getExpRecoverable(), pcd.getExpNeed(), pcd.getCurrentReposteEnergy(), pcd.getMaxReposteEnergy()));
            }
        }
    }
}
