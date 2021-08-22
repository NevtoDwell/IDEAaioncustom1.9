/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.alliance.events;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.google.common.base.Preconditions;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.RequestResponseHandler;
import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.alliance.PlayerAllianceService;
import com.ne.gs.model.team2.group.PlayerFilters.ExcludePlayerFilter;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.model.team2.group.PlayerGroupService;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author ATracer
 */
public class PlayerAllianceInvite extends RequestResponseHandler {

    private final Player inviter;
    private final Player invited;

    public PlayerAllianceInvite(Player inviter, Player invited) {
        super(inviter);
        this.inviter = inviter;
        this.invited = invited;
    }

    @Override
    public void acceptRequest(Creature requester, Player responder) {
        if (PlayerAllianceService.canInvite(inviter, invited)) {

            PlayerAlliance alliance = inviter.getPlayerAlliance2();

            if (alliance != null) {
                if (alliance.size() == 24) {
                    invited.sendMsg("That alliance is already full.");
                    inviter.sendMsg("Your alliance is already full.");
                    return;
                } else if (invited.isInGroup2() && invited.getPlayerGroup2().size() + alliance.size() > 24) {
                    invited.sendMsg("That alliance is now too full for your group to join.");
                    inviter.sendMsg("Your alliance is now too full for that group to join.");
                    return;
                }
            }

            List<Player> playersToAdd = new ArrayList<>();
            collectPlayersToAdd(playersToAdd, alliance);

            if (alliance == null) {
                alliance = PlayerAllianceService.createAlliance(inviter, invited);
            }

            for (Player member : playersToAdd) {
                PlayerAllianceService.addPlayer(alliance, member);
            }
        }
    }

    private final void collectPlayersToAdd(List<Player> playersToAdd, PlayerAlliance alliance) {
        // Collect Inviter Group without leader
        if (inviter.isInGroup2()) {
            Preconditions.checkState(alliance == null, "If inviter is in group - alliance should be null");
            PlayerGroup group = inviter.getPlayerGroup2();
            playersToAdd.addAll(group.filterMembers(new ExcludePlayerFilter(inviter)));

            Iterator<Player> pIter = group.getMembers().iterator();
            while (pIter.hasNext()) {
                PlayerGroupService.removePlayer(pIter.next());
            }
        }

        // Collect full Invited Group
        if (invited.isInGroup2()) {
            PlayerGroup group = invited.getPlayerGroup2();
            playersToAdd.addAll(group.getMembers());
            Iterator<Player> pIter = group.getMembers().iterator();
            while (pIter.hasNext()) {
                PlayerGroupService.removePlayer(pIter.next());
            }
        } else {
            playersToAdd.add(invited);
        }
    }

    @Override
    public void denyRequest(Creature requester, Player responder) {
        inviter.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_HE_REJECT_INVITATION(responder.getName()));
    }

}
