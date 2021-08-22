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

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.questEngine.handlers.QuestHandler;
import com.ne.gs.questEngine.handlers.models.Monster;
import com.ne.gs.questEngine.model.QuestDialog;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;
import com.ne.gs.services.QuestService;

/**
 * @author MrPoke
 * @reworked vlog
 */
public class MonsterHunt extends QuestHandler {

    private final int questId;
    private final Set<Integer> startNpcs = new HashSet<>();
    private final Set<Integer> endNpcs = new HashSet<>();
    private final Set<Integer> aggroNpcs = new HashSet<>();
    private final FastMap<List<Integer>, Monster> monsters;

    public MonsterHunt(int questId, List<Integer> startNpcIds, List<Integer> endNpcIds,
            List<Integer> aggroStartNpcsIds, FastMap<List<Integer>, Monster> monsters) {
        super(questId);
        this.questId = questId;
        startNpcs.addAll(startNpcIds);
        startNpcs.remove(0);
        if (endNpcIds == null) {
            endNpcs.addAll(startNpcs);
        } else {
            endNpcs.addAll(endNpcIds);
            endNpcs.remove(0);
        }
        if (aggroStartNpcsIds != null) {
            aggroNpcs.addAll(aggroStartNpcsIds);
            aggroNpcs.remove(0);
        }
        this.monsters = monsters;
    }

    @Override
    public void register() {
        Iterator<Integer> iterator = startNpcs.iterator();
        while (iterator.hasNext()) {
            int startNpc = iterator.next();
            qe.registerQuestNpc(startNpc).addOnQuestStart(getQuestId());
            qe.registerQuestNpc(startNpc).addOnTalkEvent(getQuestId());
        }
        for (List<Integer> monsterIds : monsters.keySet()) {
            iterator = monsterIds.iterator();
            while (iterator.hasNext()) {
                int monsterId = iterator.next();
                qe.registerQuestNpc(monsterId).addOnKillEvent(questId);
            }
        }
        for (Integer aggroNpc : aggroNpcs) {
            qe.registerQuestNpc(aggroNpc).addOnAddAggroListEvent(getQuestId());
        }
        iterator = endNpcs.iterator();
        while (iterator.hasNext()) {
            int endNpc = iterator.next();
            qe.registerQuestNpc(endNpc).addOnTalkEvent(getQuestId());
        }
    }

    @Override
    public boolean onAddAggroListEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
            QuestService.startQuest(env);
            return true;
        }
        return false;
    }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        int targetId = env.getTargetId();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
            if ((startNpcs.isEmpty()) || (startNpcs.contains(targetId))) {
                if (env.getDialog() == QuestDialog.START_DIALOG) {
                    return sendQuestDialog(env, 1011);
                } else {
                    return sendQuestStartDialog(env);
                }
            }
        } else if (qs.getStatus() == QuestStatus.START) {
            for (Monster mi : monsters.values()) {
                if (mi.getEndVar() > qs.getQuestVarById(mi.getVar())) {
                    return false;
                }
            }
            if (endNpcs.contains(targetId)) {
                if (env.getDialog() == QuestDialog.START_DIALOG) {
                    return sendQuestDialog(env, 1352);
                } else if (env.getDialog() == QuestDialog.SELECT_REWARD) {
                    qs.setStatus(QuestStatus.REWARD);
                    updateQuestStatus(env);
                    return sendQuestDialog(env, 5);
                }
            }
        } else if ((qs.getStatus() == QuestStatus.REWARD) && (endNpcs.contains(targetId))) {
            return sendQuestEndDialog(env);
        }

        return false;
    }

    @Override
    public boolean onKillEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs != null && qs.getStatus() == QuestStatus.START) {
            for (Monster m : monsters.values()) {
                if (m.getNpcIds().contains(env.getTargetId()) && qs.getQuestVarById(m.getVar()) < m.getEndVar()) {
                    if (!aggroNpcs.isEmpty()) {
                        qs.setStatus(QuestStatus.REWARD);
                        updateQuestStatus(env);
                    } else {
                        qs.setQuestVarById(m.getVar(), qs.getQuestVarById(m.getVar()) + 1);
                        updateQuestStatus(env);
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
