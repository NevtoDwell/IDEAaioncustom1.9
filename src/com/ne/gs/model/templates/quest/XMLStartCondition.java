/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.quest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.QuestStateList;
import com.ne.gs.model.templates.QuestTemplate;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;

/**
 * Checks quest start conditions, listed in quest_data.xml
 *
 * @author antness
 * @reworked vlog
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestStartConditions")
public class XMLStartCondition {

    @XmlElement(name = "finished")
    protected List<FinishedQuestCond> finished;
    @XmlList
    @XmlElement(name = "unfinished", type = Integer.class)
    protected List<Integer> unfinished;
    @XmlList
    @XmlElement(name = "noacquired", type = Integer.class)
    protected List<Integer> noacquired;
    @XmlList
    @XmlElement(name = "acquired", type = Integer.class)
    protected List<Integer> acquired;
    @XmlList
    @XmlElement(name = "equipped", type = Integer.class)
    protected List<Integer> equipped;

    /**
     * Check, if the player has finished listed quests
     */
    private boolean checkFinishedQuests(QuestStateList qsl) {
        if (finished != null && finished.size() > 0) {
            for (FinishedQuestCond fqc : finished) {
                int questId = fqc.getQuestId();
                int reward = fqc.getReward();
                QuestState qs = qsl.getQuestState(questId);
                if (qs == null || qs.getStatus() != QuestStatus.COMPLETE || !checkReward(questId, reward, qs.getReward())) {
                    return false;
                }
                QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
                if (template != null && template.isRepeatable()) {
                    if (qs.getCompleteCount() != template.getMaxRepeatCount()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Check, if the player has not finished listed quests
     */
    private boolean checkUnfinishedQuests(QuestStateList qsl) {
        if (unfinished != null && unfinished.size() > 0) {
            for (Integer questId : unfinished) {
                QuestState qs = qsl.getQuestState(questId);
                if (qs != null && qs.getStatus() == QuestStatus.COMPLETE) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check, if the player has not acquired listed quests
     */
    private boolean checkNoAcquiredQuests(QuestStateList qsl) {
        if (noacquired != null && noacquired.size() > 0) {
            for (Integer questId : noacquired) {
                QuestState qs = qsl.getQuestState(questId);
                if (qs != null
                    && (qs.getStatus() == QuestStatus.START || qs.getStatus() == QuestStatus.REWARD || qs.getStatus() == QuestStatus.COMPLETE)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check, if the player has acquired listed quests
     */
    private boolean checkAcquiredQuests(QuestStateList qsl) {
        if (acquired != null && acquired.size() > 0) {
            for (Integer questId : acquired) {
                QuestState qs = qsl.getQuestState(questId);
                if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.getStatus() == QuestStatus.LOCKED) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkEquippedItems(Player player, boolean warn) {
        if (!warn) {
            return true;
        }
        if (equipped != null && equipped.size() > 0) {
            for (int itemId : equipped) {
                if (!player.getEquipment().getEquippedItemIds().contains(itemId)) {
                    int requiredItemNameId = DataManager.ITEM_DATA.getItemTemplate(itemId).getNameId();
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_EQUIP_ITEM(DescId.of(requiredItemNameId)));
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check all conditions
     */
    public boolean check(Player player, boolean warn) {
        QuestStateList qsl = player.getQuestStateList();
        return checkFinishedQuests(qsl) && checkUnfinishedQuests(qsl) && checkAcquiredQuests(qsl)
            && checkNoAcquiredQuests(qsl) && checkEquippedItems(player, warn);
    }

    private boolean checkReward(int questId, int neededReward, int currentReward) {
        // Temporary exceptions-quests till abyss entry quests work with correct reward
        if (neededReward != currentReward && questId != 2947 && questId != 1922) {
            return false;
        }
        return true;
    }

    public List<FinishedQuestCond> getFinishedPreconditions() {
        return finished;
    }
}
