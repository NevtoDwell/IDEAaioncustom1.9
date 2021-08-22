/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.configs.main.PvPConfig;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.world.World;

public final class PvPSpreeService {

    private static final Logger log = LoggerFactory.getLogger("PVP_LOG");
    private static final String STRING_SPREE1 = "Кровавый путь";
    private static final String STRING_SPREE2 = "Кровавое безумие";
    private static final String STRING_SPREE3 = "Геноцид";

    public static void increaseRawKillCount(Player winner) {
        int currentRawKillCount = winner.getRawKillCount();
        winner.setRawKillCount(currentRawKillCount + 1);
        int newRawKillCount = currentRawKillCount + 1;
        PacketSendUtility.sendWhiteMessageOnCenter(winner, "Вы совершили " + newRawKillCount + " убийств подряд");

        if ((newRawKillCount == PvPConfig.SPREE_KILL_COUNT) || (newRawKillCount == PvPConfig.RAMPAGE_KILL_COUNT)
            || (newRawKillCount == PvPConfig.GENOCIDE_KILL_COUNT)) {
            if (newRawKillCount == PvPConfig.SPREE_KILL_COUNT) {
                updateSpreeLevel(winner, 1);
            }
            if (newRawKillCount == PvPConfig.RAMPAGE_KILL_COUNT) {
                updateSpreeLevel(winner, 2);
            }
            if (newRawKillCount == PvPConfig.GENOCIDE_KILL_COUNT) {
                updateSpreeLevel(winner, 3);
            }
        }
    }

    private static void updateSpreeLevel(Player winner, int level) {
        winner.setSpreeLevel(level);
        sendUpdateSpreeMessage(winner, level);
    }

    private static void sendUpdateSpreeMessage(Player winner, int level) {
        for (Player p : World.getInstance().getAllPlayers()) {
            if (level == 1) {
                PacketSendUtility.sendBrightYellowMessageOnCenter(p, winner.getName() + " расы " + winner.getCommonData().getRace().toString().toLowerCase()
                    + " ступает на " + STRING_SPREE1 + "! Берегитесь!");
            }
            if (level == 2) {
                PacketSendUtility.sendBrightYellowMessageOnCenter(p, winner.getName() + " расы " + winner.getCommonData().getRace().toString().toLowerCase()
                    + " начинает " + STRING_SPREE2 + " ! Скоро Ваш черед!");
            }
            if (level == 3) {
                PacketSendUtility.sendBrightYellowMessageOnCenter(p, winner.getName() + " расы " + winner.getCommonData().getRace().toString().toLowerCase()
                    + " совершает настоящий " + STRING_SPREE3 + " ! Бегите, жалкие смертные!");
            }
        }
        log.info("[PvP][Кровавое веселье] {Игрок : " + winner.getName() + "} перешел на уровень " + level + "!");
    }

    public static void cancelSpree(Player victim, Creature killer, boolean isPvPDeath) {
        int killsBeforeDeath = victim.getRawKillCount();
        victim.setRawKillCount(0);
        if (victim.getSpreeLevel() > 0) {
            victim.setSpreeLevel(0);
            sendEndSpreeMessage(victim, killer, isPvPDeath, killsBeforeDeath);
        }
    }

    private static void sendEndSpreeMessage(Player victim, Creature killer, boolean isPvPDeath, int killsBeforeDeath) {
        String spreeEnder = isPvPDeath ? killer.getName() : "Монстр";
        for (Player p : World.getInstance().getAllPlayers()) {
            PacketSendUtility.sendWhiteMessageOnCenter(p, "Кровожадный убийца " + victim.getName() + " остановлен игроком " + spreeEnder + " после "
                + killsBeforeDeath + " непрерывных убийств!");
        }
        log.info("[PvP][Кровавое веселье] {Кровожадный убийца " + victim.getName() + "} остановлен игроком " + spreeEnder + "}");
    }
}
