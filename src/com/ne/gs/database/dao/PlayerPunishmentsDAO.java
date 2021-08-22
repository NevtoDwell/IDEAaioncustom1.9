/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.dao;

import com.ne.commons.database.dao.DAO;
import com.ne.gs.model.account.CharacterBanInfo;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.services.PunishmentService.PunishmentType;

/**
 * @author lord_rex
 */
public abstract class PlayerPunishmentsDAO implements DAO {

    @Override
    public final String getClassName() {
        return PlayerPunishmentsDAO.class.getName();
    }

    public abstract void loadPlayerPunishments(Player player, PunishmentType punishmentType);

    public abstract void storePlayerPunishments(Player player, PunishmentType punishmentType);

    public abstract void punishPlayer(int playerId, PunishmentType punishmentType, long expireTime, String reason);

    public abstract void punishPlayer(Player player, PunishmentType punishmentType, String reason);

    public abstract void unpunishPlayer(int playerId, PunishmentType punishmentType);

    public abstract CharacterBanInfo getCharBanInfo(int playerId);
}
