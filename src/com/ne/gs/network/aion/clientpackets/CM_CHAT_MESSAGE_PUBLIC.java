/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.commons.func.Filter;
import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.configs.main.LoggingConfig;
import com.ne.gs.model.ChatType;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_MESSAGE;
import com.ne.gs.restrictions.RestrictionsManager;
import com.ne.gs.services.NameRestrictionService;
import com.ne.gs.services.player.PlayerChatService;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.audit.AuditLogger;
import com.ne.gs.utils.chathandlers.ChatProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Packet that reads normal chat messages.<br>
 *
 * @author SoulKeeper
 */
public class CM_CHAT_MESSAGE_PUBLIC extends AionClientPacket {

    private static final Logger log = LoggerFactory.getLogger(AionClientPacket.class);
    /**
     * Chat type
     */
    private ChatType type;

    /**
     * Chat message
     */
    private String message;

    @Override
    protected void readImpl() {
        type = ChatType.getChatTypeByInt(readC());
        message = readS();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        
        message = message.replaceAll("\\s+", " ");
        //log.info(message);
        // TODO rework access level(byte) to more flexible implementation
        if (ChatProcessor.getInstance().process(player, message)) {
            return;
        }

        message = NameRestrictionService.filterMessage(message);

        if (LoggingConfig.LOG_CHAT) {
            PlayerChatService.chatLogging(player, type, message);
        }

        if (RestrictionsManager.canChat(player) && !PlayerChatService.isFlooding(player)) {
            switch (type) {
                case GROUP:
                    if (!player.isInTeam()) {
                        return;
                    }
                    broadcastToGroupMembers(player);
                    break;
                case ALLIANCE:
                    if (!player.isInAlliance2()) {
                        return;
                    }
                    broadcastToAllianceMembers(player);
                    break;
                case GROUP_LEADER:
                    if (!player.isInTeam()) {
                        return;
                    }
                    // Alert must go to entire group or alliance.
                    if (player.isInGroup2()) {
                        broadcastToGroupMembers(player);
                    } else {
                        broadcastToAllianceMembers(player);
                    }
                    break;
                case LEGION:
                    broadcastToLegionMembers(player);
                    break;
                case LEAGUE:
                case LEAGUE_ALERT:
                    if (!player.isInLeague()) {
                        return;
                    }
                    broadcastToLeagueMembers(player);
                    break;
                case NORMAL:
                case SHOUT:
                    if (player.isGM()) {
                        broadcastFromGm(player);
                    } else if (CustomConfig.SPEAKING_BETWEEN_FACTIONS) {
                        broadcastToNonBlockedPlayers(player);
                    } else {
                        broadcastToNonBlockedRacePlayers(player);
                    }
                    break;
                default:
                    if (player.isGM()) {
                        broadcastFromGm(player);
                    } else {
                        AuditLogger.info(player, String.format("Send message type %s. Message: %s", type, message));
                    }
                    break;
            }
        } else {
            player.sendMsg("You are gaged, you can't talk.");
        }
    }

    /**
     * Sends message to all players from admin
     *
     * @param player
     */
    private void broadcastFromGm(Player player) {
        PacketSendUtility.broadcastPacket(player, new SM_MESSAGE(player, message, type), true);
    }

    /**
     * Sends message to all players that are not in blocklist
     *
     * @param player
     */
    private void broadcastToNonBlockedPlayers(final Player player) {
        PacketSendUtility.broadcastPacket(player, new SM_MESSAGE(player, message, type), true, new Filter<Player>() {
            @Override
            public boolean accept(Player object) {
                return true;
                //return object.isGM() || !object.getBlockList().contains(player.getObjectId());
            }
        });
    }

    /**
     * Sends message to races members (other race will receive an unknown message)
     *
     * @param player
     */
    private void broadcastToNonBlockedRacePlayers(final Player player) {
        final int senderRace = player.getRace().getRaceId();
        PacketSendUtility.broadcastPacket(player, new SM_MESSAGE(player, message, type), true, new Filter<Player>() {
            @Override
            public boolean accept(Player object) {
                return object.isGM() || senderRace == object.getRace().getRaceId() /*&& !object.getBlockList().contains(player.getObjectId())*/;
            }

        });
        PacketSendUtility.broadcastPacket(player, new SM_MESSAGE(player, "Unknow Message", type), false, new Filter<Player>() {
            @Override
            public boolean accept(Player object) {
                return senderRace != object.getRace().getRaceId() && !object.getBlockList().contains(player.getObjectId()) && !object.isGM();
            }
        });
    }

    /**
     * Sends message to all group members (regular player group, or alliance sub-group Error 105, random value for players to report. Should never happen.
     *
     * @param player
     */
    private void broadcastToGroupMembers(Player player) {
        if (player.isInTeam()) {
            player.getCurrentGroup().sendPacket(new SM_MESSAGE(player, message, type));
        } else {
            player.sendMsg("You are not in an alliance or group. (Error 105)");
        }
    }

    /**
     * Sends message to all alliance members
     *
     * @param player
     */
    private void broadcastToAllianceMembers(Player player) {
        player.getPlayerAlliance2().sendPacket(new SM_MESSAGE(player, message, type));
    }

    /**
     * Sends message to all league members
     *
     * @param player
     */
    private void broadcastToLeagueMembers(Player player) {
        player.getPlayerAlliance2().getLeague().sendPacket(new SM_MESSAGE(player, message, type));
    }

    /**
     * Sends message to all legion members
     *
     * @param player
     */
    private void broadcastToLegionMembers(Player player) {
        if (player.isLegionMember()) {
            PacketSendUtility.broadcastPacketToLegion(player.getLegion(), new SM_MESSAGE(player, message, type));
        }
    }

}
