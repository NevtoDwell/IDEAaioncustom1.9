/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils;

import com.ne.commons.func.Filter;
import com.ne.gs.model.ChatType;
import com.ne.gs.model.gameobjects.AionObject;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team.legion.Legion;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.serverpackets.SM_MESSAGE;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.Visitor;
import com.ne.gs.world.zone.SiegeZoneInstance;

/**
 * This class contains static methods, which are utility methods, all of them are interacting only with objects passed as parameters.<br>
 * These methods could be placed directly into Player class, but we want to keep Player class as a pure data holder.<br>
 *
 * @author Luno
 */
public final class PacketSendUtility {

    public static void sendWhiteMessage(Player player, String msg) {
        player.sendPck(new SM_MESSAGE(0, null, msg, ChatType.WHITE));
    }

    public static void sendWhiteMessageOnCenter(Player player, String msg) {
        player.sendPck(new SM_MESSAGE(0, null, msg, ChatType.WHITE_CENTER));
    }

    public static void sendYellowMessage(Player player, String msg) {
        player.sendPck(new SM_MESSAGE(0, null, msg, ChatType.YELLOW));
    }

    public static void sendYellowMessageOnCenter(Player player, String msg) {
        player.sendPck(new SM_MESSAGE(0, null, msg, ChatType.YELLOW_CENTER));
    }

    public static void sendBrightYellowMessage(Player player, String msg) {
        player.sendPck(new SM_MESSAGE(0, null, msg, ChatType.BRIGHT_YELLOW));
    }

    public static void sendBrightYellowMessageOnCenter(Player player, String msg) {
        player.sendPck(new SM_MESSAGE(0, null, msg, ChatType.BRIGHT_YELLOW_CENTER));
    }

    public static void sendMessage(Player player, String sender, String msg, ChatType chatType) {
        player.sendPck(new SM_MESSAGE(0, sender, msg, chatType));
    }

    public static void sendMessage(Player player, String msg) {
        player.sendMsg(msg);
    }

    public static void sendPck(AionObject obj, AionServerPacket pck) {
        if (obj instanceof Player) {
            ((Player) obj).sendPck(pck);
        }
    }

    /**
     * Broadcast packet to all visible players.
     *
     * @param packet
     *     ServerPacket that will be broadcast
     * @param toSelf
     *     true if packet should also be sent to this player
     */
    public static void broadcastPacket(Player player, AionServerPacket packet, boolean toSelf) {
        if (toSelf) {
            player.sendPck(packet);
        }

        broadcastPacket(player, packet);
    }

    /**
     * Broadcast packet to all visible players.
     */
    public static void broadcastPacketAndReceive(VisibleObject visibleObject, AionServerPacket packet) {
        if (visibleObject instanceof Player) {
            ((Player) visibleObject).sendPck(packet);
        }

        broadcastPacket(visibleObject, packet);
    }

    /**
     * Broadcast packet to all Players from knownList of the given visible object.
     */
    public static void broadcastPacket(VisibleObject visibleObject, final AionServerPacket packet) {
        visibleObject.getKnownList().doOnAllPlayers(new Visitor<Player>() {

            @Override
            public void visit(Player player) {
                if (player.isOnline()) {
                    player.sendPck(packet);
                }
            }
        });
    }


    /**
     * Broadcasts packet to all visible players matching a filter
     *
     * @param packet
     *     ServerPacket to be broadcast
     * @param toSelf
     *     true if packet should also be sent to this player
     * @param filter
     *     filter determining who should be messaged
     */
    public static void broadcastPacket(Player player, final AionServerPacket packet, boolean toSelf, final Filter<Player> filter) {
        if (toSelf) {
            player.sendPck(packet);
        }

        player.getKnownList().doOnAllPlayers(new Visitor<Player>() {
            @Override
            public void visit(Player object) {
                if (filter.accept(object)) {
                    object.sendPck(packet);
                }
            }
        });
    }

    /**
     * Broadcasts packet to all Players from knownList of the given visible object within the specified distance in meters
     */
    public static void broadcastPacket(final VisibleObject visibleObject, final AionServerPacket packet, final int distance) {
        visibleObject.getKnownList().doOnAllPlayers(new Visitor<Player>() {
            @Override
            public void visit(Player p) {
                if (MathUtil.isIn3dRange(visibleObject, p, distance)) {
                    p.sendPck(packet);
                }
            }
        });
    }

    /**
     * Broadcasts packet to ALL players matching a filter
     *
     * @param packet
     *     ServerPacket to be broadcast
     * @param filter
     *     filter determining who should be messaged
     */
    public static void broadcastFilteredPacket(final AionServerPacket packet, final Filter<Player> filter) {
        World.getInstance().doOnAllPlayers(new Visitor<Player>() {
            @Override
            public void visit(Player object) {
                if (filter.accept(object)) {
                    object.sendPck(packet);
                }
            }
        });
    }

    /**
     * Broadcasts packet to all legion members of a legion
     *
     * @param legion
     *     Legion to broadcast packet to
     * @param packet
     *     ServerPacket to be broadcast
     */
    public static void broadcastPacketToLegion(Legion legion, AionServerPacket packet) {
        for (Player onlineLegionMember : legion.getOnlineLegionMembers()) {
            onlineLegionMember.sendPck(packet);
        }
    }

    public static void broadcastPacketToLegion(Legion legion, AionServerPacket packet, int playerObjId) {
        for (Player onlineLegionMember : legion.getOnlineLegionMembers()) {
            if (onlineLegionMember.getObjectId() != playerObjId) {
                onlineLegionMember.sendPck(packet);
            }
        }
    }

    public static void broadcastPacketToZone(SiegeZoneInstance zone, final AionServerPacket packet) {
        zone.doOnAllPlayers(new Visitor<Player>() {
            @Override
            public void visit(Player player) {
                player.sendPck(packet);

            }
        });
    }
}
