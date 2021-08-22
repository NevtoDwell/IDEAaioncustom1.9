/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.audit;

import com.ne.gs.configs.main.PunishmentConfig;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.BannedMacManager;
import com.ne.gs.network.aion.serverpackets.SM_QUIT_RESPONSE;
import com.ne.gs.network.loginserver.LoginServer;
import com.ne.gs.services.PunishmentService;

/**
 * @author synchro2
 */
public final class AutoBan {

    protected static void punishment(Player player, String message) {

        String reason = "AUTO " + message;
        String address = player.getClientConnection().getMacAddress();
        String accountIp = player.getClientConnection().getIP();
        int accountId = player.getClientConnection().getAccount().getId();
        int playerId = player.getObjectId();
        int time = PunishmentConfig.PUNISHMENT_TIME;
        int minInDay = 1440;
        int dayCount = (int) (Math.floor(time / minInDay));

        switch (PunishmentConfig.PUNISHMENT_TYPE) {
            case 1:
                player.getClientConnection().close(new SM_QUIT_RESPONSE(), false);
                break;
            case 2:
                PunishmentService.banChar(playerId, dayCount, reason);
                break;
            case 3:
                LoginServer.getInstance().sendBanPacket((byte) 1, accountId, accountIp, time, 0);
                break;
            case 4:
                LoginServer.getInstance().sendBanPacket((byte) 2, accountId, accountIp, time, 0);
                break;
            case 5:
                player.getClientConnection().closeNow();
                BannedMacManager.getInstance().banAddress(address, System.currentTimeMillis() + time * 60000, reason);
                break;
        }
    }
}
