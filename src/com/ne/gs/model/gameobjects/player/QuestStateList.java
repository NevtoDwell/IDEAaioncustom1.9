/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;
import javolution.util.FastList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.dataholders.QuestsData;
import com.ne.gs.model.templates.quest.QuestCategory;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;

/**
 * @author MrPoke
 */
public class QuestStateList {

    private static final Logger log = LoggerFactory.getLogger(QuestStateList.class);

    private final SortedMap<Integer, QuestState> _quests;
    private final QuestsData _questData = DataManager.QUEST_DATA;

    /**
     * Creates an empty quests list
     */
    public QuestStateList() {
        _quests = new TreeMap<>();
    }

    public synchronized boolean addQuest(int questId, QuestState questState) {
        if (_quests.containsKey(questId)) {
            log.warn("Duplicate quest. ");
            return false;
        }
        _quests.put(questId, questState);
        return true;
    }

    public synchronized boolean removeQuest(int questId) {
        if (_quests.containsKey(questId)) {
            _quests.remove(questId);
            return true;
        }
        return false;
    }

    public QuestState getQuestState(int questId) {
        return _quests.get(questId);
    }

    public QuestStatus getQuestStatus(int questId) {
        QuestState qs = _quests.get(questId);
        return qs != null ? qs.getStatus() : QuestStatus.NONE;
    }

    public Collection<QuestState> getAllQuestState() {
        return _quests.values();
    }

    public FastList<QuestState> getAllFinishedQuests() {
        FastList<QuestState> completeQuestList = FastList.newInstance();
        for (QuestState qs : _quests.values()) {
            if (qs.getStatus() == QuestStatus.COMPLETE) {
                completeQuestList.add(qs);
            }
        }
        return completeQuestList;
    }

    /*
     * Issue #13 fix Used by the QuestService to check the amount of normal quests in the player's list
     * @author vlog
     */
    public int getNormalQuestListSize() {
        return getNormalQuests().size();
    }

    /*
     * Issue #13 fix Returns the list of normal quests
     * @author vlog
     */
    public Collection<QuestState> getNormalQuests() {
        Collection<QuestState> l = new ArrayList<>();

        for (QuestState qs : getAllQuestState()) {
            QuestCategory qc = _questData.getQuestById(qs.getQuestId()).getCategory();
            String name = _questData.getQuestById(qs.getQuestId()).getName();
            QuestStatus s = qs.getStatus();

            if (s != QuestStatus.COMPLETE && s != QuestStatus.LOCKED && s != QuestStatus.NONE && (qc == QuestCategory.FACTION || qc == QuestCategory.SEEN_MARKER ||
                    qc == QuestCategory.SIGNIFICANT || qc == QuestCategory.QUEST || qc == QuestCategory.IMPORTANT || qc == QuestCategory.EVENT)) {
                l.add(qs);
            }
        }
        return l;
    }

    /*
     * Returns true if there is a quest in the list with this id Used by the QuestService
     * @author vlog
     */
    public boolean hasQuest(int questId) {
        return _quests.containsKey(questId);
    }

    /*
     * Change the old value of the quest status to the new one Used by the QuestService
     * @author vlog
     */
    public void changeQuestStatus(Integer key, QuestStatus newStatus) {
        _quests.get(key).setStatus(newStatus);
    }

    public int size() {
        return _quests.size();
    }

    public SortedMap<Integer, QuestState> getQuests() {
        return _quests;
    }
}
