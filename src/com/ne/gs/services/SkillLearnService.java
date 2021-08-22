/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import com.ne.gs.configs.main.MembershipConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.PlayerClass;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.skill.PlayerSkillList;
import com.ne.gs.network.aion.serverpackets.SM_SKILL_LIST;
import com.ne.gs.network.aion.serverpackets.SM_SKILL_REMOVE;
import com.ne.gs.skillengine.model.SkillLearnTemplate;

/**
 * @author ATracer, xTz
 */
public final class SkillLearnService {

    /**
     * @param player
     */
    public static void addNewSkills(Player player) {
        int level = player.getCommonData().getLevel();
        PlayerClass playerClass = player.getCommonData().getPlayerClass();
        Race playerRace = player.getRace();

        if (level == 10 && player.getSkillList().getSkillEntry(30001) != null) {
            int skillLevel = player.getSkillList().getSkillLevel(30001);
            removeSkill(player, 30001);
            player.sendPck(new SM_SKILL_LIST(player));
            player.getSkillList().addSkill(player, 30002, skillLevel);
        }
        addSkills(player, level, playerClass, playerRace);
    }

    /**
     * Recursively check missing skills and add them to player
     *
     * @param player
     */
    public static void addMissingSkills(Player player) {
        int level = player.getCommonData().getLevel();
        PlayerClass playerClass = player.getCommonData().getPlayerClass();
        Race playerRace = player.getRace();

        for (int i = 0; i <= level; i++) {
            addSkills(player, i, playerClass, playerRace);
        }

        if (!playerClass.isStartingClass()) {
            PlayerClass startinClass = PlayerClass.getStartingClassFor(playerClass);

            for (int i = 1; i < 10; i++) {
                addSkills(player, i, startinClass, playerRace);
            }

            if (player.getSkillList().getSkillEntry(30001) != null) {
                int skillLevel = player.getSkillList().getSkillLevel(30001);
                player.getSkillList().removeSkill(30001);
                player.sendPck(new SM_SKILL_LIST(player));
                player.getSkillList().addSkill(player, 30002, skillLevel);
            }
        }
    }

    /**
     * Adds skill to player according to the specified level, class and race
     *
     * @param player
     * @param level
     * @param playerClass
     * @param playerRace
     */
    private static void addSkills(Player player, int level, PlayerClass playerClass,
                                  Race playerRace) {
        SkillLearnTemplate[] skillTemplates = DataManager.SKILL_TREE_DATA
            .getTemplatesFor(playerClass, level, playerRace);
        PlayerSkillList playerSkillList = player.getSkillList();

        for (SkillLearnTemplate template : skillTemplates) {
            if (!checkLearnIsPossible(player, playerSkillList, template)) {
                continue;
            }

            playerSkillList.addSkill(player, template.getSkillId(), template.getSkillLevel());
        }
    }

    /**
     * Check SKILL_AUTOLEARN property Check skill already learned Check skill template auto-learn attribute
     *
     * @param playerSkillList
     * @param template
     *
     * @return
     */
    private static boolean checkLearnIsPossible(Player player, PlayerSkillList playerSkillList,
                                                SkillLearnTemplate template) {
        boolean skill = player.havePermission(MembershipConfig.SKILL_AUTOLEARN);
        boolean stigma = player.havePermission(MembershipConfig.STIGMA_AUTOLEARN);

        return playerSkillList.isSkillPresent(template.getSkillId())
            || (skill && !template.isStigma())
            || (stigma && template.isStigma())
            || template.isAutolearn();
    }

    public static void learnSkillBook(Player player, int skillId) {
        SkillLearnTemplate[] skillTemplates;
        int maxLevel = 0;

        for (int i = 1; i <= player.getLevel(); i++) {
            skillTemplates = DataManager.SKILL_TREE_DATA.getTemplatesFor(player.getPlayerClass(), i, player.getRace());

            for (SkillLearnTemplate skill : skillTemplates) {
                if (skillId == skill.getSkillId()) {
                    if (skill.getSkillLevel() > maxLevel) {
                        maxLevel = skill.getSkillLevel();
                    }
                }
            }
        }
        player.getSkillList().addSkill(player, skillId, maxLevel);
    }

    public static void removeSkill(Player player, int skillId) {
        if (player.getSkillList().isSkillPresent(skillId)) {
            Integer skillLevel = player.getSkillList().getSkillLevel(skillId);
            if (skillLevel == 0) {
                skillLevel = 1;
            }
            player.sendPck(new SM_SKILL_REMOVE(skillId, skillLevel, player.getSkillList()
                .getSkillEntry(skillId).isStigma()));
            player.getSkillList().removeSkill(skillId);
        }
    }

    public static int getSkillLearnLevel(int skillId, int playerLevel, int wantedSkillLevel) {
        SkillLearnTemplate[] skillTemplates = DataManager.SKILL_TREE_DATA.getTemplatesForSkill(skillId);
        int learnFinishes;
        int maxLevel = 0;

        for (SkillLearnTemplate template : skillTemplates) {
            if (maxLevel < template.getSkillLevel()) {
                maxLevel = template.getSkillLevel();
            }
        }

        // no data in skill tree, use as wanted
        if (maxLevel == 0) {
            return wantedSkillLevel;
        }

        learnFinishes = playerLevel + maxLevel;

        if (learnFinishes > DataManager.PLAYER_EXPERIENCE_TABLE.getMaxLevel()) {
            learnFinishes = DataManager.PLAYER_EXPERIENCE_TABLE.getMaxLevel();
        }

        return Math.max(wantedSkillLevel, Math.min(playerLevel - (learnFinishes - maxLevel) + 1, maxLevel));
    }

    public static int getSkillMinLevel(int skillId, int playerLevel, int wantedSkillLevel) {
        SkillLearnTemplate[] skillTemplates = DataManager.SKILL_TREE_DATA.getTemplatesForSkill(skillId);
        SkillLearnTemplate foundTemplate = null;

        for (SkillLearnTemplate template : skillTemplates) {
            if (template.getSkillLevel() <= wantedSkillLevel && template.getMinLevel() <= playerLevel) {
                foundTemplate = template;
            }
        }

        if (foundTemplate == null) {
            return playerLevel;
        }

        return foundTemplate.getMinLevel();
    }

}
