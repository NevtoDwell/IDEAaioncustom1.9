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
import com.ne.gs.network.aion.serverpackets.SM_ABYSS_RANK_UPDATE;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public abstract class PlayerStopMentoringEvent<T extends TemporaryPlayerTeam<? extends TeamMember<Player>>> extends AlwaysTrueTeamEvent implements
    Predicate<Player> {

    protected final T team;
    protected final Player player;

    public PlayerStopMentoringEvent(T team, Player player) {
        this.team = team;
        this.player = player;
    }

    @Override
    public void handleEvent() {
        player.setMentor(false);
        player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_MENTOR_END);
        team.applyOnMembers(this);
        PacketSendUtility.broadcastPacketAndReceive(player, new SM_ABYSS_RANK_UPDATE(2, player));
    }

    @Override
    public boolean apply(Player member) {
        if (!player.equals(member)) {
            member.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_MENTOR_END_PARTYMSG(player.getName()));
        }
        sendGroupPacketOnMentorEnd(member);
        return true;
    }

    /**
     * @param member
     */
    protected abstract void sendGroupPacketOnMentorEnd(Player member);
}
