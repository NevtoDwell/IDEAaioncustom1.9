/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.group.events;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.common.events.PlayerStopMentoringEvent;
import com.ne.gs.model.team2.common.legacy.GroupEvent;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.network.aion.serverpackets.SM_GROUP_MEMBER_INFO;

/**
 * @author ATracer
 */
public class PlayerGroupStopMentoringEvent extends PlayerStopMentoringEvent<PlayerGroup> {

    /**
     * @param group
     * @param player
     */
    public PlayerGroupStopMentoringEvent(PlayerGroup group, Player player) {
        super(group, player);
    }

    @Override
    protected void sendGroupPacketOnMentorEnd(Player member) {
        member.sendPck(new SM_GROUP_MEMBER_INFO(team, player, GroupEvent.MOVEMENT));
    }

}
