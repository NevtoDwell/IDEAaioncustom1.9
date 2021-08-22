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
import com.ne.gs.services.QuestService;

/**
 * @author MrPoke
 * @reworked vlog
 */
public class ItemCollecting extends QuestHandler {

    private final Set<Integer> startNpcs = new HashSet<>();
    private final Set<Integer> actionItems = new HashSet<>();
    private final Set<Integer> endNpcs = new HashSet<>();
    private final int questMovie;

    public ItemCollecting(int questId, List<Integer> startNpcIds, List<Integer> actionItemIds, List<Integer> endNpcIds, int questMovie) {
        super(questId);
        startNpcs.addAll(startNpcIds);
        startNpcs.remove(0);
        if (actionItemIds != null) {
            actionItems.addAll(actionItemIds);
            actionItems.remove(0);
        }
        if (endNpcIds == null) {
            endNpcs.addAll(startNpcs);
        } else {
            endNpcs.addAll(endNpcIds);
            endNpcs.remove(0);
        }
        this.questMovie = questMovie;
    }

    @Override
    public void register() {
        Iterator<Integer> iterator = startNpcs.iterator();
        while (iterator.hasNext()) {
            int startNpc = iterator.next();
            qe.registerQuestNpc(startNpc).addOnQuestStart(getQuestId());
            qe.registerQuestNpc(startNpc).addOnTalkEvent(getQuestId());
        }

        iterator = actionItems.iterator();
        while (iterator.hasNext()) {
            int actionItem = iterator.next();
            qe.registerQuestNpc(actionItem).addOnTalkEvent(getQuestId());
            qe.registerCanAct(getQuestId(), actionItem);
        }

        iterator = endNpcs.iterator();
        while (iterator.hasNext()) {
            int endNpc = iterator.next();
            qe.registerQuestNpc(endNpc).addOnTalkEvent(getQuestId());
        }
    }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(getQuestId());
        QuestDialog dialog = env.getDialog();
        int targetId = env.getTargetId();

        if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
            if ((startNpcs.isEmpty()) || (startNpcs.contains(targetId))) {
                switch (dialog) {
                    case START_DIALOG: {
                        if (!QuestService.inventoryItemCheck(env, true)) {
                            return true;
                        }
                        return sendQuestDialog(env, 1011);
                    }
                    case SELECT_ACTION_1012: {
                        if (questMovie != 0) {
                            playQuestMovie(env, questMovie);
                        }
                        return sendQuestDialog(env, 1012);
                    }
                    default: {
                        return sendQuestStartDialog(env);
                    }
                }
            }
        } else if (qs.getStatus() == QuestStatus.START) {
            int var = qs.getQuestVarById(0);
            if (endNpcs.contains(targetId)) {
                switch (dialog) {
                    case START_DIALOG: {
                        return sendQuestDialog(env, 2375);
                    }
                    case CHECK_COLLECTED_ITEMS: {
                        return checkQuestItems(env, var, var, true, 5, 2716); // reward
                    }
                    case CHECK_COLLECTED_ITEMS_SIMPLE:
                        return checkQuestItemsSimple(env, var, var, true, 5, 0, 0);
                    case FINISH_DIALOG: {
                        return sendQuestSelectionDialog(env);
                    }
                }
            } else if ((targetId != 0) && (actionItems.contains(targetId))) {
                return true; // looting
            }
        } else if ((qs.getStatus() == QuestStatus.REWARD) && (endNpcs.contains(targetId))) {
            return sendQuestEndDialog(env);
        }

        return false;
    }
}
