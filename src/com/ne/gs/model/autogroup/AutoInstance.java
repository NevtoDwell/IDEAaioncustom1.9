/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.autogroup;

import java.util.List;
import javolution.util.FastList;

import com.ne.gs.model.PlayerClass;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.instance.instancereward.InstanceReward;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.model.team2.group.PlayerGroupService;
import com.ne.gs.world.WorldMapInstance;

import static ch.lambdaj.Lambda.*;
import com.ne.gs.configs.main.CustomConfig;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author xTz
 */
public class AutoInstance {

    private final FastList<Player> players = new FastList<>();
    private final FastList<Player> playersInside = new FastList<>();
    private final Race race;
    private final byte instanceMaskId;
    private WorldMapInstance worldMapInstance;
    private final long startInstanceTime;
    private final AutoGroupsType agt;
    private final EntryRequestType registredErt;

    public AutoInstance(Race race, byte instanceMaskId, WorldMapInstance worldMapInstance, EntryRequestType registredErt) {
        this.race = race;
        this.instanceMaskId = instanceMaskId;
        this.worldMapInstance = worldMapInstance;
        startInstanceTime = System.currentTimeMillis();
        agt = AutoGroupsType.getAutoGroupByInstanceMaskId(instanceMaskId);
        this.registredErt = registredErt;
    }

    public int getPlayerSize() {
        return players.size();
    }

    public Race getRace() {
        return race;
    }

    public byte getInstanceMaskId() {
        return instanceMaskId;
    }

    public void setWorldMapInstance(WorldMapInstance worldMapInstance) {
        this.worldMapInstance = worldMapInstance;
    }

    public synchronized void enterToGroup(Player player) {

        List<Player> playersByRace = getPlayersInsideByRace(player.getRace());
        if (playersByRace.size() == 1 && !playersByRace.get(0).isInGroup2()) {
            PlayerGroup newGroup = PlayerGroupService.createGroup(playersByRace.get(0), player);
            newGroup.setGroupType(0x02);
            int groupId = newGroup.getObjectId();
            if (!worldMapInstance.isRegistered(groupId)) {
                worldMapInstance.register(groupId);
            }
        } else if (!playersByRace.isEmpty() && playersByRace.get(0).isInGroup2()) {
            PlayerGroupService.addPlayer(playersByRace.get(0).getPlayerGroup2(), player);
        }
        Integer object = player.getObjectId();
        if (!worldMapInstance.isRegistered(object)) {
            worldMapInstance.register(object);
        }
        playersInside.add(player);
    }

    private List<Player> getPlayersInsideByRace(Race race) {
        return select(playersInside, having(on(Player.class).getRace(), equalTo(race)));
    }

    private List<Player> getPlayersByRace(Race race) {
        return select(players, having(on(Player.class).getRace(), equalTo(race)));
    }

    public FastList<Player> getPlayersInside() {
        return playersInside;
    }

    private synchronized int getDmgPlayerCount(Player player) {
        int dmgPlayers = 0;
        for (Player playerInside : players) {
            if (!player.getRace().equals(playerInside.getRace())) {
                continue;
            }
            switch (playerInside.getPlayerClass()) {
                case GLADIATOR:
                case ASSASSIN:
                case RANGER:
                case SORCERER:
                case SPIRIT_MASTER:
                case CHANTER:
                    dmgPlayers++;
                    break;
            }
        }
        if (dmgPlayers < 4) {
            players.add(player);
        }
        return dmgPlayers;
    }

    private synchronized boolean canEnterSpecialPlayer(Player player) {
        for (Player playerInside : players) {
            if (!player.getRace().equals(playerInside.getRace())) {
                continue;
            }
            switch (playerInside.getPlayerClass()) {
                case CLERIC:
                    if (player.getPlayerClass() == PlayerClass.CLERIC) {
                        return false;
                    }
                    break;
                case TEMPLAR:
                    if (player.getPlayerClass() == PlayerClass.TEMPLAR) {
                        return false;
                    }
                    break;
            }
        }
        players.add(player);
        return true;
    }

    public boolean canAddPlayer(Player player) {
        if (agt.isPvPSoloArena() || agt.isTrainigPvPSoloArena()) {
            if (getPlayerSize() >= 2) {
                return false;
            }
            addPlayer(player);
            return true;
        } else if (agt.isPvPFFAArena() || agt.isTrainigPvPFFAArena()) {
            if (agt.isPvPFFAArena() && getPlayerSize() >= CustomConfig.ARENA_OF_CHAOS_PLAYERS_SIZE) {
                return false;
            }
            if (agt.isTrainigPvPFFAArena() && getPlayerSize() >= 10) {
                return false;
            }
            addPlayer(player);
            return true;
        }

        switch (player.getPlayerClass()) {
            case GLADIATOR:
            case ASSASSIN:
            case RANGER:
            case SORCERER:
            case SPIRIT_MASTER:
            case CHANTER:
                return getDmgPlayerCount(player) < 4;
            case CLERIC:
            case TEMPLAR:
                return canEnterSpecialPlayer(player);
        }
        return false;
    }

    public FastList<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public boolean containPlayer(Player player) {
        return players.contains(player);
    }

    public int getInstanceId() {
        return worldMapInstance.getInstanceId();
    }

    public WorldMapInstance getWorldMapInstance() {
        return worldMapInstance;
    }

    public long getStartInstanceTime() {
        return startInstanceTime;
    }

    public boolean satisfyTime(EntryRequestType ert) {
        InstanceReward<?> instanceReward = worldMapInstance.getInstanceHandler().getInstanceReward();
        if (instanceReward != null && instanceReward.getInstanceScoreType().isEndProgress()) {
            return false;
        }
        if (registredErt.isGroupEntry() && (System.currentTimeMillis() - startInstanceTime < 121000)) {
            return false;
        }
        if (!ert.isQuickGroupEntry()) {
            return false;
        }
        int time = agt.getTime();
        if (time == 0) {
            return true;
        }
        return System.currentTimeMillis() - startInstanceTime < time;
    }

    public void unregisterPlayer(Player player) {
        players.remove(player);
        playersInside.remove(player);
    }

    public boolean hasRacePermit(Race race) {
        if (this.race == Race.PC_ALL) {
            return true;
        }
        return this.race.equals(race);
    }

    public boolean hasSizePermit() {
        if (agt.isPvPFFAArena()) {
            return getPlayerSize() == CustomConfig.ARENA_OF_CHAOS_PLAYERS_SIZE;
        }
        return agt.getPlayerSize() == getPlayerSize();
    }

    public boolean hasInstanceMask(byte instanceMaskId) {
        return this.instanceMaskId == instanceMaskId;
    }
}
