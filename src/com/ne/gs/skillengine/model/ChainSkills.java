/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.model;

import java.util.Map;
import gnu.trove.map.hash.THashMap;

import com.ne.gs.model.gameobjects.player.Player;

public class ChainSkills {

    private final Map<String, ChainSkill> multiSkills = new THashMap<>();
    private final ChainSkill chainSkill = new ChainSkill("", 0, 0);

    public int getChainCount(Player player, SkillTemplate template, String category) {
        if (category == null) {
            return 0;
        }

        long nullTime = player.getSkillCoolDown(template.getCooldownId());
        ChainSkill cs = multiSkills.get(category);
        if (cs != null) {
            if (System.currentTimeMillis() >= nullTime && cs.getUseTime() <= nullTime) {
                cs.setChainCount(0);
            }

            return cs.getChainCount();
        }

        return 0;
    }

    public boolean chainSkillEnabled(String category, int time) {
        long useTime = 0L;
        ChainSkill cs = multiSkills.get(category);
        if (cs != null) {
            useTime = cs.getUseTime();
        } else if (chainSkill.getCategory().equals(category)) {
            useTime = chainSkill.getUseTime();
        }

        return useTime + time >= System.currentTimeMillis();
    }

    public void addChainSkill(String category, boolean multiCast) {
        if (multiCast) {
            if (multiSkills.get(category) != null) {
                multiSkills.get(category).increaseChainCount();
                multiSkills.get(category).setUseTime(System.currentTimeMillis());
            } else {
                multiSkills.put(category, new ChainSkill(category, 1, System.currentTimeMillis()));
            }
        } else {
            chainSkill.updateChainSkill(category);
        }
    }

    public void flush() {
        multiSkills.clear();
        chainSkill.setUseTime(0);
        chainSkill.setChainCount(0);
        chainSkill.setCategory("");
    }
}
