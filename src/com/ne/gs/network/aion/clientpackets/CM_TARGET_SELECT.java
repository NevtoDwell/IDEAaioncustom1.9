/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Trap;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.TeamMember;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_TARGET_SELECTED;
import com.ne.gs.network.aion.serverpackets.SM_TARGET_UPDATE;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.audit.AuditLogger;

/**
 * Client Sends this packet when /Select NAME is typed.<br>
 * I believe it's the same as mouse click on a character.<br>
 * If client want's to select target - d is object id.<br>
 * If client unselects target - d is 0;
 *
 * @author SoulKeeper, Sweetkr, KID
 */
public class CM_TARGET_SELECT extends AionClientPacket {

    /**
     * Target object id that client wants to select or 0 if wants to unselect
     */
    private int targetObjectId;
    private int type;

    /**
     * Read packet.<br>
     * d - object id; c - selection type;
     */
    @Override
    protected void readImpl() {
        targetObjectId = readD();
        type = readC();
    }

    /**
     * Do logging
     */
    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        VisibleObject obj = null;
        ;
        if (targetObjectId == player.getObjectId()) {
            obj = player;
        } else {
            obj = player.getKnownList().getObject(targetObjectId);

            if (obj == null && player.isInTeam()) {
                TeamMember<Player> member = player.getCurrentTeam().getMember(targetObjectId);
                if (member != null) {
                    obj = member.getObject();
                }
            }
        }

        if (obj != null) {
            if (type == 1) {
                if (obj.getTarget() == null) {
                    return;
                }
                player.setTarget(obj.getTarget());
            } else {
                player.setTarget(obj);
            }
            if (obj instanceof Player) {
                Player target = (Player) obj;
                if (player != obj && !player.canSee(target)) {
                    AuditLogger.info(player,
                        "Possible radar hacker detected, targeting on invisible Player name: " + target.getName() + " objectId: " + target.getObjectId()
                            + " by");
                }
            } else if (obj instanceof Trap) {
                Trap target = (Trap) obj;
                boolean isSameTeamTrap = false;
                if (target.getMaster() instanceof Player) {
                    isSameTeamTrap = ((Player) target.getMaster()).isInSameTeam(player);
                }
                if (player != obj && !player.canSee(target) && !isSameTeamTrap) {
                    AuditLogger.info(player,
                        "Possible radar hacker detected, targeting on invisible Trap name: " + target.getName() + " objectId: " + target.getObjectId()
                            + " by");
                }
            } else if (obj instanceof Creature) {
                Creature target = (Creature) obj;
                if (player != obj && !player.canSee(target)) {
                    AuditLogger.info(player,
                        "Possible radar hacker detected, targeting on invisible Npc name: " + target.getName() + " objectId: " + target.getObjectId()
                            + " by");
                }
            }
        } else {
            player.setTarget(null);
        }
        sendPacket(new SM_TARGET_SELECTED(player));
        PacketSendUtility.broadcastPacket(player, new SM_TARGET_UPDATE(player));
    }
}
