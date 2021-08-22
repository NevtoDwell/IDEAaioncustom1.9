/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers.template;

import javolution.util.FastMap;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.questEngine.handlers.QuestHandler;
import com.ne.gs.questEngine.handlers.models.QuestSkillData;
import com.ne.gs.questEngine.model.QuestDialog;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;

/**
 * @author vlog
 */
public class SkillUse extends QuestHandler {

    private final int questId;
    private final int startNpc;
    private final int endNpc;
    private final FastMap<Integer, QuestSkillData> qsd;

    public SkillUse(int questId, int startNpc, int endNpc, FastMap<Integer, QuestSkillData> qsd) {
        super(questId);
        this.questId = questId;
        this.startNpc = startNpc;
        if (endNpc != 0) {
            this.endNpc = endNpc;
        } else {
            this.endNpc = startNpc;
        }
        this.qsd = qsd;
    }

    @Override
    public void register() {
        qe.registerQuestNpc(startNpc).addOnQuestStart(questId);
        qe.registerQuestNpc(startNpc).addOnTalkEvent(questId);
        if (endNpc != startNpc) {
            qe.registerQuestNpc(endNpc).addOnTalkEvent(questId);
        }
        for (int skillId : qsd.keySet()) {
            qe.registerQuestSkill(skillId, questId);
        }
    }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        QuestDialog dialog = env.getDialog();
        int targetId = env.getTargetId();

        if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
            if (targetId == startNpc) {
                if (dialog == QuestDialog.START_DIALOG) {
                    return sendQuestDialog(env, 4762);
                } else {
                    return sendQuestStartDialog(env);
                }
            }
        } else if (qs.getStatus() == QuestStatus.START) {
            int var = qs.getQuestVarById(0);
            if (targetId == endNpc) {
                if (dialog == QuestDialog.START_DIALOG) {
                    return sendQuestDialog(env, 10002);
                } else if (dialog == QuestDialog.SELECT_REWARD) {
                    changeQuestStep(env, var, var, true); // reward
                    return sendQuestDialog(env, 5);
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == endNpc) {
                return sendQuestEndDialog(env);
            }
        }
        return false;
    }

    @Override
    public boolean onUseSkillEvent(QuestEnv env, int skillId) {
        return defaultOnUseSkillEvent(env, qsd.get(skillId).getStartVar(), qsd.get(skillId).getEndVar(),
            qsd.get(skillId).getVarNum());
    }
}
