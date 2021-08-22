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
import com.ne.gs.model.team2.TeamEvent;
import com.ne.gs.model.team2.common.legacy.GroupEvent;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.model.team2.group.PlayerGroupService;
import com.ne.gs.network.aion.serverpackets.SM_GROUP_INFO;
import com.ne.gs.network.aion.serverpackets.SM_GROUP_MEMBER_INFO;
import com.ne.gs.network.aion.serverpackets.SM_INSTANCE_INFO;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author ATracer
 */
public class PlayerEnteredEvent implements Predicate<Player>, TeamEvent {

    private final PlayerGroup group;
    private final Player enteredPlayer;

    public PlayerEnteredEvent(PlayerGroup group, Player enteredPlayer) {
        this.group = group;
        this.enteredPlayer = enteredPlayer;
    }

    /**
     * Entered player should not be in group yet
     */
    @Override
    public boolean checkCondition() {
        return !group.hasMember(enteredPlayer.getObjectId());
    }

    @Override
    public void handleEvent() {
        PlayerGroupService.addPlayerToGroup(group, enteredPlayer);
        enteredPlayer.sendPck(new SM_GROUP_INFO(group));
        enteredPlayer.sendPck(new SM_GROUP_MEMBER_INFO(group, enteredPlayer, GroupEvent.JOIN));
        enteredPlayer.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_ENTERED_PARTY);
        group.applyOnMembers(this);
    }

    @Override
    public boolean apply(Player player) {
        if (!player.getObjectId().equals(enteredPlayer.getObjectId())) {
            // TODO probably here JOIN event
            player.sendPck(new SM_GROUP_MEMBER_INFO(group, enteredPlayer, GroupEvent.ENTER));
            player.sendPck(new SM_INSTANCE_INFO(enteredPlayer, false, group));
            player.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_HE_ENTERED_PARTY(enteredPlayer.getName()));

            enteredPlayer.sendPck(new SM_GROUP_MEMBER_INFO(group, player, GroupEvent.ENTER));
        }
        return true;
    }

}
