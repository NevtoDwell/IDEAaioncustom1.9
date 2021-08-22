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
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.questEngine.handlers.QuestHandler;
import com.ne.gs.questEngine.model.QuestActionType;
import com.ne.gs.questEngine.model.QuestDialog;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;
import com.ne.gs.services.QuestService;

public class FountainRewards extends QuestHandler {

    private final int questId;
    private final Set<Integer> startNpcs = new HashSet<>();

    public FountainRewards(int questId, List<Integer> startNpcIds) {
        super(questId);
        this.questId = questId;
        startNpcs.addAll(startNpcIds);
        startNpcs.remove(0);
    }

    public void register() {
        Iterator<Integer> iterator = startNpcs.iterator();
        while (iterator.hasNext()) {
            int startNpc = iterator.next();
            qe.registerQuestNpc(startNpc).addOnQuestStart(getQuestId());
            qe.registerQuestNpc(startNpc).addOnTalkEvent(getQuestId());
        }
    }

    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        int targetId = env.getTargetId();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        QuestDialog dialog = env.getDialog();

        if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
            if (startNpcs.contains(targetId)) {
                switch (dialog) {
                    case CHECK_COLLECTED_ITEMS:
                        if (!QuestService.inventoryItemCheck(env, true)) {
                            return true;
                        }
                        return sendQuestSelectionDialog(env);
                    case SELECT_REWARD:
                        if (QuestService.collectItemCheck(env, false)) {
                            if (!player.getInventory().isFull()) {
                                if (QuestService.startQuest(env)) {
                                    changeQuestStep(env, 0, 0, true);
                                    return sendQuestDialog(env, 5);
                                }
                            } else {
                                player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_FULL_INVENTORY);
                                return sendQuestSelectionDialog(env);
                            }
                        } else {
                            return sendQuestSelectionDialog(env);
                        }
                        break;
                }
            }
        } else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
            if (startNpcs.contains(targetId)) {
                if (dialog == QuestDialog.SELECT_NO_REWARD) {
                    if (QuestService.collectItemCheck(env, true)) {
                        return sendQuestEndDialog(env);
                    }
                } else {
                    return QuestService.abandonQuest(player, questId);
                }
            }
        }
        return false;
    }

    public boolean onCanAct(QuestEnv env, QuestActionType questEventType, Object... objects) {
        if (startNpcs.contains(env.getTargetId())) {
            return true;
        }
        return false;
    }
}
