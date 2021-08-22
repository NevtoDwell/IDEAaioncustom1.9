/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.alliance.events;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.common.events.AbstractTeamPlayerEvent;
import com.ne.gs.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author ATracer
 */
public class AssignViceCaptainEvent extends AbstractTeamPlayerEvent<PlayerAlliance> {

    public static enum AssignType {
        PROMOTE,
        DEMOTE_CAPTAIN_TO_VICECAPTAIN,
        DEMOTE
    }

    private final AssignType assignType;

    public AssignViceCaptainEvent(PlayerAlliance team, Player eventPlayer, AssignType assignType) {
        super(team, eventPlayer);
        this.assignType = assignType;
    }

    @Override
    public boolean checkCondition() {
        return eventPlayer != null && eventPlayer.isOnline();
    }

    @Override
    public void handleEvent() {
        switch (assignType) {
            case DEMOTE:
                team.getViceCaptainIds().remove(eventPlayer.getObjectId());
                break;
            case PROMOTE:
                if (team.getViceCaptainIds().size() == 4) {
                    team.getLeaderObject().sendPck(SM_SYSTEM_MESSAGE.STR_FORCE_CANNOT_PROMOTE_MANAGER);
                    return;
                }
                team.getViceCaptainIds().add(eventPlayer.getObjectId());
                break;
            case DEMOTE_CAPTAIN_TO_VICECAPTAIN:
                team.getViceCaptainIds().add(eventPlayer.getObjectId());
                break;
        }

        team.applyOnMembers(this);
    }

    @Override
    public boolean apply(Player player) {
        int messageId = 0;
        switch (assignType) {
            case PROMOTE:
                messageId = SM_ALLIANCE_INFO.VICECAPTAIN_PROMOTE;
                break;
            case DEMOTE:
                messageId = SM_ALLIANCE_INFO.VICECAPTAIN_DEMOTE;
                break;
        }
        // TODO check whether same is sent to eventPlayer
        player.sendPck(new SM_ALLIANCE_INFO(team, messageId, eventPlayer.getName()));
        return true;
    }

}
