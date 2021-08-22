/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.audit;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.configs.main.LoggingConfig;
import com.ne.gs.configs.main.PunishmentConfig;
import com.ne.gs.configs.main.SecurityConfig;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author MrPoke
 */
public final class AuditLogger {

    private static final Logger log = LoggerFactory.getLogger("AUDIT_LOG");

    public static void info(Player player, String message) {
        Preconditions.checkNotNull(player, "Player should not be null or use different info method");
        if (LoggingConfig.LOG_AUDIT) {
            info(player.getName(), player.getObjectId(), message);
        }
        if (PunishmentConfig.PUNISHMENT_ENABLE) {
            AutoBan.punishment(player, message);
        }
    }

    public static void info(String playerName, int objectId, String message) {
        message += " Player name: " + playerName + " objectId: " + objectId;
        log.info(message);

        if (SecurityConfig.GM_AUDIT_MESSAGE_BROADCAST) {
            GMService.getInstance().broadcastMesage(message);
        }
    }
}
