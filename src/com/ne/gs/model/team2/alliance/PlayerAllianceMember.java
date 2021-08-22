/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.alliance;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.PlayerTeamMember;

/**
 * @author ATracer
 */
public class PlayerAllianceMember extends PlayerTeamMember {

    private int allianceId;

    public PlayerAllianceMember(Player player) {
        super(player);
    }

    public int getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(int allianceId) {
        this.allianceId = allianceId;
    }

    public final PlayerAllianceGroup getPlayerAllianceGroup() {
        return getObject().getPlayerAllianceGroup2();
    }

    public final void setPlayerAllianceGroup(PlayerAllianceGroup playerAllianceGroup) {
        getObject().setPlayerAllianceGroup2(playerAllianceGroup);
    }

}
