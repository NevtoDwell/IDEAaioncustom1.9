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
import gnu.trove.list.array.TIntArrayList;
import javolution.util.FastMap;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.spawns.SpawnSearchResult;
import com.ne.gs.questEngine.handlers.QuestHandler;
import com.ne.gs.questEngine.handlers.models.Monster;
import com.ne.gs.questEngine.handlers.models.SpawnedMonster;
import com.ne.gs.questEngine.model.QuestDialog;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;
import com.ne.gs.services.QuestService;

/**
 * @author vlog
 */
public class KillSpawned extends QuestHandler {

    private final int questId;
    private final Set<Integer> startNpcs = new HashSet<>();
    private final Set<Integer> endNpcs = new HashSet<>();
    private final FastMap<List<Integer>, SpawnedMonster> spawnedMonsters;
    private final TIntArrayList spawnerObjects;

    public KillSpawned(int questId, List<Integer> startNpcIds, List<Integer> endNpcIds, FastMap<List<Integer>, SpawnedMonster> spawnedMonsters) {
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
        this.spawnedMonsters = spawnedMonsters;
        this.spawnerObjects = new TIntArrayList();
        for (SpawnedMonster m : spawnedMonsters.values()) {
            spawnerObjects.add(m.getSpawnerObject());
        }
    }

    @Override
    public void register() {
        Iterator<Integer> iterator = startNpcs.iterator();
        while (iterator.hasNext()) {
            int startNpc = iterator.next();
            qe.registerQuestNpc(startNpc).addOnQuestStart(getQuestId());
            qe.registerQuestNpc(startNpc).addOnTalkEvent(getQuestId());
        }
        for (List<Integer> spawnedMonsterIds : spawnedMonsters.keySet()) {
            iterator = spawnedMonsterIds.iterator();
            while (iterator.hasNext()) {
                int spawnedMonsterId = iterator.next().intValue();
                qe.registerQuestNpc(spawnedMonsterId).addOnKillEvent(questId);
            }
        }
        iterator = endNpcs.iterator();
        while (iterator.hasNext()) {
            int endNpc = iterator.next();
            qe.registerQuestNpc(endNpc).addOnTalkEvent(getQuestId());
        }
        for (int i = 0; i < spawnerObjects.size(); i++) {
            qe.registerQuestNpc(spawnerObjects.get(i)).addOnTalkEvent(questId);
        }
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
                }
                return sendQuestStartDialog(env);
            }
        } else if (qs.getStatus() == QuestStatus.START) {
            if (spawnerObjects.contains(targetId)) {
                if (env.getDialog() == QuestDialog.USE_OBJECT) {
                    int monsterId = 0;
                    for (SpawnedMonster m : spawnedMonsters.values()) {
                        if (m.getSpawnerObject() == targetId) {
                            monsterId = m.getNpcIds().get(0);
                            break;
                        }
                    }

                    SpawnSearchResult searchResult = DataManager.SPAWNS_DATA2.getFirstSpawnByNpcId(player.getWorldId(), targetId);
                    QuestService
                        .addNewSpawn(player.getWorldId(), player.getInstanceId(), monsterId, searchResult.getSpot().getX(), searchResult.getSpot().getY(), searchResult.getSpot().getZ(), searchResult
                            .getSpot().getHeading());

                    return true;
                }
            } else {
                for (Monster mi : spawnedMonsters.values()) {
                    if (mi.getEndVar() > qs.getQuestVarById(mi.getVar())) {
                        return false;
                    }
                }
                if (endNpcs.contains(targetId)) {
                    if (env.getDialog() == QuestDialog.START_DIALOG) {
                        return sendQuestDialog(env, 10002);
                    }
                    if (env.getDialog() == QuestDialog.SELECT_REWARD) {
                        return sendQuestDialog(env, 5);
                    }
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
            for (SpawnedMonster m : spawnedMonsters.values()) {
                if (m.getNpcIds().contains(env.getTargetId()) && qs.getQuestVarById(m.getVar()) < m.getEndVar()) {
                    qs.setQuestVarById(m.getVar(), qs.getQuestVarById(m.getVar()) + 1);
                    for (Monster mi : spawnedMonsters.values()) {
                        if (qs.getQuestVarById(mi.getVar()) < mi.getEndVar()) {
                            updateQuestStatus(env);
                            return true;
                        }
                    }
                    qs.setStatus(QuestStatus.REWARD);
                    updateQuestStatus(env);
                    return true;
                }
            }
        }
        return false;
    }
}
