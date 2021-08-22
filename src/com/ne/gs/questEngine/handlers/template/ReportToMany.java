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
import javolution.util.FastMap;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.questEngine.handlers.HandlerResult;
import com.ne.gs.questEngine.handlers.QuestHandler;
import com.ne.gs.questEngine.handlers.models.NpcInfos;
import com.ne.gs.questEngine.model.QuestDialog;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;
import com.ne.gs.services.QuestService;

/**
 * @author Hilgert
 * @modified vlog
 */
public class ReportToMany extends QuestHandler {

    private final int startItem;
    private final Set<Integer> startNpcs = new HashSet<>();
    private final Set<Integer> endNpcs = new HashSet<>();
    private final int startDialog;
    private final int endDialog;
    private final int maxVar;
    private final FastMap<Integer, NpcInfos> npcInfos;

    /**
     * @param questId
     * @param startItem
     * @param startDialog
     * @param endDialog
     * @param maxVar
     */
    public ReportToMany(int questId, int startItem, List<Integer> startNpcIds, List<Integer> endNpcIds, FastMap<Integer, NpcInfos> npcInfos, int startDialog, int endDialog, int maxVar) {
        super(questId);
        this.startItem = startItem;
        if (startNpcIds != null) {
            startNpcs.addAll(startNpcIds);
            startNpcs.remove(0);
        }
        if (endNpcIds != null) {
            endNpcs.addAll(endNpcIds);
            endNpcs.remove(0);
        }
        this.npcInfos = npcInfos;
        this.startDialog = startDialog;
        this.endDialog = endDialog;
        this.maxVar = maxVar;
    }

    @Override
    public void register() {
        if (startItem != 0) {
            qe.registerQuestItem(startItem, getQuestId());
        } else {
            Iterator<Integer> iterator = startNpcs.iterator();
            while (iterator.hasNext()) {
                int startNpc = iterator.next();
                qe.registerQuestNpc(startNpc).addOnQuestStart(getQuestId());
                qe.registerQuestNpc(startNpc).addOnTalkEvent(getQuestId());
            }
        }
        for (int npcId : npcInfos.keySet()) {
            qe.registerQuestNpc(npcId).addOnTalkEvent(getQuestId());
        }
        Iterator<Integer> iterator = endNpcs.iterator();
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
            if (startItem != 0) {
                if (dialog == QuestDialog.ACCEPT_QUEST) {
                    QuestService.startQuest(env);
                    return closeDialogWindow(env);
                }
            }
            if ((startNpcs.isEmpty()) || (startNpcs.contains(targetId))) {
                if (dialog == QuestDialog.START_DIALOG) {
                    return sendQuestDialog(env, startDialog);
                } else {
                    return sendQuestStartDialog(env);
                }
            }
        } else if (qs.getStatus() == QuestStatus.START) {
            int var = qs.getQuestVarById(0);
            NpcInfos targetNpcInfo = npcInfos.get(targetId);
            if (var <= maxVar) {
                if (targetNpcInfo != null && var == targetNpcInfo.getVar()) {
                    int closeDialog;
                    if (targetNpcInfo.getCloseDialog() == 0) {
                        closeDialog = 10000 + targetNpcInfo.getVar();
                    } else {
                        closeDialog = targetNpcInfo.getCloseDialog();
                    }

                    if (dialog == QuestDialog.START_DIALOG) {
                        return sendQuestDialog(env, targetNpcInfo.getQuestDialog());
                    } else if (env.getDialogId() == closeDialog) {
                        if (var == maxVar) {
                            qs.setStatus(QuestStatus.REWARD);
                            if (closeDialog == 1009) {
                                return sendQuestDialog(env, 5);
                            }
                        } else {
                            qs.setQuestVarById(0, var + 1);
                        }
                        updateQuestStatus(env);
                        return sendQuestSelectionDialog(env);
                    }
                }
            } else if ((var > maxVar) && (endNpcs.contains(targetId))) {
                if (dialog == QuestDialog.START_DIALOG) {
                    return sendQuestDialog(env, endDialog);
                } else if (env.getDialog() == QuestDialog.SELECT_REWARD) {
                    if (startItem != 0) {
                        if (!removeQuestItem(env, startItem, 1)) {
                            return false;
                        }
                    }
                    qs.setStatus(QuestStatus.REWARD);
                    updateQuestStatus(env);
                    return sendQuestEndDialog(env);
                }
            }

        } else if ((qs.getStatus() == QuestStatus.REWARD) && (endNpcs.contains(targetId))) {
            return sendQuestEndDialog(env);
        }
        return false;
    }

    @Override
    public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
        if (startItem != 0) {
            Player player = env.getPlayer();
            QuestState qs = player.getQuestStateList().getQuestState(getQuestId());
            if (qs == null || qs.getStatus() == QuestStatus.NONE) {
                return HandlerResult.fromBoolean(sendQuestDialog(env, 4));
            }
        }
        return HandlerResult.UNKNOWN;
    }
}
