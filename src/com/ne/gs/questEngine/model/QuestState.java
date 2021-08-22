/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.model;

import java.sql.Timestamp;
import java.util.*;

import com.ne.gs.questEngine.QuestEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.PersistentState;
import com.ne.gs.model.templates.QuestTemplate;
import com.ne.gs.model.templates.quest.QuestCategory;

/**
 * @author MrPoke
 * @modified vlog, Rolandas
 */
public class QuestState {

    private static final List<Integer> TenbitQuests = Arrays.asList(
            1842, 1843, 1844, 2843, 2844, 2845, 2737, //Простое и в то же время сложное задание
            41455, 41456, 41457, 41458, // защита сарфана
            41459, 41460, 41461, 41462); //защита тиамаранты

    private final int questId;
    private final QuestVars questVars;
    private QuestStatus status;
    private int completeCount;
    private Timestamp completeTime;
    private Timestamp nextRepeatTime;
    private Integer reward;
    private PersistentState persistentState;

    private static final Logger log = LoggerFactory.getLogger(QuestState.class);

    public QuestState(int questId, QuestStatus status, int questVars, int completeCount, Timestamp nextRepeatTime, Integer reward, Timestamp completeTime) {
        this.questId = questId;
        this.status = status;

        /* Temp solution for quests, that store variables as 10 bit values, not regular 6 bits */
        if (TenbitQuests.contains(questId))
            this.questVars = new QuestVars.QuestWarsTenbits(questVars);
        else
            this.questVars = new QuestVars(questVars);


        this.completeCount = completeCount;
        this.nextRepeatTime = nextRepeatTime;
        this.reward = reward;
        this.completeTime = completeTime;
        persistentState = PersistentState.NEW;
    }

    public QuestVars getQuestVars() {
        return questVars;
    }

    /**
     * @param id
     * @param var
     */
    public void setQuestVarById(int id, int var) {
        questVars.setVarById(id, var);
        setPersistentState(PersistentState.UPDATE_REQUIRED);
    }

    /**
     * @param id
     * @return Quest var by id.
     */
    public int getQuestVarById(int id) {
        return questVars.getVarById(id);
    }

    public void setQuestVar(int var) {
        questVars.setVar(var);
        setPersistentState(PersistentState.UPDATE_REQUIRED);
    }

    public QuestStatus getStatus() {
        return status;
    }

    public void setStatus(QuestStatus status) {
        if (status == QuestStatus.COMPLETE && this.status != QuestStatus.COMPLETE) {
            updateCompleteTime();
        }
        this.status = status;
        setPersistentState(PersistentState.UPDATE_REQUIRED);
    }

    public Timestamp getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Timestamp time) {
        completeTime = time;
    }

    public void updateCompleteTime() {
        completeTime = new Timestamp(Calendar.getInstance().getTimeInMillis());
    }

    public int getQuestId() {
        return questId;
    }

    public void setCompleteCount(int completeCount) {
        this.completeCount = completeCount;
        setPersistentState(PersistentState.UPDATE_REQUIRED);
    }

    public int getCompleteCount() {
        return completeCount;
    }

    public void setNextRepeatTime(Timestamp nextRepeatTime) {
        this.nextRepeatTime = nextRepeatTime;
    }

    public Timestamp getNextRepeatTime() {
        return nextRepeatTime;
    }

    public void setReward(Integer reward) {
        this.reward = reward;
        setPersistentState(PersistentState.UPDATE_REQUIRED);
    }

    public Integer getReward() {
        if (reward == null) {
            log.warn("No reward for the quest " + String.valueOf(questId));
        } else {
            return reward;
        }
        return 0;
    }

    public boolean canRepeat() {
        QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
        if (status != QuestStatus.NONE
                && (status != QuestStatus.COMPLETE || completeCount >= template.getMaxRepeatCount() && template.getMaxRepeatCount() != 255)) {
            return false;
        }
        if (questVars.getQuestVars() != 0) {
            return false;
        }
        if (template.isTimeBased() && nextRepeatTime != null) {
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            if (currentTime.before(nextRepeatTime)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the pState
     */
    public PersistentState getPersistentState() {
        return persistentState;
    }

    /**
     * @param persistentState the pState to set
     */
    public void setPersistentState(PersistentState persistentState) {
        switch (persistentState) {
            case DELETED:
                if (this.persistentState == PersistentState.NEW) {
                    break;
                }
            case UPDATE_REQUIRED:
                if (this.persistentState == PersistentState.NEW) {
                    break;
                }
            default:
                this.persistentState = persistentState;
        }
    }
}
