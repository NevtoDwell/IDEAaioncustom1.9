/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.skillengine.SkillEngine;
import com.ne.gs.skillengine.effect.EffectTemplate;
import com.ne.gs.skillengine.effect.SummonEffect;
import com.ne.gs.skillengine.effect.TransformEffect;
import com.ne.gs.skillengine.model.Skill;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SkillUseAction")
public class SkillUseAction extends AbstractItemAction {

    @XmlAttribute
    protected int skillid;
    @XmlAttribute
    protected int level;

    /**
     * Gets the value of the skillid property.
     */
    public int getSkillid() {
        return skillid;
    }

    /**
     * Gets the value of the level property.
     */
    public int getLevel() {
        return level;
    }

    @Override
    public boolean canAct(Player player, Item parentItem, Item targetItem) {
        Skill skill = SkillEngine.getInstance().getSkill(player, skillid, level, player.getTarget(),
            parentItem.getItemTemplate());
        if (skill == null) {
            return false;
        }
        // Cant use transform items while already transformed
        if (player.isTransformed()) {
            for (EffectTemplate template : skill.getSkillTemplate().getEffects().getEffects()) {
                if (template instanceof TransformEffect) {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_CANT_USE_ITEM(DescId.of(parentItem.getItemTemplate().getNameId())));
                    return false;
                }
            }
        }
        if (player.getSummon() != null) {
            for (EffectTemplate template : skill.getSkillTemplate().getEffects().getEffects()) {
                if (template instanceof SummonEffect) {
                    player.sendPck(new SM_SYSTEM_MESSAGE(1300072));
                    return false;
                }
            }
        }

//        if (!RestrictionsManager.canUseSkill(player, skill)) {
//            return false;
//        }

        return skill.canUseSkill();
    }

    @Override
    public void act(Player player, Item parentItem, Item targetItem) {
        Skill skill = SkillEngine.getInstance().getSkill(player, skillid, level, player.getTarget(),
            parentItem.getItemTemplate());
        if (skill != null) {
        	if(isPetDopingAction())
        		skill.setPetDopingSkill(true);
            player.getController().cancelUseItem();
            skill.setItemObjectId(parentItem.getObjectId());
            skill.useSkill();
            QuestEnv env = new QuestEnv(player.getTarget(), player, 0, 0);
            QuestEngine.getInstance().onUseSkill(env, skillid);
        }
    }

}
