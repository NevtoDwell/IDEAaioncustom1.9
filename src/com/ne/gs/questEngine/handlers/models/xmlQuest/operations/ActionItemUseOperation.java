/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers.models.xmlQuest.operations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.EmotionType;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.network.aion.serverpackets.SM_USE_OBJECT;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActionItemUseOperation", propOrder = {"finish"})
public class ActionItemUseOperation extends QuestOperation {

    @XmlElement(required = true)
    protected QuestOperations finish;

    /*
     * (non-Javadoc)
     * @seecom.ne.gs.questEngine.handlers.models.xmlQuest.operations.QuestOperation#doOperate(com.aionemu.
     * gameserver.services.QuestService, com.ne.gs.questEngine.model.QuestEnv)
     */
    @Override
    public void doOperate(final QuestEnv env) {
        final Player player = env.getPlayer();
        final Npc npc;
        if (env.getVisibleObject() instanceof Npc) {
            npc = (Npc) env.getVisibleObject();
        } else {
            return;
        }
        final int defaultUseTime = 3000;
        player.sendPck(new SM_USE_OBJECT(player.getObjectId(), npc.getObjectId(), defaultUseTime, 1));
        PacketSendUtility.broadcastPacket(player,
            new SM_EMOTION(player, EmotionType.START_QUESTLOOT, 0, npc.getObjectId()), true);
        ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                player.sendPck(new SM_USE_OBJECT(player.getObjectId(), npc.getObjectId(), defaultUseTime,
                    0));
                finish.operate(env);
            }
        }, defaultUseTime);

    }

}
