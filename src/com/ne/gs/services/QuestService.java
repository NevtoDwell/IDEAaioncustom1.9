/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.ne.gs.model.skill.PlayerSkillList;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.Rnd;
import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.configs.main.GroupConfig;
import com.ne.gs.configs.main.LoggingConfig;
import com.ne.gs.configs.main.MembershipConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.dataholders.QuestsData;
import com.ne.gs.model.DescId;
import com.ne.gs.model.PlayerClass;
import com.ne.gs.model.Race;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.drop.Drop;
import com.ne.gs.model.drop.DropItem;
import com.ne.gs.model.gameobjects.DropNpc;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.QuestStateList;
import com.ne.gs.model.gameobjects.player.RewardType;
import com.ne.gs.model.gameobjects.player.npcFaction.NpcFaction;
import com.ne.gs.model.items.ItemId;
import com.ne.gs.model.skill.PlayerSkillEntry;
import com.ne.gs.model.team2.common.legacy.LootRuleType;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.model.templates.QuestTemplate;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.model.templates.quest.*;
import com.ne.gs.network.aion.serverpackets.SM_CUBE_UPDATE;
import com.ne.gs.network.aion.serverpackets.SM_LOOT_STATUS;
import com.ne.gs.network.aion.serverpackets.SM_QUEST_ACTION;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.handlers.HandlerResult;
import com.ne.gs.questEngine.handlers.models.WorkOrdersData;
import com.ne.gs.questEngine.handlers.models.XMLQuest;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;
import com.ne.gs.services.abyss.AbyssPointsService;
import com.ne.gs.services.craft.CraftSkillUpdateService;
import com.ne.gs.services.drop.DropRegistrationService;
import com.ne.gs.services.item.ItemPacketService.ItemUpdateType;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.services.reward.BonusService;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.utils.audit.AuditLogger;

import static com.ne.gs.services.StigmaService.AdvStigmaPerm.accessibleSlotCountFor;

/**
 * @author Mr. Poke
 * @modified vlog, bobobear, xTz, Rolandas
 */
public final class QuestService {

    static QuestsData questsData = DataManager.QUEST_DATA;
    private static final Logger log = LoggerFactory.getLogger(QuestService.class);
    private static Multimap<Integer, QuestDrop> questDrop = ArrayListMultimap.create();

    public static boolean finishQuest(QuestEnv env) {
        return finishQuest(env, 0);
    }

    public static boolean finishQuest(QuestEnv env, int reward) {
        Player player = env.getPlayer();
        int id = env.getQuestId();
        QuestState qs = player.getQuestStateList().getQuestState(id);
        if (qs == null || qs.getStatus() != QuestStatus.REWARD) {
            return false;
        }
        QuestTemplate template = questsData.getQuestById(id);
        if (template.getCategory() == QuestCategory.MISSION && qs.getCompleteCount() != 0) {
            return false; // prevent repeatable reward because of wrong quest handling
        }
        if (!template.getExtendedRewards().isEmpty()) {
            if (qs.getCompleteCount() == template.getMaxRepeatCount() - 1) {
                return giveRewardAndFinish(env, template, true, 0);
            }
        }
        if (!template.getRewards().isEmpty() || !template.getBonus().isEmpty()) {
            return giveRewardAndFinish(env, template, false, reward);
        } else {
            return setFinishingState(env, template, reward);
        }
    }

    private static boolean giveRewardAndFinish(QuestEnv env, QuestTemplate template, boolean extended, int reward) {
        Player player = env.getPlayer();
        int id = env.getQuestId();
        List<QuestItems> questItems = new ArrayList<>();
        Rewards rewards;
        if (extended) {
            rewards = template.getExtendedRewards().get(reward);
        } else {
            rewards = template.getRewards().get(reward);
        }
        questItems.addAll(rewards.getRewardItem());
        int dialogId = env.getDialogId();
        if (dialogId != 18 && dialogId != 0 && !extended) {
            if (template.isUseClassReward()) {
                QuestItems classRewardItem = null;
                PlayerClass playerClass = player.getCommonData().getPlayerClass();
                int selRewIndex = dialogId - 8;
                switch (playerClass) {
                    case ASSASSIN: {
                        classRewardItem = getQuestItemsbyClass(id, template.getAssassinSelectableReward(), selRewIndex);
                        break;
                    }
                    case CHANTER: {
                        classRewardItem = getQuestItemsbyClass(id, template.getChanterSelectableReward(), selRewIndex);
                        break;
                    }
                    case CLERIC: {
                        classRewardItem = getQuestItemsbyClass(id, template.getPriestSelectableReward(), selRewIndex);
                        break;
                    }
                    case GLADIATOR: {
                        classRewardItem = getQuestItemsbyClass(id, template.getFighterSelectableReward(), selRewIndex);
                        break;
                    }
                    case RANGER: {
                        classRewardItem = getQuestItemsbyClass(id, template.getRangerSelectableReward(), selRewIndex);
                        break;
                    }
                    case SORCERER: {
                        classRewardItem = getQuestItemsbyClass(id, template.getWizardSelectableReward(), selRewIndex);
                        break;
                    }
                    case SPIRIT_MASTER: {
                        classRewardItem = getQuestItemsbyClass(id, template.getElementalistSelectableReward(), selRewIndex);
                        break;
                    }
                    case TEMPLAR: {
                        classRewardItem = getQuestItemsbyClass(id, template.getKnightSelectableReward(), selRewIndex);
                        break;
                    }
                }
                if (classRewardItem != null) {
                    questItems.add(classRewardItem);
                }
            } else {
                QuestItems selectebleRewardItem = null;
                if (dialogId - 8 >= 0 && dialogId - 8 < rewards.getSelectableRewardItem().size()) {
                    selectebleRewardItem = rewards.getSelectableRewardItem().get(dialogId - 8);
                } else {
                    log.error("The SelectableRewardItem list has no element with the given index (dialogId - 8) of " + (dialogId - 8) + ". See quest id "
                        + env.getQuestId());
                }
                if (selectebleRewardItem != null) {
                    questItems.add(selectebleRewardItem);
                }
            }
        } else if (dialogId == 18 && extended && !rewards.getSelectableRewardItem().isEmpty()) {
            QuestItems selectebleRewardItem = null;
            int index = env.getExtendedRewardIndex();
            if (index - 8 >= 0 && index - 8 < rewards.getSelectableRewardItem().size()) {
                selectebleRewardItem = rewards.getSelectableRewardItem().get(index - 8);
            } else if ((index - 1) >= 0 && (index - 1) < rewards.getSelectableRewardItem().size()) {
                selectebleRewardItem = rewards.getSelectableRewardItem().get(index - 1);
            } else {
                log.error("The extended SelectableRewardItem list has no element with the given index (extendedRewardIndex - 8) of " + (index - 8)
                    + ". See quest id " + env.getQuestId() + ". The size is: " + rewards.getSelectableRewardItem().size());
            }
            if (selectebleRewardItem != null) {
                questItems.add(selectebleRewardItem);
            }
        }

        if (!template.getBonus().isEmpty()) {
            QuestBonuses bonus = template.getBonus().get(0);
            // Handler can add additional bonuses on repeat (for event quests no data)
            HandlerResult result = QuestEngine.getInstance().onBonusApplyEvent(env, bonus.getType(), questItems);
            if (result == HandlerResult.FAILED) {
                return false;
            }
            QuestItems additional = BonusService.getInstance().getQuestBonus(player, template);
            if (additional != null) {
                questItems.add(additional);
            }
        }

        if (ItemService.addQuestItems(player, questItems)) {
            if (rewards.getGold() > 0) {
                player.getInventory().increaseKinah((long) (player.getRates().getQuestKinahRate() * rewards.getGold()), ItemUpdateType.INC_KINAH_QUEST);
            }
            if (rewards.getExp() > 0) {
                NpcTemplate npcTemplate = DataManager.NPC_DATA.getNpcTemplate(env.getTargetId());
                player.getCommonData().addExp(rewards.getExp(), RewardType.QUEST, npcTemplate != null ? npcTemplate.getNameId() : 0);
            }
            if (rewards.getTitle() > 0) {
                player.getTitleList().addTitle(rewards.getTitle(), true, 0);
            }
            if (rewards.getRewardAbyssPoint() > 0) {
                AbyssPointsService.addAp(player, (int) (player.getRates().getQuestApRate() * rewards.getRewardAbyssPoint()));
            }

            if (rewards.getExtendInventory() == 1) {
                CubeExpandService.expand(player, false);
            } else if (rewards.getExtendInventory() == 2) {
                WarehouseService.expand(player);
            }

            // setFinishingState should be called first,
            // coz accessibleSlotCountFor uses quest complete states
            boolean res = setFinishingState(env, template, reward);
            if (res && rewards.isStigma()) {
                player.sendPck(SM_CUBE_UPDATE.stigmaSlots(accessibleSlotCountFor(player)));
            }

            return res;
        } else {
            return false;
        }
    }

    private static boolean setFinishingState(QuestEnv env, QuestTemplate template, int reward) {
        Player player = env.getPlayer();
        int id = env.getQuestId();
        QuestState qs = player.getQuestStateList().getQuestState(id);
        // remove all worker list item if finished.
        QuestWorkItems qwi = questsData.getQuestById(id).getQuestWorkItems();
        if (qwi != null) {
            long count = 0;
            for (QuestItems qi : qwi.getQuestWorkItem()) {
                if (qi != null) {
                    count = player.getInventory().getItemCountByItemId(qi.getItemId());
                    if (count > 0) {
                        if (!player.getInventory().decreaseByItemId(qi.getItemId(), count)) {
                            return false;
                        }
                    }
                }
            }
        }
        qs.setStatus(QuestStatus.COMPLETE);
        qs.setQuestVar(0);
        qs.setReward(reward);
        qs.setCompleteCount(qs.getCompleteCount() + 1);

        if (LoggingConfig.LOG_QUEST_COMPLETE) {
            log.info("[QUEST]Player: " + player.getName() + " complete quest Id: " + env.getQuestId());
        }

        if (template.getRepeatCycle() != null) {
            qs.setNextRepeatTime(countNextRepeatTime(player, template));
        }
        player.sendPck(new SM_QUEST_ACTION(id, qs.getStatus(), qs.getQuestVars().getQuestVars()));
        player.getController().updateNearbyQuests();
        QuestEngine.getInstance().onLvlUp(env);

        if (template.getNpcFactionId() != 0) {
            player.getNpcFactions().completeQuest(template);
        }
        return true;
    }

    private static QuestItems getQuestItemsbyClass(int id, List<QuestItems> classSelRew, int selRewIndex) {
        if (selRewIndex >= 0 && selRewIndex < classSelRew.size()) {
            return classSelRew.get(selRewIndex);
        } else {
            log.error("Wrong selectable reward index " + selRewIndex + " for quest " + id);
        }
        return null;
    }

    private static Timestamp countNextRepeatTime(Player player, QuestTemplate template) {
        DateTime now = DateTime.now();
        DateTime repeatDate = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), 9, 0, 0);
        if (template.getRepeatCycle() == QuestRepeatCycle.ALL) {
            if (now.isAfter(repeatDate)) {
                repeatDate = repeatDate.plusHours(24);
            }
            player.sendPck(new SM_SYSTEM_MESSAGE(1400855, "9"));
        } else {
            int daysToAdd = template.getRepeatCycle().ordinal() - repeatDate.getDayOfWeek();
            if (daysToAdd <= 0) {
                daysToAdd += 7;
            }
            repeatDate = repeatDate.plusDays(daysToAdd);
            player.sendPck(new SM_SYSTEM_MESSAGE(1400857, DescId.of(1800663), "9"));
        }
        return new Timestamp(repeatDate.getMillis());
    }

    /**
     * This method will not propagate any exceptions to the caller
     *
     * @param env
     *
     * @return
     */
    public static boolean checkStartConditions(QuestEnv env, boolean warn) {
        try {
            return checkStartConditionsImpl(env, warn);
        } catch (Exception ex) {
            log.error("QE: exception in checkStartCondition", ex);
        }
        return false;
    }

    private static boolean checkStartConditionsImpl(QuestEnv env, boolean warn) {
        Player player = env.getPlayer();
        QuestTemplate template = questsData.getQuestById(env.getQuestId());

        if (template == null) {
            return false;
        }

        if (template.getRacePermitted() != null) {
            if (template.getRacePermitted() != player.getRace() && template.getRacePermitted() != Race.PC_ALL) {
                return false;
            }
        }

        // min level - 2 so that the gray quest arrow shows when quest is almost available
        // quest level will be checked again in QuestService.startQuest() when attempting to start
        int levelDiff = template.getMinlevelPermitted() - player.getLevel();
        if (levelDiff > 2 && (template.getMinlevelPermitted() != 99)) {
            return false;
        }
        if (warn && levelDiff > 0 && (template.getMinlevelPermitted() != 99)) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_MIN_LEVEL(Integer.toString(template.getMinlevelPermitted())));
            return false;
        }
        if (template.getMaxlevelPermitted() != 0 && player.getLevel() > template.getMaxlevelPermitted()) {
            if (warn) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_MAX_LEVEL(Integer.toString(template.getMaxlevelPermitted())));
            }
            return false;
        }

        if (!template.getClassPermitted().isEmpty() && !template.getClassPermitted().contains(player.getCommonData().getPlayerClass())) {
            if (warn) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_CLASS);
            }
            return false;
        }

        if (template.getGenderPermitted() != null && template.getGenderPermitted() != player.getGender()) {
            if (warn) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_GENDER);
            }
            return false;
        }
        if (((template.isExpert()) && (!CraftSkillUpdateService.canLearnMoreExpertCraftingSkill(player)))
            || ((template.isMaster()) && (!CraftSkillUpdateService.canLearnMoreMastertCraftingSkill(player)))) {
            return false;
        }

        int fulfilledStartConditions = 0;
        if (!template.getXMLStartConditions().isEmpty()) {
            for (XMLStartCondition startCondition : template.getXMLStartConditions()) {
                if (startCondition.check(player, warn)) {
                    fulfilledStartConditions++;
                }
            }
            if (fulfilledStartConditions < 1) {
                return false;
            }
        }

        if (warn && !inventoryItemCheck(env, warn)) {
            return false;
        }


        if (template.getCombineSkill() != null) {
            List<Integer> skills = new ArrayList<>(); // skills to check
            if (template.getCombineSkill() == -1) // any skill
            {
                skills.add(30002);
                skills.add(30003);
                skills.add(40001);
                skills.add(40002);
                skills.add(40003);
                skills.add(40004);
                skills.add(40007);
                skills.add(40008);
                skills.add(40010);
            } else {
                skills.add(template.getCombineSkill());
            }
            boolean result = false;
            for (int skillId : skills) {
                PlayerSkillEntry skill = player.getSkillList().getSkillEntry(skillId);
                if (skill != null && skill.getSkillLevel() >= template.getCombineSkillPoint() && skill.getSkillLevel() - 40 <= template.getCombineSkillPoint()) {
                    result = true;
                    break;
                }
            }
            if (!result) {
                return false;
            }
        }

        // Check for updating nearby quests
        QuestState qs = player.getQuestStateList().getQuestState(template.getId());
        if (qs != null && qs.getStatus() != QuestStatus.NONE) {
            if (!qs.canRepeat()) {
                return false;
            }
        }
        return true;
    }

    /*
     * Check the starting conditions and start a quest Reworked 12.06.2011
     * @author vlog
     */
    public static boolean startQuest(QuestEnv env, QuestStatus status) {
        return startQuest(env, status, env.getDialogId() != 0);
    }

    public static boolean startQuest(QuestEnv env, QuestStatus status, boolean warn){
        return startQuest(env, status, warn, false);
    }

    public static boolean startQuest(QuestEnv env, QuestStatus status, boolean warn, boolean ignoreConditions) {
        Player player = env.getPlayer();
        int id = env.getQuestId();
        QuestStateList qsl = player.getQuestStateList();
        QuestState qs = qsl.getQuestState(id);
        QuestTemplate template = questsData.getQuestById(env.getQuestId());
        if (template.getNpcFactionId() != 0) {
            NpcFaction faction = player.getNpcFactions().getNpcFactinById(template.getNpcFactionId());
            if (!faction.isActive() || faction.getQuestId() != env.getQuestId()) {
                AuditLogger.info(player, "Possible packet hack learn Guild quest");
                return false;
            }
        }

        if (!ignoreConditions && !checkStartConditions(env, warn)) {
            return false;
        }

        if (player.getLevel() < template.getMinlevelPermitted() && template.getMinlevelPermitted() != 99) {
            return false;
        }

        if (template.getCategory() != QuestCategory.EVENT && !checkQuestListSize(qsl) && !player.havePermission(MembershipConfig.QUEST_LIMIT_DISABLED)) {
            player.sendPck(new SM_SYSTEM_MESSAGE(1300622, template.getName()));
            return false;
        }

        if (qs != null) {
            if (!qs.canRepeat()) {
                return false;
            }
            qs.setStatus(status);
        } else {
            player.getQuestStateList().addQuest(id, new QuestState(id, status, 0, 0, null, 0, null));
        }

        if (template.getNpcFactionId() != 0 && !template.isTimeBased()) {
            if (!player.getNpcFactions().canStartQuest(template)) {
                AuditLogger.info(player, "try start guild daily quest befare time");
                return false;
            }
            player.getNpcFactions().startQuest(template);
        }

        player.sendPck(new SM_QUEST_ACTION(id, status.value(), 0));
        player.getController().updateNearbyQuests();
        return true;
    }

    /*
     * Check the starting conditions and start a quest Reworked 12.06.2011
     * @author vlog
     */
    public static boolean startQuest(QuestEnv env) {
        return startQuest(env, QuestStatus.START, env.getDialogId() != 0);
    }

    /**
     * Starts or temporary locks the mission Used only from the QuestHandler class
     *
     * @param env
     * @param status
     *     START or LOCKED
     */
    public static void startMission(QuestEnv env, QuestStatus status) {
        Player player = env.getPlayer();
        int questId = env.getQuestId();

        if (player.getQuestStateList().getQuestState(questId) != null) {
            return;
        } else {
            player.getQuestStateList().addQuest(questId, new QuestState(questId, status, 0, 0, null, 0, null));
        }

        player.sendPck(new SM_QUEST_ACTION(questId, status.value(), 0));
    }

    /**
     * Check the mission start requirements
     *
     * @param env
     *
     * @return true, if all requirements are there
     */
    public static boolean checkMissionStatConditions(QuestEnv env) {
        Player player = env.getPlayer();
        QuestTemplate template = questsData.getQuestById(env.getQuestId());

        // Check template existence
        if (template == null) {
            return false;
        }

        // Check permitted race
        if (template.getRacePermitted() != null && template.getRacePermitted() != player.getRace()) {
            return false;
        }

        // Check permitted class
        if (template.getClassPermitted().size() != 0 && !template.getClassPermitted().contains(player.getCommonData().getPlayerClass())) {
            return false;
        }

        // Check permitted gender
        if (template.getGenderPermitted() != null && template.getGenderPermitted() != player.getGender()) {
            return false;
        }

        // Check required skills
        if (template.getCombineSkill() != null) {
            List<Integer> skills = new ArrayList<>(); // skills to check
            if (template.getCombineSkill() == -1) // any skill
            {
                skills.add(30002);
                skills.add(30003);
                skills.add(40001);
                skills.add(40002);
                skills.add(40003);
                skills.add(40004);
                skills.add(40007);
                skills.add(40008);
                skills.add(40010);
            } else {
                skills.add(template.getCombineSkill());
            }
            boolean result = false;
            for (int skillId : skills) {
                PlayerSkillEntry skill = player.getSkillList().getSkillEntry(skillId);
                if (skill != null && skill.getSkillLevel() >= template.getCombineSkillPoint() && skill.getSkillLevel() - 40 <= template.getCombineSkillPoint()) {
                    result = true;
                    break;
                }
            }
            if (!result) {
                return false;
            }
        }

        // Everything is ok
        return true;
    }

    public static boolean startEventQuest(QuestEnv env, QuestStatus questStatus) {
        QuestTemplate template = questsData.getQuestById(env.getQuestId());
        if (template.getCategory() != QuestCategory.EVENT) {
            return false;
        }

        int id = env.getQuestId();
        Player player = env.getPlayer();

        player.sendPck(new SM_QUEST_ACTION(id, questStatus, 0));
        QuestState qs = player.getQuestStateList().getQuestState(id);
        if (qs == null) {
            qs = new QuestState(template.getId(), questStatus, 0, 0, null, 0, null);
            player.getQuestStateList().addQuest(id, qs);
        } else if (template.getMaxRepeatCount() >= qs.getCompleteCount()) {
            qs.setStatus(questStatus);
            qs.setQuestVar(0);
        }

        player.getController().updateNearbyQuests();
        return true;
    }

    /*
     * Check the player's quest list size for starting a new one Issue #13 fix
     * @param quest state list
     */
    private static boolean checkQuestListSize(QuestStateList qsl) {
        // The player's quest list size + the new one to start
        return (qsl.getNormalQuestListSize() + 1) <= CustomConfig.BASIC_QUEST_SIZE_LIMIT;
    }

    public boolean completeQuest(QuestEnv env) {
        Player player = env.getPlayer();
        int id = env.getQuestId();
        QuestState qs = player.getQuestStateList().getQuestState(id);
        if (qs == null || qs.getStatus() != QuestStatus.START) {
            return false;
        }

        qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
        qs.setStatus(QuestStatus.REWARD);
        player.sendPck(new SM_QUEST_ACTION(id, qs.getStatus(), qs.getQuestVars().getQuestVars()));
        player.getController().updateNearbyQuests();
        return true;
    }

    public static boolean collectItemCheck(QuestEnv env, boolean removeItem) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(env.getQuestId());
        if (qs == null && removeItem) {
            return false;
        }
        QuestTemplate template = questsData.getQuestById(env.getQuestId());
        CollectItems collectItems = template.getCollectItems();
        if (collectItems == null) {
            InventoryItems inventoryItems = template.getInventoryItems();
            if (inventoryItems == null) {
                return true;
            }
            for (InventoryItem inventoryItem : inventoryItems.getInventoryItem()) {
                int itemId = inventoryItem.getItemId();
                if (player.getInventory().getItemCountByItemId(itemId) < 1L) {
                    return false;
                }
            }
            if (removeItem) {
                for (InventoryItem inventoryItem : inventoryItems.getInventoryItem()) {
                    player.getInventory().decreaseByItemId(inventoryItem.getItemId(), 1L);
                }
            }
            return true;
        }

        for (CollectItem collectItem : collectItems.getCollectItem()) {
            int itemId = collectItem.getItemId();
            long count = itemId == ItemId.KINAH.value() ? player.getInventory().getKinah() : player.getInventory().getItemCountByItemId(itemId);
            if (collectItem.getCount() > count) {
                return false;
            }
        }
        if (removeItem) {
            for (CollectItem collectItem : collectItems.getCollectItem()) {
                if (collectItem.getItemId() == 182400001) {
                    player.getInventory().decreaseKinah(collectItem.getCount());
                } else {
                    player.getInventory().decreaseByItemId(collectItem.getItemId(), collectItem.getCount());
                }
            }
        }
        return true;
    }

    public static boolean inventoryItemCheck(QuestEnv env, boolean showWarning) {
        Player player = env.getPlayer();
        QuestTemplate template = questsData.getQuestById(env.getQuestId());
        InventoryItems inventoryItems = template.getInventoryItems();
        if (inventoryItems == null) {
            return true;
        }
        int requiredItemNameId = 0;
        for (InventoryItem inventoryItem : inventoryItems.getInventoryItem()) {
            Item item = player.getInventory().getFirstItemByItemId(inventoryItem.getItemId());
            if (item == null) {
                requiredItemNameId = DataManager.ITEM_DATA.getItemTemplate(inventoryItem.getItemId()).getNameId();
                break;
            }
        }
        if (requiredItemNameId != 0 && showWarning) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_QUEST_ACQUIRE_ERROR_INVENTORY_ITEM(DescId.of(requiredItemNameId)));
        }

        return requiredItemNameId == 0;
    }

    public static VisibleObject spawnQuestNpc(int worldId, int instanceId, int templateId, float x, float y, float z,
                                              byte heading) {
        return SpawnEngine
                .spawnObject(SpawnEngine.addNewSingleTimeSpawn(worldId, templateId, x, y, z, heading), instanceId);
    }

    public static VisibleObject addNewSpawn(int worldId, int instanceId, int templateId, float x, float y, float z,
                                            int heading) {

        return SpawnEngine.spawnObject(SpawnEngine.addNewSingleTimeSpawn(worldId, templateId, x, y, z, heading), instanceId);
    }

    public static void addNewSpawn(int worldId, int instanceId, int templateId, float x, float y, float z,
                                   byte heading, int timeInMin) {
        final Npc npc = (Npc) spawnQuestNpc(worldId, instanceId, templateId, x, y, z, (byte) 0);
        if (!npc.getPosition().isInstanceMap()) {
            despawnQuestNpc(npc, timeInMin);
        }
    }

    private static void despawnQuestNpc(final Npc npc, int timeInMin) {
        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if (npc != null && !npc.getLifeStats().isAlreadyDead()) {
                    npc.getController().onDelete();
                }
            }
        }, 60000 * timeInMin);
    }

    public static int getQuestDrop(Set<DropItem> dropItems, int index, Npc npc, Collection<Player> players, Player player) {
        Collection<QuestDrop> drops = getQuestDrop(npc.getNpcId());
        if (drops.isEmpty()) {
            return index;
        }
        DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npc.getObjectId());
        for (QuestDrop drop : drops) {
            if (!Rnd.chance(drop.getChance())) {
                continue;
            }
            if (players != null && player.isInGroup2()) {
                List<Player> pls = new ArrayList<>();
                if (drop.isDropEachMember()) {
                    for (Player member : players) {
                        if (isQuestDrop(member, drop)) {
                            pls.add(member);
                            dropItems.add(regQuestDropItem(drop, index++, member.getObjectId()));
                        }
                    }
                } else {
                    for (Player member : players) {
                        if (isQuestDrop(member, drop)) {
                            pls.add(member);
                            break;
                        }
                    }
                }
                if (pls.size() > 0) {
                    if (!drop.isDropEachMember()) {
                        dropItems.add(regQuestDropItem(drop, index++, 0));
                    }
                    for (Player p : pls) {
                        dropNpc.setPlayerObjectId(p.getObjectId());
                        if (player.getPlayerGroup2().getLootGroupRules().getLootRule() != LootRuleType.FREEFORALL) {
                            p.sendPck(new SM_LOOT_STATUS(npc.getObjectId(), 0));
                        }
                    }
                    pls.clear();
                }
            } else if (isQuestDrop(player, drop)) {
                dropItems.add(regQuestDropItem(drop, index++, player.getObjectId()));
            }
        }
        return index;
    }

    private static DropItem regQuestDropItem(QuestDrop drop, int index, Integer winner) {
        DropItem item = new DropItem(new Drop(drop.getItemId(), 1, 1, drop.getChance(), false));
        item.setPlayerObjId(winner);
        item.setIndex(index);
        item.setCount(1);
        return item;
    }

    private static boolean isQuestDrop(Player player, QuestDrop drop) {
        int questId = drop.getQuestId();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null || qs.getStatus() != QuestStatus.START) {
            return false;
        }
        QuestTemplate qt = DataManager.QUEST_DATA.getQuestById(questId);
        if (qt.getMentorType() == QuestMentorType.MENTE) {
            if (!player.isInGroup2()) {
                return false;
            }

            PlayerGroup group = player.getPlayerGroup2();
            boolean found = false;
            for (Player member : group.getMembers()) {
                if (member.isMentor() && MathUtil.getDistance(player, member) < GroupConfig.GROUP_MAX_DISTANCE) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        if (drop instanceof HandlerSideDrop) {
            if (((HandlerSideDrop) drop).getNeededAmount() <= player.getInventory().getItemCountByItemId(drop.getItemId())) {
                return false;
            } else {
                return true;
            }
        }

        CollectItems collectItems = questsData.getQuestById(questId).getCollectItems();
        if (collectItems == null) {
            return true;
        }

        for (CollectItem collectItem : collectItems.getCollectItem()) {
            int collectItemId = collectItem.getItemId();
            if (collectItemId != drop.getItemId()) {
                continue;
            }
            long count = player.getInventory().getItemCountByItemId(collectItemId);
            if (collectItem.getCount() > count) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param questId
     * @param playerLevel
     *
     * @return false if player is 2 or more levels below quest level
     */
    public static boolean checkLevelRequirement(int questId, int playerLevel) {
        return playerLevel >= questsData.getQuestById(questId).getMinlevelPermitted();
    }

    public static boolean questTimerStart(QuestEnv env, int timeInSeconds) {
        final Player player = env.getPlayer();

        // Schedule Action When Timer Finishes
        Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                QuestEngine.getInstance().onQuestTimerEnd(new QuestEnv(null, player, 0, 0));
            }
        }, timeInSeconds * 1000);
        player.getController().addTask(TaskId.QUEST_TIMER, task);
        player.sendPck(new SM_QUEST_ACTION(env.getQuestId(), timeInSeconds));
        return true;
    }

    public static boolean invisibleTimerStart(QuestEnv env, int timeInSeconds) {
        final Player player = env.getPlayer();

        ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                QuestEngine.getInstance().onInvisibleTimerEnd(new QuestEnv(null, player, 0, 0));
            }
        }, timeInSeconds * 1000);

        return true;
    }

    public static boolean questTimerEnd(QuestEnv env) {
        Player player = env.getPlayer();

        player.getController().cancelTask(TaskId.QUEST_TIMER);
        player.sendPck(new SM_QUEST_ACTION(env.getQuestId(), 0));
        return true;
    }

    public static boolean abandonQuest(Player player, int questId) {
        QuestTemplate template = questsData.getQuestById(questId);
        if (template == null) {
            return false;
        }
        if (template.isCannotGiveup()) {
            return false;
        }

        QuestState qs = player.getQuestStateList().getQuestState(questId);

        if (qs == null) {
            return false;
        }

        if (qs.getStatus() == QuestStatus.COMPLETE || qs.getStatus() == QuestStatus.LOCKED) {
            AuditLogger.info(player, "Cancel from completed quest. quest Id: " + questId);
            return false;
        }
        if (template.getNpcFactionId() != 0) {
            player.getNpcFactions().abortQuest(template);
        }
        qs.setStatus(QuestStatus.NONE);
        qs.setQuestVar(0);
        // remove all worker list item if abandoned
        QuestWorkItems qwi = template.getQuestWorkItems();
        if (qwi != null) {
            long count = 0;
            for (QuestItems qi : qwi.getQuestWorkItem()) {
                if (qi != null) {
                    count = player.getInventory().getItemCountByItemId(qi.getItemId());
                    if (count > 0) {
                        player.getInventory().decreaseByItemId(qi.getItemId(), count);
                    }
                }
            }
        }
        if (template.getCategory() == QuestCategory.TASK) {
            WorkOrdersData wod = null;
            for (XMLQuest xmlQuest : DataManager.XML_QUESTS.getQuest()) {
                if (xmlQuest.getId() == questId) {
                    if (xmlQuest instanceof WorkOrdersData) {
                        wod = (WorkOrdersData) xmlQuest;
                        break;
                    }
                }
            }
            if (wod != null) {
                player.getRecipeList().deleteRecipe(player, wod.getRecipeId());
            }
        }

        if (player.getController().getTask(TaskId.QUEST_TIMER) != null) {
            questTimerEnd(new QuestEnv(null, player, questId, 0));
        }

        player.sendPck(new SM_QUEST_ACTION(questId));
        player.getController().updateNearbyQuests();
        return true;
    }

    public static Collection<QuestDrop> getQuestDrop(int npcId) {
        if (questDrop.containsKey(npcId)) {
            return questDrop.get(npcId);
        }
        return Collections.<QuestDrop>emptyList();
    }

    public static void addQuestDrop(int npcId, QuestDrop drop) {
        if (!questDrop.containsKey(npcId)) {
            questDrop.put(npcId, drop);
        } else {
            questDrop.get(npcId).add(drop);
        }
    }

    public static List<Player> getEachDropMembers(PlayerGroup group, int npcId, int questId) {
        List<Player> players = new ArrayList<>();
        for (QuestDrop qd : getQuestDrop(npcId)) {
            if (qd.isDropEachMember()) {
                for (Player player : group.getMembers()) {
                    QuestState qstel = player.getQuestStateList().getQuestState(questId);
                    if (qstel != null && qstel.getStatus() == QuestStatus.START) {
                        players.add(player);
                    }
                }
                break;
            }
        }
        return players;
    }
}
