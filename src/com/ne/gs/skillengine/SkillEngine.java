/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.skillengine.model.ActivationAttribute;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.skillengine.model.SkillTemplate;

/**
 * @author ATracer
 */
public class SkillEngine {

    public static final SkillEngine skillEngine = new SkillEngine();

    /**
     * should not be instantiated directly
     */
    private SkillEngine() {

    }

    /**
     * This method is used for skills that were learned by player
     *
     * @param player
     * @param skillId
     *
     * @return Skill
     */
    public Skill getSkillFor(Player player, int skillId, VisibleObject firstTarget) {
        SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skillId);

        if (template == null) {
            return null;
        }

        return getSkillFor(player, template, firstTarget);
    }

    /**
     * This method is used for skills that were learned by player
     *
     * @param player
     * @param template
     * @param firstTarget
     *
     * @return
     */
    public Skill getSkillFor(Player player, SkillTemplate template, VisibleObject firstTarget) {
        // player doesn't have such skill and ist not provoked
        if (template.getActivationAttribute() != ActivationAttribute.PROVOKED) {
            if (!player.getSkillList().isSkillPresent(template.getSkillId())) {
                return null;
            }
        }

        Creature target = null;
        if (firstTarget instanceof Creature) {
            target = (Creature) firstTarget;
        }

        return new Skill(template, player, target);
    }

    public Skill getSkillFor(Player player, SkillTemplate template, VisibleObject firstTarget, int skillLevel) {
        Creature target = null;
        if ((firstTarget instanceof Creature)) {
            target = (Creature) firstTarget;
        }
        return new Skill(template, player, target, skillLevel);
    }

    /**
     * This method is used for not learned skills (item skills etc)
     *
     * @param creature
     * @param skillId
     * @param skillLevel
     *
     * @return Skill
     */
    public Skill getSkill(Creature creature, int skillId, int skillLevel, VisibleObject firstTarget) {
        return getSkill(creature, skillId, skillLevel, firstTarget, null);
    }

    public Skill getSkill(Creature creature, int skillId, int skillLevel, VisibleObject firstTarget, ItemTemplate itemTemplate) {
        SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skillId);

        if (template == null) {
            return null;
        }

        Creature target = null;
        if (firstTarget instanceof Creature) {
            target = (Creature) firstTarget;
        }
        return new Skill(template, creature, skillLevel, target, itemTemplate);
    }

    public static SkillEngine getInstance() {
        return skillEngine;
    }

    /**
     * This method is used to apply directly effect of given skill without checking properties, sending packets, etc
     * Should be only used from quest scripts, or when you are sure about it
     *
     * @param skillId
     * @param effector
     * @param effected
     * @param duration
     *     => 0 takes duration from skill_templates, >0 forced duration
     */
    public void applyEffectDirectly(int skillId, Creature effector, Creature effected, int duration) {
        SkillTemplate st = DataManager.SKILL_DATA.getSkillTemplate(skillId);
        if (st == null) {
            return;
        }
        Effect ef = new Effect(effector, effected, st, st.getLvl(), duration);
        ef.setIsForcedEffect(true);
        ef.initialize();
        if (duration > 0) {
            ef.setForcedDuration(true);
        }
        ef.applyEffect();
    }
}
