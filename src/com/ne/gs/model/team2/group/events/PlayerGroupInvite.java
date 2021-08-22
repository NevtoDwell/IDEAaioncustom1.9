/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.group.events;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.RequestResponseHandler;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.model.team2.group.PlayerGroupService;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author ATracer
 */
public class PlayerGroupInvite extends RequestResponseHandler {

    private final Player inviter;
    private final Player invited;

    public PlayerGroupInvite(Player inviter, Player invited) {
        super(inviter);
        this.inviter = inviter;
        this.invited = invited;
    }

    @Override
    public void acceptRequest(Creature requester, Player responder) {
        if (PlayerGroupService.canInvite(inviter, invited)) {
            inviter.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_INVITED_HIM(invited.getName()));
            PlayerGroup group = inviter.getPlayerGroup2();
            if (group != null) {
                PlayerGroupService.addPlayer(group, invited);
            } else {
                PlayerGroupService.createGroup(inviter, invited);
            }
        }
    }

    @Override
    public void denyRequest(Creature requester, Player responder) {
        inviter.sendPck(SM_SYSTEM_MESSAGE.STR_PARTY_HE_REJECT_INVITATION(responder.getName()));
    }

}
