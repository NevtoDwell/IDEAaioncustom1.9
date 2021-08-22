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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import gnu.trove.map.hash.TIntObjectHashMap;

import com.ne.gs.model.PlayerClass;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.stats.CalculatedPlayerStatsTemplate;
import com.ne.gs.model.templates.stats.PlayerStatsTemplate;

/**
 * Created on: 31.07.2009 14:20:03
 *
 * @author Aquanox
 */
@XmlRootElement(name = "player_stats_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlayerStatsData {

    @XmlElement(name = "player_stats", required = true)
    private List<PlayerStatsType> templatesList;

    private final TIntObjectHashMap<PlayerStatsTemplate> playerTemplates = new TIntObjectHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        if (templatesList == null) {
            return;
        }

        for (PlayerStatsType pt : templatesList) {
            int code = makeHash(pt.getRequiredPlayerClass(), pt.getRequiredLevel());
            PlayerStatsTemplate template = pt.getTemplate();
            //TODO move to DP
            template.setMaxMp(Math.round(template.getMaxMp() * 100f / template.getWill()));
            template.setMaxHp(Math.round(template.getMaxHp() * 100f / template.getHealth()));
            int agility = template.getAgility();
            agility = (agility - 100);
            template.setEvasion(Math.round(template.getEvasion() - template.getEvasion() * agility * 0.003f));
            template.setBlock(Math.round(template.getBlock() - template.getBlock() * agility * 0.0025f));
            template.setParry(Math.round(template.getParry() - template.getParry() * agility * 0.0025f));
            playerTemplates.put(code, pt.getTemplate());
        }

        /** for unknown templates **/
        playerTemplates.put(makeHash(PlayerClass.WARRIOR, 0), new CalculatedPlayerStatsTemplate(PlayerClass.WARRIOR));
        playerTemplates.put(makeHash(PlayerClass.ASSASSIN, 0), new CalculatedPlayerStatsTemplate(PlayerClass.ASSASSIN));
        playerTemplates.put(makeHash(PlayerClass.CHANTER, 0), new CalculatedPlayerStatsTemplate(PlayerClass.CHANTER));
        playerTemplates.put(makeHash(PlayerClass.CLERIC, 0), new CalculatedPlayerStatsTemplate(PlayerClass.CLERIC));
        playerTemplates.put(makeHash(PlayerClass.GLADIATOR, 0), new CalculatedPlayerStatsTemplate(PlayerClass.GLADIATOR));
        playerTemplates.put(makeHash(PlayerClass.MAGE, 0), new CalculatedPlayerStatsTemplate(PlayerClass.MAGE));
        playerTemplates.put(makeHash(PlayerClass.PRIEST, 0), new CalculatedPlayerStatsTemplate(PlayerClass.PRIEST));
        playerTemplates.put(makeHash(PlayerClass.RANGER, 0), new CalculatedPlayerStatsTemplate(PlayerClass.RANGER));
        playerTemplates.put(makeHash(PlayerClass.SCOUT, 0), new CalculatedPlayerStatsTemplate(PlayerClass.SCOUT));
        playerTemplates.put(makeHash(PlayerClass.SORCERER, 0), new CalculatedPlayerStatsTemplate(PlayerClass.SORCERER));
        playerTemplates.put(makeHash(PlayerClass.SPIRIT_MASTER, 0), new CalculatedPlayerStatsTemplate(
            PlayerClass.SPIRIT_MASTER));
        playerTemplates.put(makeHash(PlayerClass.TEMPLAR, 0), new CalculatedPlayerStatsTemplate(PlayerClass.TEMPLAR));

        templatesList = null;
    }

    /**
     * @param player
     *
     * @return
     */
    public PlayerStatsTemplate getTemplate(Player player) {
        PlayerStatsTemplate template = getTemplate(player.getCommonData().getPlayerClass(), player.getLevel());
        if (template == null) {
            template = getTemplate(player.getCommonData().getPlayerClass(), 0);
        }
        return template;
    }

    /**
     * @param playerClass
     * @param level
     *
     * @return
     */
    public PlayerStatsTemplate getTemplate(PlayerClass playerClass, int level) {
        PlayerStatsTemplate template = playerTemplates.get(makeHash(playerClass, level));
        if (template == null) {
            template = getTemplate(playerClass, 0);
        }
        return template;
    }

    /**
     * Size of player templates
     *
     * @return
     */
    public int size() {
        return playerTemplates.size();
    }

    @XmlRootElement(name = "playerStatsTemplateType")
    private static class PlayerStatsType {

        @XmlAttribute(name = "class", required = true)
        private PlayerClass requiredPlayerClass;
        @XmlAttribute(name = "level", required = true)
        private int requiredLevel;

        @XmlElement(name = "stats_template")
        private PlayerStatsTemplate template;

        public PlayerClass getRequiredPlayerClass() {
            return requiredPlayerClass;
        }

        public int getRequiredLevel() {
            return requiredLevel;
        }

        public PlayerStatsTemplate getTemplate() {
            return template;
        }
    }

    /**
     * @param playerClass
     * @param level
     *
     * @return
     */
    private static int makeHash(PlayerClass playerClass, int level) {
        return level << 8 | playerClass.ordinal();
    }
}
