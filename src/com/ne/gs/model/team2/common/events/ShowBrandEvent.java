/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.common.events;

import com.google.common.base.Predicate;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.TeamMember;
import com.ne.gs.model.team2.TemporaryPlayerTeam;
import com.ne.gs.network.aion.serverpackets.SM_SHOW_BRAND;

/**
 * @author ATracer
 */
public class ShowBrandEvent<T extends TemporaryPlayerTeam<? extends TeamMember<Player>>> extends AlwaysTrueTeamEvent implements Predicate<Player> {

    private final T team;
    private final int targetObjId;
    private final int brandId;

    public ShowBrandEvent(T team, int targetObjId, int brandId) {
        this.team = team;
        this.targetObjId = targetObjId;
        this.brandId = brandId;
    }

    @Override
    public void handleEvent() {
        team.applyOnMembers(this);
    }

    @Override
    public boolean apply(Player member) {
        member.sendPck(new SM_SHOW_BRAND(brandId, targetObjId));
        return true;
    }

}
