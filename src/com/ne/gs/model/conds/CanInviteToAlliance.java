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
public abstract class CanInviteToAlliance extends SimpleCond<Tuple2<Player, Player>> {

    public static final CanInviteToAlliance STATIC = new CanInviteToAlliance() {
        @Override
        public Boolean onEvent(@NotNull Tuple2<Player, Player> e) {
            Player player = e._1;
            Player target = e._2;

            if (target == null) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_FORCE_NO_USER_TO_INVITE);
                return false;
            }

            if (target.getRace() != player.getRace() && !GroupConfig.ALLIANCE_INVITEOTHERFACTION) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_CANT_INVITE_OTHER_RACE);
                return false;
            }

            com.ne.gs.model.team2.alliance.PlayerAlliance alliance = player.getPlayerAlliance2();

            if (target.isInAlliance2()) {
                if (target.getPlayerAlliance2() == alliance) {
                    player.sendPck(SM_SYSTEM_MESSAGE
                        .STR_PARTY_ALLIANCE_HE_IS_ALREADY_MEMBER_OF_OUR_ALLIANCE(target.getName()));
                    return false;
                } else {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_FORCE_ALREADY_OTHER_FORCE(target.getName()));
                    return false;
                }
            }

            if (alliance != null && alliance.isFull()) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_CANT_ADD_NEW_MEMBER);
                return false;
            }

            if (alliance != null && !alliance.isSomeCaptain(player)) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_ONLY_PARTY_LEADER_CAN_LEAVE_ALLIANCE);
                return false;
            }

            if (target.sameObjectId(player.getObjectId())) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_FORCE_CAN_NOT_INVITE_SELF);
                return false;
            }

            if (target.getLifeStats().isAlreadyDead()) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_UI_PARTY_DEAD);
                return false;
            }

            if (player.getLifeStats().isAlreadyDead()) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_FORCE_CANT_INVITE_WHEN_DEAD);
                return false;
            }

            if (target.isInGroup2()) {
                PlayerGroup targetGroup = target.getPlayerGroup2();
                if (targetGroup.isLeader(target)) {
                    player.sendPck(SM_SYSTEM_MESSAGE
                        .STR_FORCE_INVITE_PARTY_HIM(target.getName(), targetGroup.getLeader().getName()));
                    return false;
                }
                if (alliance != null && (targetGroup.size() + alliance.size() >= 24)) {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_FORCE_INVITE_FAILED_NOT_ENOUGH_SLOT);
                    return false;
                }
            }

            return target.getConditioner().check(CanBeInvitedToAlliance.class, Tuple.of(player, target));
        }
    };

    public static final CanInviteToAlliance FALSE = new CanInviteToAlliance() {
        @Override
        public Boolean onEvent(@NotNull Tuple2<Player, Player> e) {
            return false;
        }
    };

    @NotNull
    @Override
    public final String getType() {
        return CanInviteToAlliance.class.getName();
    }
}
