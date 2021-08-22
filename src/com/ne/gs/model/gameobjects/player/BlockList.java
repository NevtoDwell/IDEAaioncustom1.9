/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.google.common.collect.ImmutableMap;
import gnu.trove.map.hash.THashMap;

/**
 * Represents a players list of blocked users<br />
 * Blocks via a player's CommonData
 *
 * @author Ben
 */
public class BlockList implements Iterable<BlockedPlayer> {

    /**
     * The maximum number of users a block list can contain
     */
    public static final int MAX_BLOCKS = 10;

    // Indexes blocked players by their player ID
    private final Map<Integer, BlockedPlayer> blockedList;

    /**
     * Constructs a new (empty) blocked list
     */
    public BlockList() {
        this(new THashMap<Integer, BlockedPlayer>(0));
    }

    /**
     * Constructs a new blocked list with the given initial items
     *
     * @param initialList
     *     A map of blocked players indexed by their object IDs
     */
    public BlockList(Map<Integer, BlockedPlayer> initialList) {
        blockedList = new THashMap<>(initialList);

    }

    /**
     * Adds a player to the blocked users list<br />
     * <ul>
     * <li>Does not send packets or update the database</li>
     * </ul>
     *
     * @param plr
     *     The player to be blocked
     */
    public synchronized void add(BlockedPlayer plr) {
        blockedList.put(plr.getObjId(), plr);
    }

    /**
     * Removes a player from the blocked users list<br />
     * <ul>
     * <li>Does not send packets or update the database</li>
     * </ul>
     *
     * @param objIdOfPlayer
     */
    public synchronized void remove(int objIdOfPlayer) {
        blockedList.remove(objIdOfPlayer);
    }

    /**
     * Returns the blocked player with this name if they exist
     *
     * @param name
     *
     * @return CommonData of player with this name, null if not blocked
     */
    public synchronized BlockedPlayer getBlockedPlayer(String name) {
        for (BlockedPlayer entry : blockedList.values()) {
            if (entry.getName().equalsIgnoreCase(name)) {
                return entry;
            }
        }
        return null;
    }

    public synchronized BlockedPlayer getBlockedPlayer(int playerObjId) {
        return blockedList.get(playerObjId);
    }

    public synchronized boolean contains(int playerObjectId) {
        return blockedList.containsKey(playerObjectId);
    }

    /**
     * Returns the number of blocked players in this list
     *
     * @return blockedList.size()
     */
    public synchronized int getSize() {
        return blockedList.size();
    }

    public synchronized boolean isFull() {
        return getSize() >= MAX_BLOCKS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Iterator<BlockedPlayer> iterator() {
        return ImmutableMap.copyOf(blockedList).values().iterator();
    }

}
