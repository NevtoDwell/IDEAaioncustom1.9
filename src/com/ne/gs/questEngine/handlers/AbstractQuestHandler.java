/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers;

import java.util.List;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.quest.QuestItems;
import com.ne.gs.model.templates.rewards.BonusType;
import com.ne.gs.questEngine.model.QuestActionType;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;
import com.ne.gs.world.zone.ZoneName;

/**
 * The methods will be overridden in concrete quest handlers
 *
 * @author vlog
 */
public abstract class AbstractQuestHandler {

    public abstract void register();

    public boolean onDialogEvent(QuestEnv questEnv) {
        return false;
    }

    public boolean onEnterWorldEvent(QuestEnv questEnv) {
        return false;
    }

    public boolean onEnterZoneEvent(QuestEnv questEnv, ZoneName zoneName) {
        return false;
    }

    public boolean onAddAggroListEvent(QuestEnv questEnv) {
        return false;
    }

    public boolean onLeaveZoneEvent(QuestEnv questEnv, ZoneName zoneName) {
        return false;
    }

    public HandlerResult onItemUseEvent(QuestEnv questEnv, Item item) {
        return HandlerResult.UNKNOWN;
    }

    public boolean onHouseItemUseEvent(QuestEnv env, int itemId) {
        return false;
    }

    public boolean onGetItemEvent(QuestEnv questEnv) {
        return false;
    }

    public boolean onUseSkillEvent(QuestEnv questEnv, int skillId) {
        return false;
    }

    public boolean onKillEvent(QuestEnv questEnv) {
        return false;
    }

    public boolean onAttackEvent(QuestEnv questEnv) {
        return false;
    }

    public boolean onLvlUpEvent(QuestEnv questEnv) {
        return false;
    }

    public boolean onZoneMissionEndEvent(QuestEnv env) {
        return false;
    }

    public boolean onDieEvent(QuestEnv questEnv) {
        return false;
    }

    public boolean onLogOutEvent(QuestEnv env) {
        return false;
    }

    public boolean onNpcReachTargetEvent(QuestEnv env) {
        return false;
    }

    public boolean onNpcLostTargetEvent(QuestEnv env) {
        return false;
    }

    public boolean onMovieEndEvent(QuestEnv questEnv, int movieId) {
        return false;
    }

    public boolean onQuestTimerEndEvent(QuestEnv questEnv) {
        return false;
    }

    public boolean onInvisibleTimerEndEvent(QuestEnv questEnv) {
        return false;
    }

    public boolean onPassFlyingRingEvent(QuestEnv questEnv, String flyingRing) {
        return false;
    }

    public boolean onKillRankedEvent(QuestEnv env) {
        return false;
    }

    public boolean onKillInWorldEvent(QuestEnv env) {
        return false;
    }

    public boolean onFailCraftEvent(QuestEnv env, int itemId) {
        return false;
    }

    public boolean onEquipItemEvent(QuestEnv env, int itemId) {
        return false;
    }

    public boolean onCanAct(QuestEnv env, QuestActionType questEventType, Object... objects) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(env.getQuestId());
        return (qs != null) && (qs.getStatus() == QuestStatus.START);
    }

    public boolean onDredgionRewardEvent(QuestEnv env) {
        return false;
    }

    public HandlerResult onBonusApplyEvent(QuestEnv env, BonusType bonusType, List<QuestItems> rewardItems) {
        return HandlerResult.UNKNOWN;
    }
}
