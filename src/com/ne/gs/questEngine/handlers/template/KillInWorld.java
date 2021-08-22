/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers.template;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.questEngine.handlers.QuestHandler;
import com.ne.gs.questEngine.model.QuestDialog;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;

/**
 * Standard xml-based handling for the DAILY quests with onKillInZone events
 *
 * @author vlog
 */
public class KillInWorld extends QuestHandler {

    private final int questId;
    private final Set<Integer> startNpcs = new HashSet<>();
    private final Set<Integer> endNpcs = new HashSet<>();
    private final Set<Integer> worldIds = new HashSet<>();
    private final int killAmount;

    public KillInWorld(int questId, List<Integer> endNpcIds, List<Integer> startNpcIds, List<Integer> worldIds, int killAmount) {
        super(questId);
        if (startNpcIds != null) {
            startNpcs.addAll(startNpcIds);
            startNpcs.remove(0);
        }
        if (endNpcIds == null) {
            endNpcs.addAll(startNpcs);
        } else {
            endNpcs.addAll(endNpcIds);
            endNpcs.remove(0);
        }
        this.questId = questId;
        this.worldIds.addAll(worldIds);
        this.worldIds.remove(0);
        this.killAmount = killAmount;
    }

    @Override
    public void register() {
        Iterator<Integer> iterator = startNpcs.iterator();
        while (iterator.hasNext()) {
            int startNpc = iterator.next();
            qe.registerQuestNpc(startNpc).addOnQuestStart(getQuestId());
            qe.registerQuestNpc(startNpc).addOnTalkEvent(getQuestId());
        }
        iterator = endNpcs.iterator();
        while (iterator.hasNext()) {
            int endNpc = iterator.next();
            qe.registerQuestNpc(endNpc).addOnTalkEvent(getQuestId());
        }
        iterator = worldIds.iterator();
        while (iterator.hasNext()) {
            int worldId = iterator.next();
            qe.registerOnKillInWorld(worldId, questId);
        }
    }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        int targetId = env.getTargetId();
        QuestDialog dialog = env.getDialog();
        if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
            if ((startNpcs.isEmpty()) || (startNpcs.contains(targetId))) {
                switch (dialog) {
                    case START_DIALOG: {
                        return sendQuestDialog(env, 4762);
                    }
                    case ACCEPT_QUEST: {
                        return sendQuestStartDialog(env);
                    }
                    default: {
                        return sendQuestStartDialog(env);
                    }
                }
            }
        } else if ((qs != null) && (qs.getStatus() == QuestStatus.REWARD) && (endNpcs.contains(targetId))) {
            return sendQuestEndDialog(env);
        }

        return false;
    }

    @Override
    public boolean onKillInWorldEvent(QuestEnv env) {
        return defaultOnKillRankedEvent(env, 0, killAmount, true); // reward
    }
}
