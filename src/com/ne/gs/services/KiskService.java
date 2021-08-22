/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import java.util.Map;
import javolution.util.FastMap;

import com.ne.gs.model.gameobjects.Kisk;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_LEVEL_UPDATE;
import com.ne.gs.network.aion.serverpackets.SM_SET_BIND_POINT;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author Sarynth
 */
 public class KiskService {

    private static final KiskService instance = new KiskService();
    private final Map<Integer, Kisk> boundButOfflinePlayer = new FastMap<Integer, Kisk>().shared();
    private final Map<Integer, Kisk> ownerPlayer = new FastMap<Integer, Kisk>().shared();

    public void removeKisk(Kisk kisk) {

        for (int memberId : kisk.getCurrentMemberIds()) {
            boundButOfflinePlayer.remove(memberId);
        }

        for (Integer obj : ownerPlayer.keySet()) {
            if (ownerPlayer.get(obj).equals(kisk)) {
                ownerPlayer.remove(obj);
                break;
            }
        }

        /**
         * Remove kisk references and containers.
         *
         * @param kisk
         */
        for (Player member : kisk.getCurrentMemberList()) {

            if(member.getKisk() != kisk)
                continue;

            member.setKisk(null);
            member.sendPck(new SM_SET_BIND_POINT(0, 0f, 0f, 0f, member));
            if (member.getLifeStats().isAlreadyDead()) {
                member.getController().sendDie();
            }
        }
    }

    /**
     * @param kisk
     * @param player
     */
    public void onBind(Kisk kisk, Player player) {
        if (player.getKisk() != null) {
            player.getKisk().removePlayer(player);
        }

        kisk.addPlayer(player);

        // Send Bind Point Data
        TeleportService.sendSetBindPoint(player);

        // Send System Message
        player.sendPck(SM_SYSTEM_MESSAGE.STR_BINDSTONE_REGISTER);

        // Send Animated Bind Flash
        PacketSendUtility.broadcastPacket(player, new SM_LEVEL_UPDATE(player.getObjectId(), 2, player.getCommonData().getLevel()), true);
    }

    /**
     * @param player
     */
    public void onLogin(Player player) {
        Kisk kisk = boundButOfflinePlayer.get(player.getObjectId());
        if (kisk != null) {
            kisk.addPlayer(player);
            boundButOfflinePlayer.remove(player.getObjectId());
        }
    }

    public void onLogout(Player player) {
        Kisk kisk = player.getKisk();

        if (kisk != null) {
            boundButOfflinePlayer.put(player.getObjectId(), kisk);
        }
    }

    public void regKisk(Kisk kisk, Integer objOwnerId) {
        ownerPlayer.put(objOwnerId, kisk);
    }

    public boolean haveKisk(Integer objOwnerId) {
        return ownerPlayer.containsKey(objOwnerId);
    }

    public static KiskService getInstance() {
        return instance;
    }
}
