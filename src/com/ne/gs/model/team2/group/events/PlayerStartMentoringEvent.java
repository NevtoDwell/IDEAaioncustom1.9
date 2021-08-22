/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.group.events;

import com.google.common.base.Predicate;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.common.events.AlwaysTrueTeamEvent;
import com.ne.gs.model.team2.common.legacy.GroupEvent;
import com.ne.gs.model.team2.group.PlayerFilters.MentorSuiteFilter;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.network.aion.serverpackets.SM_ABYSS_RANK_UPDATE;
import com.ne.gs.network.aion.serverpackets.SM_GROUP_MEMBER_INFO;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.audit.AuditLogger;

/**
 * @author ATracer
 */
public class PlayerStartMentoringEvent extends AlwaysTrueTeamEvent implements Predicate<Player> {

    private final PlayerGroup group;
    private final Player player;

    public PlayerStartMentoringEvent(PlayerGroup group, Player player) {
        this.group = group;
        this.player = player;
    }

    @Override
    public void handleEvent() {
        if (group.filterMembers(new MentorSuiteFilter(player)).size() == 0) {
            AuditLogger.info(player, "Send fake start mentoring packet");
            return;
        }
        player.setMentor(true);
        player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_MENTOR_START);
        group.applyOnMembers(this);
        PacketSendUtility.broadcastPacketAndReceive(player, new SM_ABYSS_RANK_UPDATE(2, player));
    }

    @Override
    public boolean apply(Player member) {
        if (!player.equals(member)) {
            member.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_MENTOR_START_PARTYMSG(player.getName()));
        }
        member.sendPck(new SM_GROUP_MEMBER_INFO(group, player, GroupEvent.MOVEMENT));
        return true;
    }
}
