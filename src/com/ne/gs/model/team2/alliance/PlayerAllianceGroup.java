/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.alliance;

import com.ne.gs.model.team2.TemporaryPlayerTeam;

/**
 * @author ATracer
 */
public class PlayerAllianceGroup extends TemporaryPlayerTeam<PlayerAllianceMember> {

    private final PlayerAlliance alliance;

    public PlayerAllianceGroup(PlayerAlliance alliance, Integer objId) {
        super(objId);
        this.alliance = alliance;
    }

    @Override
    public void addMember(PlayerAllianceMember member) {
        super.addMember(member);
        member.setPlayerAllianceGroup(this);
        member.setAllianceId(getTeamId());
    }

    @Override
    public void removeMember(PlayerAllianceMember member) {
        super.removeMember(member);
        member.setPlayerAllianceGroup(null);
    }

    @Override
    public boolean isFull() {
        return size() == 6;
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

    public PlayerAlliance getAlliance() {
        return alliance;
    }

}
