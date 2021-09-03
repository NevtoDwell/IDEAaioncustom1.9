/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player.npcFaction;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ne.commons.utils.Rnd;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.DescId;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.skill.PlayerSkillEntry;
import com.ne.gs.model.templates.QuestTemplate;
import com.ne.gs.model.templates.factions.FactionCategory;
import com.ne.gs.model.templates.factions.NpcFactionTemplate;
import com.ne.gs.model.templates.quest.QuestMentorType;
import com.ne.gs.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.ne.gs.network.aion.serverpackets.SM_QUEST_ACTION;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.network.aion.serverpackets.SM_TITLE_INFO;
import com.ne.gs.services.QuestService;
import com.ne.gs.services.craft.CraftSkillUpdateService;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author MrPoke
 */
public class NpcFactions {

    private final Player owner;

    private final Map<Integer, NpcFaction> factions = new HashMap<>();
    private final NpcFaction[] activeNpcFaction = new NpcFaction[2];
    private final int[] timeLimit = new int[]{0, 0};

    /**
     * @param owner
     */
    public NpcFactions(Player owner) {
        this.owner = owner;
    }

    public void addNpcFaction(NpcFaction faction) {
        factions.put(faction.getId(), faction);
        int type = 0;
        if (faction.isMentor()) {
            type = 1;
        }

        if (faction.isActive()) {
            activeNpcFaction[type] = faction;
        }
        if (timeLimit[type] < faction.getTime() && faction.getState() == ENpcFactionQuestState.COMPLETE) {
            timeLimit[type] = faction.getTime();
        }
    }

    public NpcFaction getNpcFactinById(int id) {
        return factions.get(id);
    }

    public Collection<NpcFaction> getNpcFactions() {
        return factions.values();
    }

    public NpcFaction getActiveNpcFaction(boolean mentor) {
        if (mentor) {
            return activeNpcFaction[1];
        } else {
            return activeNpcFaction[0];
        }
    }

    public NpcFaction setActive(int npcFactionId) {
        NpcFaction npcFaction = factions.get(npcFactionId);
        if (npcFaction == null) {
            npcFaction = new NpcFaction(npcFactionId, 0, false, ENpcFactionQuestState.NOTING, 0);
            factions.put(npcFactionId, npcFaction);
        }
        npcFaction.setActive(true);
        if (npcFaction.isMentor()) {
            activeNpcFaction[1] = npcFaction;
        } else {
            activeNpcFaction[0] = npcFaction;
        }
        return npcFaction;
    }

    public void leaveNpcFaction(Npc npc) {
        int targetObjectId = npc.getObjectId();
        NpcFactionTemplate npcFactionTemplate = DataManager.NPC_FACTIONS_DATA.getNpcFactionByNpcId(npc.getNpcId());
        if (npcFactionTemplate == null) {
            return;
        }
        NpcFaction npcFaction = getNpcFactinById(npcFactionTemplate.getId());
        if (npcFaction == null || !npcFaction.isActive()) {
            owner.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 1438));
            return;
        }

        owner.sendPck(new SM_SYSTEM_MESSAGE(1300526, DescId.of(npcFactionTemplate.getNameId())));
        owner.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 1353));

        npcFaction.setActive(false);
        activeNpcFaction[npcFactionTemplate.isMentor() ? 1 : 0] = null;
        if (npcFaction.getState() == ENpcFactionQuestState.START) {
            QuestService.abandonQuest(owner, npcFaction.getQuestId());
            npcFaction.setState(ENpcFactionQuestState.NOTING);
        }
    }

    public void enterGuild(Npc npc) {
        int targetObjectId = npc.getObjectId();
        NpcFactionTemplate npcFactionTemplate = DataManager.NPC_FACTIONS_DATA.getNpcFactionByNpcId(npc.getNpcId());
        if (npcFactionTemplate == null) {
            return;
        }
        NpcFaction npcFaction = getNpcFactinById(npcFactionTemplate.getId());
        int npcFactionId = npcFactionTemplate.getId();
        int skillPoints = npcFactionTemplate.getSkillPoints();
        if (skillPoints != 0) {
            boolean canEnter = false;
            if (npcFactionTemplate.getCategory() == FactionCategory.COMBINESKILL) {
                for (PlayerSkillEntry skill : owner.getSkillList().getAllSkills()) {
                    if (CraftSkillUpdateService.getInstance().isCraftingSkill(skill.getSkillId()) && skill.getSkillLevel() >= skillPoints) {
                        canEnter = true;
                        break;
                    }
                }
            }
            if (!canEnter) {
                owner.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 1098));
                return;
            }
        }
        if (owner.getLevel() < npcFactionTemplate.getMinLevel() || owner.getLevel() > npcFactionTemplate.getMaxLevel()) {
            owner.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 1182));
            return;
        }
        if (owner.getRace() != npcFactionTemplate.getRace() && !npcFactionTemplate.getRace().equals(Race.NPC)) {
            owner.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 1097));
            return;
        }
        if (npcFaction != null && npcFaction.isActive()) {
            owner.sendPck(new SM_SYSTEM_MESSAGE(1300525));
            return;
        }
        NpcFaction activeNpcFaction = getActiveNpcFaction(npcFactionTemplate.isMentor());
        if (activeNpcFaction != null && activeNpcFaction.getId() != npcFactionId) {
            owner.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 1267));
            return;
        }
        if (npcFaction == null || !npcFaction.isActive()) {
            owner.sendPck(new SM_SYSTEM_MESSAGE(1300524, DescId.of(npcFactionTemplate.getNameId())));
            owner.sendPck(new SM_DIALOG_WINDOW(targetObjectId, 1012));
            setActive(npcFactionId);

            sendDailyQuest();
        }
    }

    public void startQuest(QuestTemplate questTemplate) {
        NpcFaction npcFaction = activeNpcFaction[questTemplate.isMentor() ? 1 : 0];
        if (npcFaction == null) {
            return;
        }
        if (npcFaction.getState() != ENpcFactionQuestState.NOTING && npcFaction.getQuestId() == 0) {
            return;
        }
        npcFaction.setState(ENpcFactionQuestState.START);
    }

    public void abortQuest(QuestTemplate questTemplate) {
        NpcFaction npcFaction = factions.get(questTemplate.getNpcFactionId());
        if (npcFaction == null || !npcFaction.isActive()) {
            return;
        }
        npcFaction.setState(ENpcFactionQuestState.NOTING);
        sendDailyQuest();
    }

    public void completeQuest(QuestTemplate questTemplate) {
        NpcFaction npcFaction = activeNpcFaction[questTemplate.isMentor() ? 1 : 0];
        if (npcFaction == null) {
            return;
        }
        npcFaction.setTime(getNextTime());
        npcFaction.setState(ENpcFactionQuestState.COMPLETE);
        timeLimit[npcFaction.isMentor() ? 1 : 0] = npcFaction.getTime();
        if (questTemplate.getMentorType() == QuestMentorType.MENTOR) { //fix если делаешь кв арахны выдаёт наставкинку крылья возле ника
            owner.getCommonData().setMentorFlagTime((int) (System.currentTimeMillis() / 1000) + 60 * 60 * 24); // TODO 1 day
            PacketSendUtility.broadcastPacket(owner, new SM_TITLE_INFO(owner, true), false);
            owner.sendPck(new SM_TITLE_INFO(true));
        }
    }

    /**
     *
     */
    public void sendDailyQuest() {
        for (int i = 0; i < 2; i++) {
            NpcFaction faction = activeNpcFaction[i];
            if (faction == null || !faction.isActive()) {
                continue;
            }
            if (timeLimit[i] > System.currentTimeMillis() / 1000) {
                continue;
            }
            int questId = 0;
            switch (faction.getState()) {
                case COMPLETE:
                    if (faction.getTime() > System.currentTimeMillis() / 1000) {
                        continue;
                    }
                    break;
                case START:
                    continue;
                case NOTING:
                    if (faction.getTime() > System.currentTimeMillis() / 1000) {
                        questId = faction.getQuestId();
                    }
                    break;
            }

            if (questId == 0) {
                List<QuestTemplate> quests = DataManager.QUEST_DATA.getQuestsByNpcFaction(faction.getId(), owner);
                if (quests.isEmpty()) {
                    continue;
                }
                questId = quests.get(Rnd.get(quests.size())).getId();
                faction.setQuestId(questId);
                faction.setTime(getNextTime());
            }
            owner.sendPck(new SM_QUEST_ACTION(questId, true));
        }
    }

    public void onLevelUp() {
        for (int i = 0; i < 2; i++) {
            NpcFaction faction = activeNpcFaction[i];
            if (faction == null || !faction.isActive()) {
                continue;
            }
            NpcFactionTemplate npcFactionTemplate = DataManager.NPC_FACTIONS_DATA.getNpcFactionById(faction.getId());
            if (npcFactionTemplate.getMaxLevel() < owner.getLevel()) {
                faction.setActive(false);
                activeNpcFaction[i] = null;
                if (faction.getState() == ENpcFactionQuestState.START) {
                    QuestService.abandonQuest(owner, faction.getQuestId());
                }
                owner.sendPck(SM_SYSTEM_MESSAGE.STR_FACTION_LEAVE_BY_LEVEL_LIMIT(npcFactionTemplate.getNameId()));
                faction.setState(ENpcFactionQuestState.NOTING);
            }
        }
    }

    private int getNextTime() {
        Calendar repeatDate = Calendar.getInstance(); // current date
        repeatDate.set(Calendar.AM_PM, Calendar.AM);
        repeatDate.set(Calendar.HOUR, 9);
        repeatDate.set(Calendar.MINUTE, 0);
        repeatDate.set(Calendar.SECOND, 0); // current date 09:00
        if (repeatDate.getTime().getTime() < System.currentTimeMillis()) {
            repeatDate.add(Calendar.HOUR, 24); // can repeat next day
        }
        return (int) (repeatDate.getTimeInMillis() / 1000);
    }

    public boolean canStartQuest(QuestTemplate template) {
        int type = template.isMentor() ? 1 : 0;
        NpcFaction faction = activeNpcFaction[type];
        if (faction != null && faction.getTime() > System.currentTimeMillis() / 1000L && timeLimit[type] < System.currentTimeMillis() / 1000L) {
            return true;
        }
        return false;
    }
}
