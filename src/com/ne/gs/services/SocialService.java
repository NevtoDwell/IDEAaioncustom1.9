/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import com.ne.gs.database.GDB;
import com.ne.gs.database.dao.BlockListDAO;
import com.ne.gs.database.dao.FriendListDAO;
import com.ne.gs.database.dao.PlayerDAO;
import com.ne.gs.model.gameobjects.player.BlockedPlayer;
import com.ne.gs.model.gameobjects.player.Friend;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_BLOCK_LIST;
import com.ne.gs.network.aion.serverpackets.SM_BLOCK_RESPONSE;
import com.ne.gs.network.aion.serverpackets.SM_FRIEND_LIST;
import com.ne.gs.network.aion.serverpackets.SM_FRIEND_NOTIFY;
import com.ne.gs.network.aion.serverpackets.SM_FRIEND_RESPONSE;
import com.ne.gs.world.World;

/**
 * Handles activities related to social groups ingame such as the buddy list, legions, etc
 *
 * @author Ben
 */
public final class SocialService {

    /**
     * Blocks the given object ID for the given player.<br />
     * <ul>
     * <li>Does not send packets</li>
     * </ul>
     *
     * @param player
     * @param blockedPlayer
     * @param reason
     *
     * @return Success
     */
    public static boolean addBlockedUser(Player player, Player blockedPlayer, String reason) {
        if (GDB.get(BlockListDAO.class).addBlockedUser(player.getObjectId(), blockedPlayer.getObjectId(), reason)) {
            player.getBlockList().add(new BlockedPlayer(blockedPlayer.getCommonData(), reason));

            player.getClientConnection().sendPacket(new SM_BLOCK_RESPONSE(SM_BLOCK_RESPONSE.BLOCK_SUCCESSFUL, blockedPlayer.getName()));
            player.getClientConnection().sendPacket(new SM_BLOCK_LIST());

            return true;
        }
        return false;
    }

    /**
     * Unblocks the given object ID for the given player.<br />
     * <ul>
     * <li>Does not send packets</li>
     * </ul>
     *
     * @param player
     * @param blockedUserId
     *     ID of player to unblock
     *
     * @return Success
     */
    public static boolean deleteBlockedUser(Player player, int blockedUserId) {
        if (GDB.get(BlockListDAO.class).delBlockedUser(player.getObjectId(), blockedUserId)) {
            player.getBlockList().remove(blockedUserId);
            player.getClientConnection().sendPacket(
                new SM_BLOCK_RESPONSE(SM_BLOCK_RESPONSE.UNBLOCK_SUCCESSFUL, GDB.get(PlayerDAO.class).loadPlayerCommonData(blockedUserId)
                    .getName()));

            player.getClientConnection().sendPacket(new SM_BLOCK_LIST());
            return true;
        }
        return false;
    }

    /**
     * Sets the reason for blocking a user
     *
     * @param player
     *     Player whos block list is to be edited
     * @param target
     *     Whom to block
     * @param reason
     *     Reason to set
     *
     * @return Success - May be false if the reason was the same and therefore not edited
     */
    public static boolean setBlockedReason(Player player, BlockedPlayer target, String reason) {

        if (!target.getReason().equals(reason)) {
            if (GDB.get(BlockListDAO.class).setReason(player.getObjectId(), target.getObjId(), reason)) {
                target.setReason(reason);
                player.getClientConnection().sendPacket(new SM_BLOCK_LIST());
                return true;
            }
        }
        return false;
    }

    /**
     * Adds two players to each others friend lists, and updates the database<br />
     *
     * @param friend1
     * @param friend2
     */
    public static void makeFriends(Player friend1, Player friend2) {
        GDB.get(FriendListDAO.class).addFriends(friend1, friend2);

        friend1.getFriendList().addFriend(new Friend(friend2.getCommonData()));
        friend2.getFriendList().addFriend(new Friend(friend1.getCommonData()));

        friend1.getClientConnection().sendPacket(new SM_FRIEND_LIST());
        friend2.getClientConnection().sendPacket(new SM_FRIEND_LIST());

        friend1.getClientConnection().sendPacket(new SM_FRIEND_RESPONSE(friend2.getName(), SM_FRIEND_RESPONSE.TARGET_ADDED));
        friend2.getClientConnection().sendPacket(new SM_FRIEND_RESPONSE(friend1.getName(), SM_FRIEND_RESPONSE.TARGET_ADDED));
    }

    /**
     * Deletes two players from eachother's friend lists, and updates the database
     * <ul>
     * <li>Note: Does not send notification packets, and does not send new list packet
     * </ul>
     * </li>
     *
     * @param deleter
     *     Player deleting a friend
     * @param exFriend2Id
     *     Object ID of the friend he is deleting
     */
    public static void deleteFriend(Player deleter, int exFriend2Id) {

        // If the DAO is successful
        if (GDB.get(FriendListDAO.class).delFriends(deleter.getObjectId(), exFriend2Id)) {
            Player friend2Player = World.getInstance().findPlayer(exFriend2Id);
            String friend2Name = friend2Player != null ? friend2Player.getName() : GDB.get(PlayerDAO.class).loadPlayerCommonData(exFriend2Id).getName();

            // Delete from deleter's friend list and send packets
            deleter.getFriendList().delFriend(exFriend2Id);

            deleter.getClientConnection().sendPacket(new SM_FRIEND_LIST());
            deleter.getClientConnection().sendPacket(new SM_FRIEND_RESPONSE(friend2Name, SM_FRIEND_RESPONSE.TARGET_REMOVED));

            if (friend2Player != null) {
                friend2Player.getFriendList().delFriend(deleter.getObjectId());

                if (friend2Player.isOnline()) {
                    friend2Player.getClientConnection().sendPacket(new SM_FRIEND_NOTIFY(SM_FRIEND_NOTIFY.DELETED, deleter.getName()));
                    friend2Player.getClientConnection().sendPacket(new SM_FRIEND_LIST());
                }
            }
        }

    }
}
