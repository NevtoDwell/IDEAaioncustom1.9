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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.Rnd;
import com.ne.gs.configs.main.PvPConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.PlayerClass;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ItemTemplate;

public final class PvPRewardService {

    private static final Logger log = LoggerFactory.getLogger("PVP_LOG");
    private static final String templar = "115001381,100901028,100101022,100001339,112601240,110601295,111601260,113601249,114601244,121001151,125002883,123001205,120001233,122001392";
    private static final String gladiator = "101300978,100901028,100001339,112601240,110601295,111601260,113601249,114601244,121001151,125002883,123001205,120001233,122001392";
    private static final String cleric = "115001381,101501050,100101022,112501222,114501300,113501294,111501279,110501318,121001152,125002882,123001206,120001234,122001393";
    private static final String chanter = "115001381,101501050,100101022,112501222,114501300,113501294,111501279,110501318,121001151,125002882,123001205,120001233,122001392";
    private static final String assassin = "100201184,100001339,111301285,114301342,113301309,110301340,112301229,121001151,125002881,123001205,120001233,122001392";
    private static final String ranger = "101701060,111301285,114301342,113301309,110301340,112301229,121001151,125002881,123001205,120001233,122001392";
    private static final String sorcerer = "100501035,100601088,110101413,111101290,113101307,112101245,114101336,121001152,125002880,123001206,120001234,122001393";
    private static final String sm = "100501035,100601088,110101413,111101290,113101307,112101245,114101336,121001152,125002880,123001206,120001234,122001393";

    private static List<Integer> getRewardList(PlayerClass pc) {
        List<Integer> rewardList = new ArrayList<>();
        String rewardString;
        switch (pc) {
            case TEMPLAR:
                rewardString = templar;
                break;
            case GLADIATOR:
                rewardString = gladiator;
                break;
            case CLERIC:
                rewardString = cleric;
                break;
            case CHANTER:
                rewardString = chanter;
                break;
            case ASSASSIN:
                rewardString = assassin;
                break;
            case RANGER:
                rewardString = ranger;
                break;
            case SORCERER:
                rewardString = sorcerer;
                break;
            case SPIRIT_MASTER:
                rewardString = sm;
                break;
            default:
                rewardString = null;
        }

        if (rewardString != null) {
            String[] parts = rewardString.split(",");
            for (String part : parts) {
                rewardList.add(Integer.parseInt(part));
            }
        } else {
            log.warn("[PvP][Reward] There is no reward list for the {PlayerClass: " + pc.toString() + "}");
        }
        return rewardList;
    }

    public static int getRewardId(Player winner, Player victim, boolean isAdvanced) {
        int itemId = 0;
        if (victim.getSpreeLevel() > 2) {
            isAdvanced = true;
        }
        if (!isAdvanced) {
            int lvl = victim.getLevel();
            if (lvl <= 45) {
                itemId = PvPConfig.MEDAL_REWARD_ID_LV45;
            }
            if ((lvl > 45) && (lvl <= 50)) {
                itemId = PvPConfig.MEDAL_REWARD_ID_LV50;
            }
            if ((lvl > 50) && (lvl <= 55)) {
                itemId = PvPConfig.MEDAL_REWARD_ID_LV55;
            }
            if (lvl > 55) {
                itemId = PvPConfig.MEDAL_REWARD_ID_LV60;
            }
        } else {
            List<Integer> abyssItemsList = getAdvancedReward(winner);
            itemId = abyssItemsList.get(Rnd.get(abyssItemsList.size()));
        }
        return itemId;
    }

    public static float getMedalRewardChance(Player winner, Player victim) {
        float chance = PvPConfig.MEDAL_REWARD_CHANCE;
        chance += 1.5F * winner.getRawKillCount();
        int diff = victim.getLevel() - winner.getLevel();
        if (diff * diff > 100) {
            if (diff < 0) {
                diff = -10;
            } else {
                diff = 10;
            }
        }
        chance += 2.0F * diff;

        if ((victim.getSpreeLevel() > 0) || (chance > 100.0F)) {
            chance = 100.0F;
        }

        return chance;
    }

    public static int getRewardQuantity(Player winner, Player victim) {
        int rewardQuantity = winner.getSpreeLevel() + 1;
        switch (victim.getSpreeLevel()) {
            case 1:
                rewardQuantity += 5;
                break;
            case 2:
                rewardQuantity += 10;
                break;
            case 3:
                rewardQuantity += 15;
        }

        return rewardQuantity;
    }

    public static float getTollRewardChance(Player winner, Player victim) {
        float chance = PvPConfig.TOLL_REWARD_CHANCE;
        chance += 1.5F * winner.getRawKillCount();
        int diff = victim.getLevel() - winner.getLevel();
        if (diff * diff > 100) {
            if (diff < 0) {
                diff = -10;
            } else {
                diff = 10;
            }
        }
        chance += 2.0F * diff;

        if ((victim.getSpreeLevel() > 0) || (chance > 100.0F)) {
            chance = 100.0F;
        }
        return chance;
    }

    public static int getTollQuantity(Player winner, Player victim) {
        int tollQuantity = winner.getSpreeLevel() + 1;
        switch (victim.getSpreeLevel()) {
            case 1:
                tollQuantity += 5;
                break;
            case 2:
                tollQuantity += 10;
                break;
            case 3:
                tollQuantity += 15;
        }

        return tollQuantity;
    }

    private static List<Integer> getAdvancedReward(Player winner) {
        int lvl = winner.getLevel();
        PlayerClass pc = winner.getPlayerClass();
        List<Integer> rewardList = new ArrayList<>();

        if ((lvl >= 10) && (lvl < 70)) {
            rewardList.addAll(getFilteredRewardList(pc, 10, 70));
        }
        return rewardList;
    }

    private static List<Integer> getFilteredRewardList(PlayerClass pc, int minLevel, int maxLevel) {
        List<Integer> filteredRewardList = new ArrayList<>();
        List<Integer> rewardList = getRewardList(pc);

        for (Integer id : rewardList) {
            ItemTemplate itemTemp = DataManager.ITEM_DATA.getItemTemplate(id);
            if (itemTemp == null) {
                log.warn("[PvP][Reward] Incorrect {Item ID: " + id + "} reward for {PlayerClass: " + pc.toString() + "}");
            } else {
                int itemLevel = itemTemp.getLevel();

                if (itemLevel >= minLevel && itemLevel < maxLevel) {
                    filteredRewardList.add(id);
                }
            }
        }
        return filteredRewardList.size() > 0 ? filteredRewardList : new ArrayList<Integer>();
    }
}
