/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2014, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.conds;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.utils.SimpleCond;
import com.ne.gs.configs.main.GroupConfig;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * This class ...
 *
 * @author hex1r0
 */
public abstract class CanInviteToGroup extends SimpleCond<Tuple2<Player, Player>> {

    public static final CanInviteToGroup STATIC = new CanInviteToGroup() {
        @Override
        public Boolean onEvent(@NotNull Tuple2<Player, Player> e) {
            Player player = e._1;
            Player target = e._2;

            PlayerGroup group = player.getPlayerGroup2();

            if (group != null && group.isFull()) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_CANT_ADD_NEW_MEMBER);
                return false;
            } else if (group != null && !player.getObjectId().equals(group.getLeader().getObjectId())) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_ONLY_LEADER_CAN_INVITE);
                return false;
            } else if (target == null) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_NO_USER_TO_INVITE);
                return false;
            } else if (target.getRace() != player.getRace() && !GroupConfig.GROUP_INVITEOTHERFACTION) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_CANT_INVITE_OTHER_RACE);
                return false;
            } else if (target.sameObjectId(player.getObjectId())) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_CAN_NOT_INVITE_SELF);
                return false;
            } else if (target.getLifeStats().isAlreadyDead()) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_UI_PARTY_DEAD);
                return false;
            } else if (player.getLifeStats().isAlreadyDead()) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_CANT_INVITE_WHEN_DEAD);
                return false;
            } else if (player.isInGroup2() && target.isInGroup2() && player.getPlayerGroup2().getTeamId().equals(target
                .getPlayerGroup2().getTeamId())) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_HE_IS_ALREADY_MEMBER_OF_OUR_PARTY(target.getName()));
            } else if (target.isInGroup2()) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_HE_IS_ALREADY_MEMBER_OF_OTHER_PARTY(target.getName()));
                return false;
            } else if (target.isInAlliance2()) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_FORCE_ALREADY_OTHER_FORCE(target.getName()));
                return false;
            }

            return target.getConditioner().check(CanBeInvitedToGroup.class, Tuple.of(player, target));
        }
    };

    public static final CanInviteToGroup FALSE = new CanInviteToGroup() {
        @Override
        public Boolean onEvent(@NotNull Tuple2<Player, Player> e) {
            return false;
        }
    };

    @NotNull
    @Override
    public final String getType() {
        return CanInviteToGroup.class.getName();
    }
}
