/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.configs.main.MembershipConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.PlayerClass;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.QuestTemplate;
import com.ne.gs.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.ne.gs.network.aion.serverpackets.SM_QUEST_ACTION;
import com.ne.gs.network.aion.serverpackets.SM_QUEST_COMPLETED_LIST;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ATracer, sweetkr
 */
public final class ClassChangeService {

    private static final Logger log = LoggerFactory.getLogger(ClassChangeService.class);
    // TODO dialog enum

    /**
     * @param player
     */
    public static void showClassChangeDialog(Player player) {
        if (CustomConfig.ENABLE_SIMPLE_2NDCLASS) {
            PlayerClass playerClass = player.getPlayerClass();
            Race playerRace = player.getRace();
            if (player.getLevel() >= 9 && playerClass.isStartingClass()) {
                if (playerRace == Race.ELYOS) {
                    switch (playerClass) {
                        case WARRIOR:
                            player.sendPck(new SM_DIALOG_WINDOW(0, 2375, 1006));
                            break;
                        case SCOUT:
                            player.sendPck(new SM_DIALOG_WINDOW(0, 2716, 1006));
                            break;
                        case MAGE:
                            player.sendPck(new SM_DIALOG_WINDOW(0, 3057, 1006));
                            break;
                        case PRIEST:
                            player.sendPck(new SM_DIALOG_WINDOW(0, 3398, 1006));
                            break;
                    }
                } else if (playerRace == Race.ASMODIANS) {
                    switch (playerClass) {
                        case WARRIOR:
                            player.sendPck(new SM_DIALOG_WINDOW(0, 3057, 2008));
                            break;
                        case SCOUT:
                            player.sendPck(new SM_DIALOG_WINDOW(0, 3398, 2008));
                            break;
                        case MAGE:
                            player.sendPck(new SM_DIALOG_WINDOW(0, 3739, 2008));
                            break;
                        case PRIEST:
                            player.sendPck(new SM_DIALOG_WINDOW(0, 4080, 2008));
                            break;
                    }
                }
            }
        }
    }

    /**
     * @param player
     * @param dialogId
     */
    public static void changeClassToSelection(Player player, int dialogId) {
        Race playerRace = player.getRace();
        if (CustomConfig.ENABLE_SIMPLE_2NDCLASS) {
            if (playerRace == Race.ELYOS) {
                switch (dialogId) {
                    case 2376:
                        setClass(player, PlayerClass.getPlayerClassById(Byte.parseByte("1")));
                        break;
                    case 2461:
                        setClass(player, PlayerClass.getPlayerClassById(Byte.parseByte("2")));
                        break;
                    case 2717:
                        setClass(player, PlayerClass.getPlayerClassById(Byte.parseByte("4")));
                        break;
                    case 2802:
                        setClass(player, PlayerClass.getPlayerClassById(Byte.parseByte("5")));
                        break;
                    case 3058:
                        setClass(player, PlayerClass.getPlayerClassById(Byte.parseByte("7")));
                        break;
                    case 3143:
                        setClass(player, PlayerClass.getPlayerClassById(Byte.parseByte("8")));
                        break;
                    case 3399:
                        setClass(player, PlayerClass.getPlayerClassById(Byte.parseByte("10")));
                        break;
                    case 3484:
                        setClass(player, PlayerClass.getPlayerClassById(Byte.parseByte("11")));
                        break;
                }
                completeQuest(player, 1006);
                completeQuest(player, 1007);

                // Stigma Quests Elyos
                if (player.havePermission(MembershipConfig.STIGMA_SLOT_QUEST)) {
                    completeQuest(player, 1929);
                }
            } else if (playerRace == Race.ASMODIANS) {
                switch (dialogId) {
                    case 3058:
                        setClass(player, PlayerClass.getPlayerClassById(Byte.parseByte("1")));
                        break;
                    case 3143:
                        setClass(player, PlayerClass.getPlayerClassById(Byte.parseByte("2")));
                        break;
                    case 3399:
                        setClass(player, PlayerClass.getPlayerClassById(Byte.parseByte("4")));
                        break;
                    case 3484:
                        setClass(player, PlayerClass.getPlayerClassById(Byte.parseByte("5")));
                        break;
                    case 3740:
                        setClass(player, PlayerClass.getPlayerClassById(Byte.parseByte("7")));
                        break;
                    case 3825:
                        setClass(player, PlayerClass.getPlayerClassById(Byte.parseByte("8")));
                        break;
                    case 4081:
                        setClass(player, PlayerClass.getPlayerClassById(Byte.parseByte("10")));
                        break;
                    case 4166:
                        setClass(player, PlayerClass.getPlayerClassById(Byte.parseByte("11")));
                        break;
                }
                completeQuest(player, 2008);
                completeQuest(player, 2009);

                // Stigma Quests Asmodians
                if (player.havePermission(MembershipConfig.STIGMA_SLOT_QUEST)) {
                    completeQuest(player, 2900);
                }
            }

            int[] quests = player.getRace() == Race.ASMODIANS ?
                    CustomConfig.SECONDCLASS_AQUESTS :
                    CustomConfig.SECONDCLASS_EQUESTS;

            if (quests != null) {
                for (int questId : quests) {
                    completeQuest(player, questId);
                }

                player.sendPck(new SM_QUEST_COMPLETED_LIST(player.getQuestStateList().getAllFinishedQuests()));
            }

            String param = CustomConfig.SECONDCLASS_ESKILLS;
            if (player.getRace() == Race.ASMODIANS) {
                param = CustomConfig.SECONDCLASS_ASKILLS;
            }

            if (param != null && !param.isEmpty()) {
                for (String skill : param.split(",")) {
                    String[] tokens = skill.split(":");

                    int skillId = Integer.parseInt(tokens[0]);
                    int skillLevel = Integer.parseInt(tokens[1]);

                    player.getSkillList().addSkill(player, skillId, skillLevel);
                }
            }
        }
    }

    private static void completeQuest(Player player, int questId) {

        QuestTemplate qt = DataManager.QUEST_DATA.getQuestById(questId);
        if(qt == null)
        {
            log.warn("No such quest: {}", questId);
            return;
        }

        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null) {
            player.getQuestStateList().addQuest(questId, new QuestState(questId, QuestStatus.COMPLETE, 0, 0, null, 0, null));
            player.sendPck(new SM_QUEST_ACTION(questId, QuestStatus.COMPLETE.value(), 0));
        } else {
            qs.setStatus(QuestStatus.COMPLETE);
            player.sendPck(new SM_QUEST_ACTION(questId, qs.getStatus(), qs.getQuestVars().getQuestVars()));
        }
    }

    public static void setClass(Player player, PlayerClass playerClass) {
        if (validateSwitch(player, playerClass)) {
            player.getCommonData().setPlayerClass(playerClass);
            player.getController().upgradePlayer();
            player.sendPck(new SM_DIALOG_WINDOW(0, 0, 0));
            if (CustomConfig.SECONDCLASS_LEVEL > 0)
                player.getCommonData().setLevel(CustomConfig.SECONDCLASS_LEVEL);
        }
    }

    private static boolean validateSwitch(Player player, PlayerClass playerClass) {
        int level = player.getLevel();
        PlayerClass oldClass = player.getPlayerClass();
        if (level < 9) {
            player.sendMsg("You can only switch class at level 9");
            return false;
        }
        if (!oldClass.isStartingClass()) {
            player.sendMsg("You already switched class");
            return false;
        }
        switch (oldClass) {
            case WARRIOR:
                if (playerClass == PlayerClass.GLADIATOR || playerClass == PlayerClass.TEMPLAR) {
                    break;
                }
            case SCOUT:
                if (playerClass == PlayerClass.ASSASSIN || playerClass == PlayerClass.RANGER) {
                    break;
                }
            case MAGE:
                if (playerClass == PlayerClass.SORCERER || playerClass == PlayerClass.SPIRIT_MASTER) {
                    break;
                }
            case PRIEST:
                if (playerClass == PlayerClass.CLERIC || playerClass == PlayerClass.CHANTER) {
                    break;
                }
            default:
                player.sendMsg("Invalid class switch chosen");
                return false;
        }
        return true;
    }
}
