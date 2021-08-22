/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.abyss;

import com.ne.gs.model.gameobjects.player.AbyssRank;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.utils.stats.AbyssRankEnum;

/**
 * @author ATracer
 */
public final class AbyssSkillService {

    /**
     * @param player
     */
    public static void updateSkills(Player player) {
        AbyssRank abyssRank = player.getAbyssRank();
        if (abyssRank == null) {
            return;
        }
        AbyssRankEnum rankEnum = abyssRank.getRank();
        // remove all abyss skills first
        for (AbyssSkills abyssSkill : AbyssSkills.values()) {
            if (abyssSkill.getRace() == player.getRace()) {
                for (int skillId : abyssSkill.getSkills()) {
                    player.getSkillList().removeSkill(skillId);
                }
            }
        }
        // add new skills
        if (abyssRank.getRank().getId() >= AbyssRankEnum.STAR5_OFFICER.getId()) {
            for (int skillId : AbyssSkills.getSkills(player.getRace(), rankEnum)) {
                player.getSkillList().addTemporarySkill(player, skillId, 1);
            }
        }
    }

    /**
     * @param player
     */
    public static void onEnterWorld(Player player) {
        updateSkills(player);
    }
}
