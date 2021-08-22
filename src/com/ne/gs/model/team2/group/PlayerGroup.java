/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.group;

import com.ne.gs.model.team2.TemporaryPlayerTeam;
import com.ne.gs.utils.idfactory.IDFactory;

/**
 * @author ATracer
 */
public class PlayerGroup extends TemporaryPlayerTeam<PlayerGroupMember> {

    private final PlayerGroupStats playerGroupStats;
    private int groupType = 0x3F;

    public PlayerGroup(PlayerGroupMember leader) {
        super(IDFactory.getInstance().nextId());
        playerGroupStats = new PlayerGroupStats(this);
        initializeTeam(leader);
    }

    @Override
    public void addMember(PlayerGroupMember member) {
        super.addMember(member);
        playerGroupStats.onAddPlayer(member);
        member.getObject().setPlayerGroup2(this);
    }

    @Override
    public void removeMember(PlayerGroupMember member) {
        super.removeMember(member);
        playerGroupStats.onRemovePlayer(member);
        member.getObject().setPlayerGroup2(null);
    }

    @Override
    public boolean isFull() {
        return size() == 6;
    }

    @Override
    public int getMinExpPlayerLevel() {
        return playerGroupStats.getMinExpPlayerLevel();
    }

    @Override
    public int getMaxExpPlayerLevel() {
        return playerGroupStats.getMaxExpPlayerLevel();

    }

    public final int getGroupType() {
        return groupType;
    }

    public void setGroupType(int groupType) {
        this.groupType = groupType;
    }
}
