/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.conds;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.utils.SimpleCond;
import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.KiskService;

/**
 * @author hex1r0
 */
public abstract class SpawnObjCond extends SimpleCond<SpawnObjCond.Env> {

    public static final SpawnObjCond STATIC = new SpawnObjCond() {
        @Override
        public Boolean onEvent(@NotNull Env env) {
            if (env.player.getFlyState() != 0) {
                env.player.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_USE_BINDSTONE_ITEM_WHILE_FLYING);
                return false;
            }
            if (env.player.isInInstance()) {
                env.player.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_REGISTER_BINDSTONE_FAR_FROM_NPC);
                return false;
            }
            if (!CustomConfig.TOYPETSPAWN_NEW_KISK_SPAWN_ENABLE && KiskService.getInstance().haveKisk(env.player.getObjectId())) {
                env.player.sendPck(new SM_SYSTEM_MESSAGE(1390160));
                return false;
            }
            // TODO Kisk zone reparse
        /*
         * if (player.getWorldType() == WorldType.BALAUREA || player.getWorldType() == WorldType.ABYSS) if
		 * (player.isInsideZoneType(ZoneType.SIEGE)) { PacketSendUtility.sendPacket(player,
		 * SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_LOCATION); return false; }
		 */
            switch (env.player.getWorldId()) {
                case 110010000:
                case 120010000:
                case 110020000:
                case 120020000:
                case 600010000:
                case 600040000:
                    env.player.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_LOCATION);
                    return false;
                default:
                    break;
            }

            return true;
        }
    };

    @NotNull
    @Override
    public final String getType() {
        return SpawnObjCond.class.getName();
    }

    public static class Env {

        public final Player player;
        public final Item parentItem;
        public final Item targetItem;

        public Env(Player player, Item parentItem, Item targetItem) {
            this.player = player;
            this.parentItem = parentItem;
            this.targetItem = targetItem;
        }
    }
}
