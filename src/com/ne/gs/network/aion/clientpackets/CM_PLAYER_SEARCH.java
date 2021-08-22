/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.model.gameobjects.player.FriendList.Status;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_PLAYER_SEARCH;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.world.World;

/**
 * Received when a player searches using the social search panel
 *
 * @author Ben
 */
public class CM_PLAYER_SEARCH extends AionClientPacket {

    /**
     * The max number of players to return as results
     */
    public static final int MAX_RESULTS = 104; // 3.0

    private String name;
    private int region;
    private int classMask;
    private int minLevel;
    private int maxLevel;
    private int lfgOnly;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        if (!(name = readS()).isEmpty()) {
            readB(52 - (name.length() * 2 + 2));
        } else {
            readB(50);
        }
        region = readD();
        classMask = readD();
        minLevel = readC();
        maxLevel = readC();
        lfgOnly = readC();
        readC(); // 0x00 in search pane 0x30 in /who?
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player activePlayer = getConnection().getActivePlayer();

        Iterator<Player> it = World.getInstance().getPlayersIterator();

        List<Player> matches = new ArrayList<>(MAX_RESULTS);

        if (activePlayer.getLevel() < CustomConfig.LEVEL_TO_SEARCH) {
            sendPacket(SM_SYSTEM_MESSAGE.STR_CANT_WHO_LEVEL(String.valueOf(CustomConfig.LEVEL_TO_SEARCH)));
            return;
        }
        while (it.hasNext() && matches.size() < MAX_RESULTS) {
            Player player = it.next();
            if (!player.isSpawned()) {
                continue;
            } else if (player.getFriendList().getStatus() == Status.OFFLINE) {
                continue;
            } else if (player.isGM() && !CustomConfig.SEARCH_GM_LIST) {
                continue;
            } else if (lfgOnly == 1 && !player.isLookingForGroup()) {
                continue;
            } else if (!name.isEmpty() && !player.getName().toLowerCase().contains(name.toLowerCase())) {
                continue;
            } else if (minLevel != 0xFF && player.getLevel() < minLevel) {
                continue;
            } else if (maxLevel != 0xFF && player.getLevel() > maxLevel) {
                continue;
            } else if (classMask > 0 && (player.getPlayerClass().getMask() & classMask) == 0) {
                continue;
            } else if (region > 0 && player.getActiveRegion().getMapId() != region) {
                continue;
            } else if ((player.getRace() != activePlayer.getRace()) && (CustomConfig.FACTIONS_SEARCH_MODE == false)) {
                continue;
            } else {
                matches.add(player);
            }
        }

        sendPacket(new SM_PLAYER_SEARCH(matches, region));
    }

}
