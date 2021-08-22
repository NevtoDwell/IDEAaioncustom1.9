/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2.handler;

import com.ne.gs.ai2.AIState;
import com.ne.gs.ai2.AISubState;
import com.ne.gs.ai2.NpcAI2;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestEnv;

/**
 * @author ATracer
 */
public final class TalkEventHandler {

    /**
     * @param npcAI
     * @param creature
     */
    public static void onTalk(NpcAI2 npcAI, Creature creature) {
        onSimpleTalk(npcAI, creature);

        if (creature instanceof Player) {
            Player player = (Player) creature;
            if (QuestEngine.getInstance().onDialog(new QuestEnv(npcAI.getOwner(), player, 0, -1))) {
                return;
            }
            player.sendPck(new SM_DIALOG_WINDOW(npcAI.getOwner().getObjectId(), 10));
        }

    }

    /**
     * @param npcAI
     * @param creature
     */
    public static void onSimpleTalk(NpcAI2 npcAI, Creature creature) {
        npcAI.setSubStateIfNot(AISubState.TALK);
        npcAI.getOwner().getMoveController().abortMove();
        npcAI.getOwner().setTarget(creature);
    }

    /**
     * @param npcAI
     * @param creature
     */
    public static void onFinishTalk(NpcAI2 npcAI, Creature creature) {
        Npc owner = npcAI.getOwner();
        if (owner.isTargeting(creature.getObjectId())) {
            if (npcAI.getState() != AIState.FOLLOWING) {
                owner.setTarget(null);
            }
            npcAI.think();
        }
    }

    /**
     * No SM_LOOKATOBJECT broadcast
     *
     * @param npcAI
     * @param creature
     */
    public static void onSimpleFinishTalk(NpcAI2 npcAI, Creature creature) {
        Npc owner = npcAI.getOwner();
        if (owner.isTargeting(creature.getObjectId()) && npcAI.setSubStateIfNot(AISubState.NONE)) {
            owner.setTarget(null);
        }
    }

}
