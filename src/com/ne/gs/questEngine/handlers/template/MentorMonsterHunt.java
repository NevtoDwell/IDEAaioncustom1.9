/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers.template;

import java.util.List;
import javolution.util.FastMap;

import com.ne.gs.configs.main.GroupConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.model.templates.QuestTemplate;
import com.ne.gs.questEngine.handlers.models.Monster;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;
import com.ne.gs.utils.MathUtil;

/**
 * @author MrPoke
 */
public class MentorMonsterHunt extends MonsterHunt {

    private int menteMinLevel;
    private int menteMaxLevel;
    private QuestTemplate qt;

    /**
     * @param questId
     * @param monsters
     */
    public MentorMonsterHunt(int questId, List<Integer> startNpcIds, List<Integer> endNpcIds, List<Integer> aggroStartNpcs, FastMap<List<Integer>, Monster> monsters, int menteMinLevel, int menteMaxLevel) {
        super(questId, startNpcIds, endNpcIds, aggroStartNpcs, monsters);
        this.menteMinLevel = menteMinLevel;
        this.menteMaxLevel = menteMaxLevel;
        this.qt = DataManager.QUEST_DATA.getQuestById(questId);
    }

    @Override
    public boolean onKillEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(getQuestId());
        if (qs != null && qs.getStatus() == QuestStatus.START) {
            switch (qt.getMentorType()) {
                case MENTOR:
                    if (player.isMentor()) {
                        PlayerGroup group = player.getPlayerGroup2();
                        for (Player member : group.getMembers()) {
                            if (member.getLevel() >= menteMinLevel && member.getLevel() <= menteMaxLevel && MathUtil.getDistance(player, member) < GroupConfig.GROUP_MAX_DISTANCE) {
                                return super.onKillEvent(env);
                            }
                        }
                    }
                    break;
                case MENTE:
                    if (player.isInGroup2()) {
                        PlayerGroup group = player.getPlayerGroup2();
                        for (Player member : group.getMembers()) {
                            if (member.isMentor() && MathUtil.getDistance(player, member) < GroupConfig.GROUP_MAX_DISTANCE) {
                                return super.onKillEvent(env);
                            }
                        }
                    }
            }
        }
        return false;
    }
}
