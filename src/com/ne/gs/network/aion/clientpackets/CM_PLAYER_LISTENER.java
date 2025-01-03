/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.services.custom.OnlineBonusService;
import com.ne.gs.services.reward.RewardService;

public class CM_PLAYER_LISTENER extends AionClientPacket {

    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        if (CustomConfig.ENABLE_REWARD_SERVICE) {
            RewardService.getInstance().verify(player);
        }
        if(CustomConfig.ONLINE_BONUSES_ENABLED) {
            OnlineBonusService.getInstance().checkPlayer(player);        	
        }
    }
}
