/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.alliance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import com.google.common.base.Preconditions;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.TemporaryPlayerTeam;
import com.ne.gs.model.team2.league.League;
import com.ne.gs.utils.idfactory.IDFactory;

/**
 * @author ATracer
 */
public class PlayerAlliance extends TemporaryPlayerTeam<PlayerAllianceMember> {

    private final Map<Integer, PlayerAllianceGroup> groups = new HashMap<>();
    private final Collection<Integer> viceCaptainIds = new CopyOnWriteArrayList<>();
    private int allianceReadyStatus;

    private League league;

    public PlayerAlliance(PlayerAllianceMember leader) {
        super(IDFactory.getInstance().nextId());
        initializeTeam(leader);
        for (int groupId = 1000; groupId <= 1003; groupId++) {
            groups.put(groupId, new PlayerAllianceGroup(this, groupId));
        }
    }

    @Override
    public void addMember(PlayerAllianceMember member) {
        super.addMember(member);
        PlayerAllianceGroup openAllianceGroup = getOpenAllianceGroup();
        openAllianceGroup.addMember(member);
    }

    @Override
    public void removeMember(PlayerAllianceMember member) {
        super.removeMember(member);
        member.getPlayerAllianceGroup().removeMember(member);
    }

    @Override
    public boolean isFull() {
        return size() == 24;
    }

    @Override
    public int getMinExpPlayerLevel() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getMaxExpPlayerLevel() {
        // TODO Auto-generated method stub
        return 0;
    }

    public PlayerAllianceGroup getOpenAllianceGroup() {
        lock();
        try {
            for (int groupId = 1000; groupId <= 1003; groupId++) {
                PlayerAllianceGroup playerAllianceGroup = groups.get(groupId);
                if (!playerAllianceGroup.isFull()) {
                    return playerAllianceGroup;
                }
            }
        } finally {
            unlock();
        }
        throw new IllegalStateException("All alliance groups are full.");
    }

    public PlayerAllianceGroup getAllianceGroup(Integer allianceGroupId) {
        PlayerAllianceGroup allianceGroup = groups.get(allianceGroupId);
        Preconditions.checkNotNull(allianceGroup, "No such alliance group " + allianceGroupId);
        return allianceGroup;
    }

    public final Collection<Integer> getViceCaptainIds() {
        return viceCaptainIds;
    }

    public final boolean isViceCaptain(Player player) {
        return viceCaptainIds.contains(player.getObjectId());
    }

    public final boolean isSomeCaptain(Player player) {
        return isLeader(player) || isViceCaptain(player);
    }

    public int getAllianceReadyStatus() {
        return allianceReadyStatus;
    }

    public void setAllianceReadyStatus(int allianceReadyStatus) {
        this.allianceReadyStatus = allianceReadyStatus;
    }

    public final League getLeague() {
        return league;
    }

    public final void setLeague(League league) {
        this.league = league;
    }

    public final boolean isInLeague() {
        return league != null;
    }

    public final int groupSize() {
        return groups.size();
    }

    public final Collection<PlayerAllianceGroup> getGroups() {
        return groups.values();
    }

}
