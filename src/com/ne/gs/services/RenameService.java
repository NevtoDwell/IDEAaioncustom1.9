/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import java.util.Iterator;

import com.ne.gs.database.GDB;
import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.database.dao.LegionDAO;
import com.ne.gs.database.dao.OldNamesDAO;
import com.ne.gs.database.dao.PlayerDAO;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_RENAME;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.player.PlayerService;
import com.ne.gs.utils.audit.AuditLogger;
import com.ne.gs.world.World;

/**
 * @author ATracer modified cura
 */
public final class RenameService {

    public static boolean renamePlayer(Player player, String oldName, String newName, int item) {
        if (!NameRestrictionService.isValidName(newName)) {
            player.sendPck(new SM_SYSTEM_MESSAGE(1400151));
            return false;
        }
        if (NameRestrictionService.isForbiddenWord(newName)) {
            player.sendMsg("You are trying to use a forbidden name. Choose another one!");
            return false;
        }
        if (!PlayerService.isFreeName(newName)) {
            player.sendPck(new SM_SYSTEM_MESSAGE(1400155));
            return false;
        }
        if (player.getName().equals(newName)) {
            player.sendPck(new SM_SYSTEM_MESSAGE(1400153));
            return false;
        }
        if (!CustomConfig.OLD_NAMES_COUPON_DISABLED && PlayerService.isOldName(newName)) {
            player.sendPck(new SM_SYSTEM_MESSAGE(1400155));
            return false;
        }
        if ((player.getInventory().getItemByObjId(item).getItemId() != 169670000 && player.getInventory().getItemByObjId(item).getItemId() != 169670001)
            || (!player.getInventory().decreaseByObjectId(item, 1))) {
            AuditLogger.info(player, "Try    rename youself without coupon.");
            return false;
        }
        if (!CustomConfig.OLD_NAMES_COUPON_DISABLED) {
            GDB.get(OldNamesDAO.class).insertNames(player.getObjectId(), player.getName(), newName);
        }
        player.getCommonData().setName(newName);

        Iterator<Player> onlinePlayers = World.getInstance().getPlayersIterator();
        while (onlinePlayers.hasNext()) {
            Player p = onlinePlayers.next();
            if (p != null && p.getClientConnection() != null) {
                p.sendPck(new SM_RENAME(player.getObjectId(), oldName, newName));
            }
        }
        GDB.get(PlayerDAO.class).storePlayer(player);

        return true;
    }

    public static boolean renameLegion(Player player, String name, int item) {
        if (!player.isLegionMember()) {
            return false;
        }
        if (!LegionService.getInstance().isValidName(name)) {
            player.sendPck(new SM_SYSTEM_MESSAGE(1400152));
            return false;
        }
        if (NameRestrictionService.isForbiddenWord(name)) {
            player.sendMsg("You are trying to use a forbidden name. Choose another one!");
            return false;
        }
        if (GDB.get(LegionDAO.class).isNameUsed(name)) {
            player.sendPck(new SM_SYSTEM_MESSAGE(1400156));
            return false;
        }
        if (player.getLegion().getLegionName().equals(name)) {
            player.sendPck(new SM_SYSTEM_MESSAGE(1400154));
            return false;
        }
        if ((player.getInventory().getItemByObjId(item).getItemId() != 169680000 && player.getInventory().getItemByObjId(item).getItemId() != 169680001)
            || (!player.getInventory().decreaseByObjectId(item, 1))) {
            AuditLogger.info(player, "Try rename legion without coupon.");
            return false;
        }
        LegionService.getInstance().setLegionName(player.getLegion(), name, true);

        return true;
    }
}
