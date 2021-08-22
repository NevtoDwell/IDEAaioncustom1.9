/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.craft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.craft.ExpertQuestsList;
import com.ne.gs.model.craft.MasterQuestsList;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.PersistentState;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.skill.PlayerSkillEntry;
import com.ne.gs.model.templates.CraftLearnTemplate;
import com.ne.gs.model.templates.recipe.RecipeTemplate;
import com.ne.gs.network.aion.serverpackets.SM_QUEST_COMPLETED_LIST;
import com.ne.gs.network.aion.serverpackets.SM_SKILL_LIST;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.services.trade.PricesService;

public final class RelinquishCraftStatus {

    private static final Logger log = LoggerFactory.getLogger(RelinquishCraftStatus.class);
    private static final int expertMinValue = 400;
    private static final int expertMaxValue = 499;
    private static final int masterMinValue = 500;
    private static final int masterMaxValue = 549;
    private static final int expertPrice = 120000;
    private static final int masterPrice = 3500000;
    private static final int systemMessageId = 1300388;
    private static final int skillMessageId = 1401127;

    public static RelinquishCraftStatus getInstance() {
        return SingletonHolder.instance;
    }

    public static void relinquishExpertStatus(Player player, Npc npc) {
        CraftLearnTemplate craftLearnTemplate = CraftSkillUpdateService.npcBySkill.get(npc.getNpcId());
        int skillId = craftLearnTemplate.getSkillId();
        PlayerSkillEntry skill = player.getSkillList().getSkillEntry(skillId);
        if (!canRelinquishCraftStatus(player, skill, craftLearnTemplate, expertMinValue, expertMaxValue)) {
            return;
        }
        if (!successDecreaseKinah(player, expertPrice)) {
            return;
        }
        skill.setSkillLvl(399);
        player.sendPck(new SM_SKILL_LIST(skill, skillMessageId, false));
        removeRecipesAbove(player, skillId, expertMinValue);
        deleteCraftStatusQuests(skillId, player, false);
    }

    public static void relinquishMasterStatus(Player player, Npc npc) {
        CraftLearnTemplate craftLearnTemplate = CraftSkillUpdateService.npcBySkill.get(npc.getNpcId());
        int skillId = craftLearnTemplate.getSkillId();
        PlayerSkillEntry skill = player.getSkillList().getSkillEntry(skillId);
        if (!canRelinquishCraftStatus(player, skill, craftLearnTemplate, masterMinValue, masterMaxValue)) {
            return;
        }
        if (!successDecreaseKinah(player, masterPrice)) {
            return;
        }
        skill.setSkillLvl(expertMaxValue);
        player.sendPck(new SM_SKILL_LIST(skill, skillMessageId, false));
        removeRecipesAbove(player, skillId, masterMinValue);
        deleteCraftStatusQuests(skillId, player, false);
    }

    private static boolean canRelinquishCraftStatus(Player player, PlayerSkillEntry skill, CraftLearnTemplate craftLearnTemplate,
                                                    int minValue, int maxValue) {
        if ((craftLearnTemplate == null) || (!craftLearnTemplate.isCraftSkill())) {
            return false;
        }
        if ((skill == null) || (skill.getSkillLevel() < minValue) || (skill.getSkillLevel() > maxValue)) {
            return false;
        }
        return true;
    }

    private static boolean successDecreaseKinah(Player player, int basePrice) {
        if (!player.getInventory().tryDecreaseKinah(PricesService.getPriceForService(basePrice, player.getRace()))) {
            player.sendPck(new SM_SYSTEM_MESSAGE(1300388));
            return false;
        }
        return true;
    }

    private static void removeRecipesAbove(Player player, int skillId, int level) {
        for (RecipeTemplate recipe : DataManager.RECIPE_DATA.getRecipeTemplates().valueCollection()) {
            if ((recipe.getSkillid().intValue() == skillId) && (recipe.getSkillpoint().intValue() >= level)) {
                player.getRecipeList().deleteRecipe(player, recipe.getId().intValue());
            }
        }
    }

    private static void deleteCraftStatusQuests(int skillId, Player player, boolean isExpert) {
        for (int questId : MasterQuestsList.getSkillsIds(skillId, player.getRace())) {
            QuestState qs = player.getQuestStateList().getQuestState(questId);
            if (qs != null) {
                qs.setQuestVar(0);
                qs.setCompleteCount(0);
                qs.setStatus(null);
                qs.setPersistentState(PersistentState.DELETED);
            }
        }
        if (isExpert) {
            for (int questId : ExpertQuestsList.getSkillsIds(skillId, player.getRace())) {
                QuestState qs = player.getQuestStateList().getQuestState(questId);
                if (qs != null) {
                    qs.setQuestVar(0);
                    qs.setCompleteCount(0);
                    qs.setStatus(null);
                    qs.setPersistentState(PersistentState.DELETED);
                }
            }
        }
        player.sendPck(new SM_QUEST_COMPLETED_LIST(player.getQuestStateList().getAllFinishedQuests()));
        player.getController().updateNearbyQuests();
    }

    private static final class SingletonHolder {

        protected static final RelinquishCraftStatus instance = new RelinquishCraftStatus();
    }
}
