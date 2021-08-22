/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.reward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.Rnd;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.dataholders.ItemGroupsData;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.QuestTemplate;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.model.templates.itemgroups.BonusItemGroup;
import com.ne.gs.model.templates.itemgroups.CraftGroup;
import com.ne.gs.model.templates.itemgroups.ItemRaceEntry;
import com.ne.gs.model.templates.itemgroups.ManastoneGroup;
import com.ne.gs.model.templates.itemgroups.MedalGroup;
import com.ne.gs.model.templates.quest.QuestBonuses;
import com.ne.gs.model.templates.quest.QuestItems;
import com.ne.gs.model.templates.rewards.BonusType;
import com.ne.gs.model.templates.rewards.CraftItem;
import com.ne.gs.model.templates.rewards.MedalItem;

/**
 * @author Rolandas
 */
public class BonusService {

    private static BonusService instance = new BonusService();
    private ItemGroupsData itemGroups = DataManager.ITEM_GROUPS_DATA;
    private static final Logger log = LoggerFactory.getLogger(BonusService.class);

    private BonusService() {
    }

    public static BonusService getInstance() {
        return instance;
    }

    public static BonusService getInstance(ItemGroupsData itemGroups) {
        instance.itemGroups = itemGroups;
        return instance;
    }

    public BonusItemGroup[] getGroupsByType(BonusType type) {
        switch (type) {
            case BOSS:
                return itemGroups.getBossGroups();
            case ENCHANT:
                return itemGroups.getEnchantGroups();
            case FOOD:
                return itemGroups.getFoodGroups();
            case GATHER:
                return ArrayUtils.addAll(itemGroups.getOreGroups(), itemGroups.getGatherGroups());
            case MANASTONE:
                return itemGroups.getManastoneGroups();
            case MEDICINE:
                return itemGroups.getMedicineGroups();
            case TASK:
                return itemGroups.getCraftGroups();
            case MOVIE:
                return null;
            default:
                log.warn("Bonus of type " + type + " is not implemented");
                return null;
        }
    }

    public BonusItemGroup getRandomGroup(BonusItemGroup[] groups) {
        float total = 0;
        if (groups == null) {
            return null;
        }
        for (BonusItemGroup gr : groups) {
            total += gr.getChance();
        }
        if (total == 0) {
            return null;
        }

        BonusItemGroup chosenGroup = null;
        if (groups != null) {
            int percent = 100;
            for (BonusItemGroup gr : groups) {
                float chance = getNormalizedChance(gr.getChance(), total);
                if (Rnd.get(0, percent) <= chance) {
                    chosenGroup = gr;
                    break;
                } else {
                    percent -= chance;
                }
            }
        }
        return chosenGroup;
    }

    float getNormalizedChance(float chance, float total) {
        return chance * 100f / total;
    }

    public BonusItemGroup getRandomGroup(BonusType type) {
        return getRandomGroup(getGroupsByType(type));
    }

    public QuestItems getQuestBonus(Player player, QuestTemplate questTemplate) {
        List<QuestBonuses> bonuses = questTemplate.getBonus();
        if (bonuses.isEmpty()) {
            return null;
        }
        // Only one
        QuestBonuses bonus = bonuses.get(0);
        if (bonus.getType() == BonusType.NONE) {
            return null;
        }

        switch (bonus.getType()) {
            case TASK:
                return getCraftBonus(player, questTemplate);
            case MANASTONE:
                return getManastoneBonus(player, bonus);
            case MEDAL:
                return getMedalBonus(player, questTemplate);
            case MOVIE:
                return null;
            default:
                log.warn("Bonus of type " + bonus.getType() + " is not implemented");
                return null;
        }
    }

    QuestItems getCraftBonus(Player player, QuestTemplate questTemplate) {
        BonusItemGroup[] groups = itemGroups.getCraftGroups();
        CraftGroup group = null;
        ItemRaceEntry[] allRewards = null;

        while (groups != null && groups.length > 0 && group == null) {
            group = (CraftGroup) getRandomGroup(groups);
            if (group == null) {
                break;
            }
            allRewards = group.getRewards(questTemplate.getCombineSkill(), questTemplate.getCombineSkillPoint());
            if (allRewards.length == 0) {
                List<BonusItemGroup> temp = new ArrayList<>();
                Collections.addAll(temp, groups);
                temp.remove(group);
                group = null;
                groups = temp.toArray(new BonusItemGroup[0]);
            }
        }

        if (group == null) {
            return null;
        }
        List<ItemRaceEntry> finalList = new ArrayList<>();

        for (int i = 0; i < allRewards.length; i++) {
            ItemRaceEntry r = allRewards[i];
            if (!r.checkRace(player.getCommonData().getRace())) {
                continue;
            }
            finalList.add(r);
        }

        if (finalList.isEmpty()) {
            return null;
        }

        int itemIndex = Rnd.get(finalList.size());
        int itemCount = 1;

        ItemRaceEntry reward = finalList.get(itemIndex);
        if (reward instanceof CraftItem) {
            itemCount = Rnd.get(3, 5);
        }

        return new QuestItems(reward.getId(), itemCount);
    }

    QuestItems getMedalBonus(Player player, QuestTemplate template) {
        BonusItemGroup[] groups = itemGroups.getMedalGroups();
        MedalGroup group = (MedalGroup) getRandomGroup(groups);
        int bonusLevel = template.getBonus().get(0).getLevel();

        MedalItem finalReward = null;

        float total = 0.0F;
        for (MedalItem medal : group.getItems()) {
            if (medal.getLevel() == bonusLevel) {
                total += medal.getChance();
            }
        }
        if (total == 0.0F) {
            return null;
        }
        float rnd = Rnd.get() * total;
        float luck = 0.0F;
        for (MedalItem medal : group.getItems()) {
            if (medal.getLevel() == bonusLevel) {
                luck += medal.getChance();

                if (rnd <= luck) {
                    finalReward = medal;
                    break;
                }
            }
        }
        return finalReward != null ? new QuestItems(finalReward.getId(), finalReward.getCount()) : null;
    }

    QuestItems getManastoneBonus(Player player, QuestBonuses bonus) {
        ManastoneGroup group = (ManastoneGroup) getRandomGroup(BonusType.MANASTONE);
        ItemRaceEntry[] allRewards = group.getRewards();
        List<ItemRaceEntry> finalList = new ArrayList<>();
        for (int i = 0; i < allRewards.length; i++) {
            ItemRaceEntry r = allRewards[i];
            ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(r.getId());
            if (bonus.getLevel() != template.getLevel()) {
                continue;
            }
            finalList.add(r);
        }
        if (finalList.isEmpty()) {
            return null;
        }

        int itemIndex = Rnd.get(finalList.size());
        ItemRaceEntry reward = finalList.get(itemIndex);
        return new QuestItems(reward.getId(), 1);
    }
}
