/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.quest;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.questEngine.QuestEngine;

/**
 * @author MrPoke
 */
public class QuestNpc {

    private static final Logger log = LoggerFactory.getLogger(QuestNpc.class);
    private final List<Integer> onQuestStart;
    private final List<Integer> onKillEvent;
    private final List<Integer> onTalkEvent;
    private final List<Integer> onAttackEvent;
    private final List<Integer> onLostTargetEvent;
    private final List<Integer> onReachTargetEvent;
    private final List<Integer> onAddAggroListEvent;
    private final int npcId;

    public QuestNpc(int npcId) {
        this.npcId = npcId;
        onQuestStart = new ArrayList<>(0);
        onKillEvent = new ArrayList<>(0);
        onTalkEvent = new ArrayList<>(0);
        onAttackEvent = new ArrayList<>(0);
        onLostTargetEvent = new ArrayList<>(0);
        onReachTargetEvent = new ArrayList<>(0);
        onAddAggroListEvent = new ArrayList<>(0);
    }

    private void registerCanAct(int questId, int npcId) {
        NpcTemplate template = DataManager.NPC_DATA.getNpcTemplate(npcId);
        if (template == null) {
            log.warn("[QuestEngine] No such NPC template for " + npcId + " in Q" + questId);
            return;
        }
        String aiName = DataManager.NPC_DATA.getNpcTemplate(npcId).getAi();
        if ("quest_use_item".equals(aiName)) {
            QuestEngine.getInstance().registerCanAct(questId, npcId);
        }
    }

    public void addOnQuestStart(int questId) {
        if (!onQuestStart.contains(questId)) {
            onQuestStart.add(questId);
        }
    }

    public List<Integer> getOnQuestStart() {
        return onQuestStart;
    }

    public void addOnAttackEvent(int questId) {
        if (!onAttackEvent.contains(questId)) {
            onAttackEvent.add(questId);
        }
    }

    public List<Integer> getOnAttackEvent() {
        return onAttackEvent;
    }

    public void addOnKillEvent(int questId) {
        if (!onKillEvent.contains(questId)) {
            onKillEvent.add(questId);
            registerCanAct(questId, npcId);
        }
    }

    public List<Integer> getOnKillEvent() {
        return onKillEvent;
    }

    public void addOnTalkEvent(int questId) {
        if (!onTalkEvent.contains(questId)) {
            onTalkEvent.add(questId);
            registerCanAct(questId, npcId);
        }
    }

    public List<Integer> getOnTalkEvent() {
        return onTalkEvent;
    }

    public void addOnReachTargetEvent(int questId) {
        if (!onReachTargetEvent.contains(questId)) {
            onReachTargetEvent.add(questId);
        }
    }

    public List<Integer> getOnReachTargetEvent() {
        return onReachTargetEvent;
    }

    public void addOnLostTargetEvent(int questId) {
        if (!onLostTargetEvent.contains(questId)) {
            onLostTargetEvent.add(questId);
        }
    }

    public List<Integer> getOnLostTargetEvent() {
        return onLostTargetEvent;
    }

    public int getNpcId() {
        return npcId;
    }

    public void addOnAddAggroListEvent(int questId) {
        if (!onAddAggroListEvent.contains(questId)) {
            onAddAggroListEvent.add(questId);
            registerCanAct(questId, npcId);
        }
    }

    public List<Integer> getOnAddAggroListEvent() {
        return onAddAggroListEvent;
    }
}
