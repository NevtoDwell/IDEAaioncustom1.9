/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.alliance.events;

import com.google.common.base.Predicate;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.common.events.AlwaysTrueTeamEvent;
import com.ne.gs.model.team2.common.events.TeamCommand;
import com.ne.gs.network.aion.serverpackets.SM_ALLIANCE_READY_CHECK;

/**
 * @author ATracer
 */
public class CheckAllianceReadyEvent extends AlwaysTrueTeamEvent implements Predicate<Player> {

    private final PlayerAlliance alliance;
    private final Player player;
    private final TeamCommand eventCode;

    public CheckAllianceReadyEvent(PlayerAlliance alliance, Player player, TeamCommand eventCode) {
        this.alliance = alliance;
        this.player = player;
        this.eventCode = eventCode;
    }

    @Override
    public void handleEvent() {
        int readyStatus = alliance.getAllianceReadyStatus();
        switch (eventCode) {
            case ALLIANCE_CHECKREADY_CANCEL:
                readyStatus = 0;
                break;
            case ALLIANCE_CHECKREADY_START:
                readyStatus = alliance.onlineMembers() - 1;
                break;
            case ALLIANCE_CHECKREADY_AUTOCANCEL:
                readyStatus = 0;
                break;
            case ALLIANCE_CHECKREADY_READY:
            case ALLIANCE_CHECKREADY_NOTREADY:
                readyStatus -= 1;
                break;
        }
        alliance.setAllianceReadyStatus(readyStatus);
        alliance.applyOnMembers(this);
    }

    @Override
    public boolean apply(Player member) {
        switch (eventCode) {
            case ALLIANCE_CHECKREADY_CANCEL:
                member.sendPck(new SM_ALLIANCE_READY_CHECK(player.getObjectId(), 0));
                break;
            case ALLIANCE_CHECKREADY_START:
                member.sendPck(new SM_ALLIANCE_READY_CHECK(player.getObjectId(), 5));
                member.sendPck(new SM_ALLIANCE_READY_CHECK(player.getObjectId(), 1));
                break;
            case ALLIANCE_CHECKREADY_AUTOCANCEL:
                member.sendPck(new SM_ALLIANCE_READY_CHECK(player.getObjectId(), 2));
                break;
            case ALLIANCE_CHECKREADY_READY:
                member.sendPck(new SM_ALLIANCE_READY_CHECK(player.getObjectId(), 5));
                if (alliance.getAllianceReadyStatus() == 0) {
                    member.sendPck(new SM_ALLIANCE_READY_CHECK(0, 3));
                }
                break;
            case ALLIANCE_CHECKREADY_NOTREADY:
                member.sendPck(new SM_ALLIANCE_READY_CHECK(player.getObjectId(), 4));
                if (alliance.getAllianceReadyStatus() == 0) {
                    member.sendPck(new SM_ALLIANCE_READY_CHECK(0, 3));
                }
                break;
        }
        return true;
    }

}
