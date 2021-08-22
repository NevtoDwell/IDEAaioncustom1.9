/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_PLAYER_STANCE;

/**
 * @author ATracer
 */
public class CM_TOGGLE_SKILL_DEACTIVATE extends AionClientPacket {

    private int skillId;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        skillId = readH();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        player.getEffectController().removeNoshowEffect(skillId);

        if (player.getController().getStanceSkillId() == skillId) {
            player.sendPck(new SM_PLAYER_STANCE(player, 0));
            player.getController().startStance(0);
        }
    }
}
