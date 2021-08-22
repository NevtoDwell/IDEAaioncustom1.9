/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Future;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.EventType;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.QuestTemplate;
import com.ne.gs.model.templates.event.EventTemplate;
import com.ne.gs.model.templates.quest.XMLStartCondition;
import com.ne.gs.network.aion.serverpackets.SM_QUEST_ACTION;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author Rolandas
 */
public class EventService {

    Logger log = LoggerFactory.getLogger(EventService.class);

    private final int CHECK_TIME_PERIOD = 1000 * 60 * 5;

    private boolean isStarted = false;

    private Future<?> checkTask = null;

    private final List<EventTemplate> activeEvents;

    TIntObjectHashMap<List<EventTemplate>> eventsForStartQuest = new TIntObjectHashMap<>();

    TIntObjectHashMap<List<EventTemplate>> eventsForMaintainQuest = new TIntObjectHashMap<>();

    private static final class SingletonHolder {

        protected static final EventService instance = new EventService();
    }

    public static EventService getInstance() {
        return SingletonHolder.instance;
    }

    private EventService() {
        activeEvents = Collections.synchronizedList(DataManager.EVENT_DATA.getActiveEvents());
        updateQuestMap();
    }

    /**
     * This method is called just after player logged in to the game.<br>
     * <br>
     * <b><font color='red'>NOTICE: </font>This method must not be called from anywhere else.</b>
     */
    public void onPlayerLogin(Player player) {
        List<Integer> activeStartQuests = new ArrayList<>();
        List<Integer> activeMaintainQuests = new ArrayList<>();
        TIntObjectHashMap<List<EventTemplate>> map1 = null;
        TIntObjectHashMap<List<EventTemplate>> map2 = null;

        synchronized (activeEvents) {
            for (EventTemplate et : activeEvents) {
                if (et.isActive()) {
                    activeStartQuests.addAll(et.getStartableQuests());
                    activeMaintainQuests.addAll(et.getMaintainableQuests());
                }
            }
            map1 = new TIntObjectHashMap<>(eventsForStartQuest);
            map2 = new TIntObjectHashMap<>(eventsForMaintainQuest);
        }

        StartOrMaintainQuests(player, activeStartQuests.listIterator(), map1, true);
        StartOrMaintainQuests(player, activeMaintainQuests.listIterator(), map2, false);

        activeStartQuests.clear();
        activeMaintainQuests.clear();
        map1.clear();
        map2.clear();
    }

    void StartOrMaintainQuests(Player player, ListIterator<Integer> questList, TIntObjectHashMap<List<EventTemplate>> templateMap,
                               boolean start) {
        while (questList.hasNext()) {
            int questId = questList.next();
            QuestState qs = player.getQuestStateList().getQuestState(questId);
            QuestEnv cookie = new QuestEnv(null, player, questId, 0);
            QuestStatus status = qs == null ? QuestStatus.START : qs.getStatus();

            if (QuestService.checkLevelRequirement(questId, player.getCommonData().getLevel())) {
                QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
                if (template.getRacePermitted() != null) {
                    if (template.getRacePermitted().ordinal() != player.getCommonData().getRace().ordinal()) {
                        continue;
                    }
                }

                if (template.getClassPermitted().size() != 0) {
                    if (!template.getClassPermitted().contains(player.getCommonData().getPlayerClass())) {
                        continue;
                    }
                }

                if (template.getGenderPermitted() != null) {
                    if (template.getGenderPermitted().ordinal() != player.getGender().ordinal()) {
                        continue;
                    }
                }

                int amountOfStartConditions = template.getXMLStartConditions().size();
                int fulfilledStartConditions = 0;
                if (amountOfStartConditions != 0) {
                    for (XMLStartCondition startCondition : template.getXMLStartConditions()) {
                        if (startCondition.check(player, false)) {
                            fulfilledStartConditions++;
                        }
                    }
                    if (fulfilledStartConditions < 1) {
                        continue;
                    }
                }

                if (qs != null) {
                    if (qs.getCompleteTime() != null || status == QuestStatus.COMPLETE) {
                        DateTime completed = null;
                        if (qs.getCompleteTime() == null) {
                            completed = new DateTime(0);
                        } else {
                            completed = new DateTime(qs.getCompleteTime().getTime());
                        }

                        if (templateMap.containsKey(questId)) {
                            for (EventTemplate et : templateMap.get(questId)) {
                                // recurring event, reset it
                                if (et.getStartDate().isAfter(completed)) {
                                    if (start) {
                                        status = QuestStatus.START;
                                        qs.setQuestVar(0);
                                        qs.setCompleteCount(0);
                                        qs.setStatus(status);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    // re-register quests
                    if (status == QuestStatus.COMPLETE) {
                        player.sendPck(new SM_QUEST_ACTION(questId, status, qs.getQuestVars().getQuestVars()));
                    } else {
                        QuestService.startEventQuest(cookie, status);
                    }
                } else if (start) {
                    QuestService.startEventQuest(cookie, status);
                }
            }
        }
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void start() {
        if (isStarted) {
            checkTask.cancel(false);
        }

        isStarted = true;

        checkTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                checkEvents();
            }
        }, 0, CHECK_TIME_PERIOD);
    }

    public void stop() {
        if (isStarted) {
            checkTask.cancel(false);
        }
        checkTask = null;
        isStarted = false;
    }

    private void checkEvents() {
        List<EventTemplate> newEvents = new ArrayList<>();
        List<EventTemplate> allEvents = DataManager.EVENT_DATA.getAllEvents();

        for (EventTemplate et : allEvents) {
            if (et.isActive()) {
                newEvents.add(et);
                et.Start();
            }
        }

        synchronized (activeEvents) {
            for (EventTemplate et : activeEvents) {
                if (et.isExpired() || !DataManager.EVENT_DATA.Contains(et.getName())) {
                    et.Stop();
                }
            }

            activeEvents.clear();
            eventsForStartQuest.clear();
            eventsForMaintainQuest.clear();
            activeEvents.addAll(newEvents);
            updateQuestMap();
        }

        newEvents.clear();
        allEvents.clear();
    }

    private void updateQuestMap() {
        for (EventTemplate et : activeEvents) {
            for (int qId : et.getStartableQuests()) {
                if (!eventsForStartQuest.containsKey(qId)) {
                    eventsForStartQuest.put(qId, new ArrayList<EventTemplate>());
                }
                eventsForStartQuest.get(qId).add(et);
            }
            for (int qId : et.getMaintainableQuests()) {
                if (!eventsForMaintainQuest.containsKey(qId)) {
                    eventsForMaintainQuest.put(qId, new ArrayList<EventTemplate>());
                }
                eventsForMaintainQuest.get(qId).add(et);
            }
        }
    }

    public boolean checkQuestIsActive(int questId) {
        synchronized (activeEvents) {
            if (eventsForStartQuest.containsKey(questId) || eventsForMaintainQuest.containsKey(questId)) {
                return true;
            }
        }
        return false;
    }

    public EventType getEventType() {
        for (EventTemplate et : activeEvents) {
            String theme = et.getTheme();
            if (theme != null) {
                EventType type = EventType.getEventType(theme);
                if (et.isActive() && !type.equals(EventType.NONE)) {
                    return type;
                }
            }
        }
        return EventType.NONE;
    }
}
