/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.dataholders;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import gnu.trove.map.hash.TIntObjectHashMap;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.QuestTemplate;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.services.QuestService;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "quests")
public class QuestsData {

    @XmlElement(name = "quest", required = true)
    protected List<QuestTemplate> questsData;
    private final TIntObjectHashMap<QuestTemplate> questData = new TIntObjectHashMap<>();
    private final TIntObjectHashMap<List<QuestTemplate>> sortedByFactionId = new TIntObjectHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        questData.clear();
        sortedByFactionId.clear();
        for (QuestTemplate quest : questsData) {
            questData.put(quest.getId(), quest);
            int npcFactionId = quest.getNpcFactionId();
            if (npcFactionId == 0 || quest.isTimeBased()) {
                continue;
            }
            if (!sortedByFactionId.containsKey(npcFactionId)) {
                List<QuestTemplate> factionQuests = new ArrayList<>();
                factionQuests.add(quest);
                sortedByFactionId.put(npcFactionId, factionQuests);
            } else {
                sortedByFactionId.get(npcFactionId).add(quest);
            }
        }

        questsData = null;
    }

    public QuestTemplate getQuestById(int id) {
        return questData.get(id);
    }

    public List<QuestTemplate> getQuestsByNpcFaction(int npcFactionId, Player player) {
        List<QuestTemplate> factionQuests = sortedByFactionId.get(npcFactionId);
        List<QuestTemplate> quests = new ArrayList<>();
        QuestEnv questEnv = new QuestEnv(null, player, 0, 0);
        for (QuestTemplate questTemplate : factionQuests) {
            if (!QuestEngine.getInstance().isHaveHandler(questTemplate.getId())) {
                continue;
            }
            if (questTemplate.getMinlevelPermitted() != 0 && player.getLevel() < questTemplate.getMinlevelPermitted()) {
                continue;
            }
            questEnv.setQuestId(questTemplate.getId());
            if (QuestService.checkStartConditions(questEnv, false)) {
                quests.add(questTemplate);
            }
        }
        return quests;
    }

    public int size() {
        return questData.size();
    }

    /**
     * @return the questsData
     */
    public Collection<QuestTemplate> getQuestsData() {
        return questData.valueCollection();
    }

    /**
     * @param questsData
     *     the questsData to set
     */
    public void setQuestsData(Collection<QuestTemplate> questsData) {
        this.questsData = new ArrayList<>(questsData);
        afterUnmarshal(null, null);
    }
}
