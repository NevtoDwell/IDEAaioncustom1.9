/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.abyss;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple4;
import com.ne.commons.utils.EventNotifier;
import com.ne.commons.utils.TypedCallback;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.AbyssRank;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.siege.SiegeNpc;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.serverpackets.SM_ABYSS_RANK;
import com.ne.gs.network.aion.serverpackets.SM_ABYSS_RANK_UPDATE;
import com.ne.gs.network.aion.serverpackets.SM_LEGION_EDIT;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.stats.AbyssRankEnum;

/**
 * @author ATracer
 */
public final class AbyssPointsService {

    public static void addAp(Player player, VisibleObject obj, int value, Class rewarder) {
        addAp(player, value);
        EventNotifier.GLOBAL.fire(ApAddCallback.class, Tuple4.of(player, obj, value, rewarder));
    }

    public static void addAp(Player player, int value) {
        if (player == null) {
            return;
        }

        // Notify player of AP gained (This should happen before setAp happens.)
        if (value > 0) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_MY_ABYSS_POINT_GAIN(value));
        } else {
            // You used %num0 Abyss Points.
            AionServerPacket packet = new SM_SYSTEM_MESSAGE(1300965, value * -1);
            player.sendPck(packet);
        }

        // Set the new AP value
        setAp(player, value);

        // Add Abyss Points to Legion
        if (player.isLegionMember() && value > 0) {
            player.getLegion().addContributionPoints(value);
            PacketSendUtility.broadcastPacketToLegion(player.getLegion(), new SM_LEGION_EDIT(0x03, player.getLegion()));
        }
    }

    /**
     * @param player
     * @param value
     */
    public static void setAp(Player player, int value) {
        if (player == null) {
            return;
        }

        AbyssRank rank = player.getAbyssRank();

        AbyssRankEnum oldAbyssRank = rank.getRank();
        rank.addAp(value);
        AbyssRankEnum newAbyssRank = rank.getRank();

        checkRankChanged(player, oldAbyssRank, newAbyssRank);

        player.sendPck(new SM_ABYSS_RANK(player.getAbyssRank()));
    }

    /**
     * @param player
     * @param oldAbyssRank
     * @param newAbyssRank
     */
    public static void checkRankChanged(Player player, AbyssRankEnum oldAbyssRank, AbyssRankEnum newAbyssRank) {
        if (oldAbyssRank == newAbyssRank) {
            return;
        }

        PacketSendUtility.broadcastPacketAndReceive(player, new SM_ABYSS_RANK_UPDATE(0, player));

        player.getEquipment().checkRankLimitItems();
        AbyssSkillService.updateSkills(player);
    }

    public static abstract class ApAddCallback implements TypedCallback<Tuple4<Player, VisibleObject, Integer, Class>, Object> {
        @NotNull
        @Override
        public final String getType() {
            return ApAddCallback.class.getName();
        }

        /**
         *
         * @param e
         *
         * @return
         */
        @Override
        public final Object onEvent(@NotNull Tuple4<Player, VisibleObject, Integer, Class> e) {
            if ((e._2 instanceof Player)) {
                onApAdd(e._1, e._2, e._3, e._4);
            } else if (((e._2 instanceof SiegeNpc)) && (!((SiegeNpc) e._2).getSpawn().isPeace())) {
                onApAdd(e._1, e._2, e._3, e._4);
            }
            return null;
        }

        public abstract void onApAdd(Player player, VisibleObject vo, int points, Class rewarder);
    }
}
