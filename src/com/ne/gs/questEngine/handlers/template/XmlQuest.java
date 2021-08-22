/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers.template;

import java.util.Iterator;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.questEngine.handlers.QuestHandler;
import com.ne.gs.questEngine.handlers.models.Monster;
import com.ne.gs.questEngine.handlers.models.XmlQuestData;
import com.ne.gs.questEngine.handlers.models.xmlQuest.events.OnKillEvent;
import com.ne.gs.questEngine.handlers.models.xmlQuest.events.OnTalkEvent;
import com.ne.gs.questEngine.model.QuestDialog;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;

/**
 * @author Mr. Poke
 */
public class XmlQuest extends QuestHandler {

    private final XmlQuestData xmlQuestData;

    public XmlQuest(XmlQuestData xmlQuestData) {
        super(xmlQuestData.getId());
        this.xmlQuestData = xmlQuestData;
    }

    @Override
    public void register() {
        if (xmlQuestData.getStartNpcId() != null) {
            qe.registerQuestNpc(xmlQuestData.getStartNpcId()).addOnQuestStart(getQuestId());
            qe.registerQuestNpc(xmlQuestData.getStartNpcId()).addOnTalkEvent(getQuestId());
        }
        if (xmlQuestData.getEndNpcId() != null) {
            qe.registerQuestNpc(xmlQuestData.getEndNpcId()).addOnTalkEvent(getQuestId());
        }

        for (OnTalkEvent talkEvent : xmlQuestData.getOnTalkEvent()) {
            for (int npcId : talkEvent.getIds()) {
                qe.registerQuestNpc(npcId).addOnTalkEvent(getQuestId());
            }
        }

        for (OnKillEvent killEvent : xmlQuestData.getOnKillEvent()) {
            for (Monster monster : killEvent.getMonsters()) {
                Iterator<Integer> iterator = monster.getNpcIds().iterator();
                while (iterator.hasNext()) {
                    int monsterId = iterator.next();
                    qe.registerQuestNpc(monsterId).addOnKillEvent(getQuestId());
                }
            }
        }
    }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        env.setQuestId(getQuestId());
        for (OnTalkEvent talkEvent : xmlQuestData.getOnTalkEvent()) {
            if (talkEvent.operate(env)) {
                return true;
            }
        }

        Player player = env.getPlayer();
        int targetId = env.getTargetId();
        QuestState qs = player.getQuestStateList().getQuestState(getQuestId());
        if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
            if (targetId == xmlQuestData.getStartNpcId()) {
                if (env.getDialog() == QuestDialog.START_DIALOG) {
                    return sendQuestDialog(env, 1011);
                } else {
                    return sendQuestStartDialog(env);
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD && targetId == xmlQuestData.getEndNpcId()) {
            return sendQuestEndDialog(env);
        }
        return false;
    }

    @Override
    public boolean onKillEvent(QuestEnv env) {
        env.setQuestId(getQuestId());
        for (OnKillEvent killEvent : xmlQuestData.getOnKillEvent()) {
            if (killEvent.operate(env)) {
                return true;
            }
        }
        return false;
    }
}
