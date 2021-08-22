/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.skillengine.model.ChainSkills;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.skillengine.model.SkillTemplate;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChainCondition")
public class ChainCondition extends Condition {

    @XmlAttribute(name = "selfcount")
    private int selfCount;

    @XmlAttribute(name = "precount")
    private int preCount;

    @XmlAttribute(name = "category")
    private String category;

    @XmlAttribute(name = "precategory")
    private String precategory;

    @XmlAttribute(name = "time")
    private int time;

    @Override
    public boolean validate(Skill env) {
        if ((env.getEffector() instanceof Player) && (precategory != null || selfCount > 0)) {
            Player pl = (Player) env.getEffector();
            ChainSkills cs = pl.getChainSkills();
            SkillTemplate st = env.getSkillTemplate();

            if (selfCount > 0) {
                boolean canUse = false;

                if (precategory != null && cs.chainSkillEnabled(precategory, time)) {
                    canUse = true;
                }

                if (cs.chainSkillEnabled(category, time)) {
                    canUse = true;
                } else if (precategory == null) {
                    canUse = true;
                }

                if (!canUse) {
                    return false;
                }

                if (selfCount <= cs.getChainCount(pl, st, category)) {
                    return false;
                }

                env.setIsMultiCast(true);
            } else if (preCount > 0) {
                if (!cs.chainSkillEnabled(precategory, time) || preCount != cs.getChainCount(pl, st, precategory)) {
                    return false;
                }
            } else if (!cs.chainSkillEnabled(precategory, time)) {
                return false;
            }
        }

        env.setChainCategory(category);
        return true;
    }

    public int getSelfCount() {
        return selfCount;
    }

    public String getCategory() {
        return category;
    }

    public int getTime() {
        return time;
    }
}
