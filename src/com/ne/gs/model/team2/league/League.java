/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.league;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.GeneralTeam;
import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.alliance.PlayerAllianceMember;
import com.ne.gs.model.team2.common.legacy.LootGroupRules;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.utils.idfactory.IDFactory;

/**
 * @author ATracer
 */
public class League extends GeneralTeam<PlayerAlliance, LeagueMember> {

    private LootGroupRules lootGroupRules = new LootGroupRules();
    private static final LeagueMemberComparator MEMBER_COMPARATOR = new LeagueMemberComparator();

    public League(LeagueMember leader) {
        super(IDFactory.getInstance().nextId());
        initializeTeam(leader);
    }

    protected final void initializeTeam(LeagueMember leader) {
        setLeader(leader);
    }

    @Override
    public Collection<PlayerAlliance> getOnlineMembers() {
        return getMembers();
    }

    @Override
    public void addMember(LeagueMember member) {
        super.addMember(member);
        member.getObject().setLeague(this);
    }

    @Override
    public void removeMember(LeagueMember member) {
        super.removeMember(member);
        member.getObject().setLeague(null);
    }

    @Override
    public void sendPacket(AionServerPacket packet) {
        for (PlayerAlliance alliance : getMembers()) {
            alliance.sendPacket(packet);
        }
    }

    @Override
    public void sendPacket(AionServerPacket packet, Predicate<PlayerAlliance> predicate) {
        for (PlayerAlliance alliance : getMembers()) {
            if (predicate.apply(alliance)) {
                alliance.sendPacket(packet, Predicates.<Player>alwaysTrue());
            }
        }
    }

    @Override
    public int onlineMembers() {
        return getMembers().size();
    }

    @Override
    public Race getRace() {
        return getLeaderObject().getRace();
    }

    @Override
    public boolean isFull() {
        return size() == 8;
    }

    public LootGroupRules getLootGroupRules() {
        return lootGroupRules;
    }

    public void setLootGroupRules(LootGroupRules lootGroupRules) {
        this.lootGroupRules = lootGroupRules;
    }

    /**
     * @return sorted alliances by position
     */
    public Collection<LeagueMember> getSortedMembers() {
        ArrayList<LeagueMember> newArrayList = Lists.newArrayList(members.values());
        Collections.sort(newArrayList, MEMBER_COMPARATOR);
        return newArrayList;
    }

    /**
     * Search for player member in all alliances
     *
     * @return player object
     */
    public Player getPlayerMember(Integer playerObjId) {
        for (PlayerAlliance member : getMembers()) {
            PlayerAllianceMember playerMember = member.getMember(playerObjId);
            if (playerMember != null) {
                return playerMember.getObject();
            }
        }
        return null;
    }

    static class LeagueMemberComparator implements Comparator<LeagueMember> {

        @Override
        public int compare(LeagueMember o1, LeagueMember o2) {
            return o1.getLeaguePosition() > o2.getLeaguePosition() ? 1 : -1;
        }

    }

}
