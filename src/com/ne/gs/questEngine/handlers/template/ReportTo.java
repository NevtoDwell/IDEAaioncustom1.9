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
 * @author MrPoke Like: Sleeping on the Job quest.
 */
public class ReportTo extends QuestHandler {

    private final Set<Integer> startNpcs = new HashSet<>();
    private final Set<Integer> endNpcs = new HashSet<>();
    private final int itemId;

    /**
     * @param questId
     */
    public ReportTo(int questId, List<Integer> startNpcIds, List<Integer> endNpcIds, int itemId) {
        super(questId);
        startNpcs.addAll(startNpcIds);
        startNpcs.remove(0);
        if (endNpcIds != null) {
            endNpcs.addAll(endNpcIds);
            endNpcs.remove(0);
        }
        this.itemId = itemId;
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
    }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        int targetId = env.getTargetId();
        QuestDialog dialog = env.getDialog();
        QuestState qs = player.getQuestStateList().getQuestState(getQuestId());

        if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
            if ((startNpcs.isEmpty()) || (startNpcs.contains(targetId))) {
                switch (dialog) {
                    case START_DIALOG: {
                        return sendQuestDialog(env, 1011);
                    }
                    case ACCEPT_QUEST: {
                        if (itemId != 0) {
                            if (giveQuestItem(env, itemId, 1)) {
                                return sendQuestStartDialog(env);
                            }
                            return false;
                        } else {
                            return sendQuestStartDialog(env);
                        }
                    }
                    default: {
                        return sendQuestStartDialog(env);
                    }
                }
            }
        } else if (qs.getStatus() == QuestStatus.START) {
            if (startNpcs.contains(targetId)) {
                if (dialog == QuestDialog.FINISH_DIALOG) {
                    return sendQuestSelectionDialog(env);
                }
            } else if (endNpcs.contains(targetId)) {
                switch (dialog) {
                    case START_DIALOG: {
                        return sendQuestDialog(env, 2375);
                    }
                    case SELECT_REWARD: {
                        if (itemId != 0) {
                            if (player.getInventory().getItemCountByItemId(itemId) < 1) {
                                return sendQuestSelectionDialog(env);
                            }
                        }
                        removeQuestItem(env, itemId, 1);
                        qs.setQuestVar(1);
                        qs.setStatus(QuestStatus.REWARD);
                        updateQuestStatus(env);
                        return sendQuestEndDialog(env);
                    }
                }
            }
        } else if ((qs.getStatus() == QuestStatus.REWARD) && (endNpcs.contains(targetId))) {
            return sendQuestEndDialog(env);
        }

        return false;
    }
}
