/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.controllers.SummonController;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_SUMMON_USESKILL;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author ATracer modified by Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PetOrderUseUltraSkillEffect")
public class PetOrderUseUltraSkillEffect extends EffectTemplate {

    @XmlAttribute
    protected boolean release;

    @Override
    public void applyEffect(Effect effect) {
        Player effector = (Player) effect.getEffector();

        if (effector.getSummon() == null) {
            return;
        }

        int effectorId = effector.getSummon().getObjectId();

        int npcId = effector.getSummon().getNpcId();
        int orderSkillId = effect.getSkillId();

        int petUseSkillId = DataManager.PET_SKILL_DATA.getPetOrderSkill(orderSkillId, npcId);
        int targetId = effect.getEffected().getObjectId();

        // Handle automatic release if skill expects so
        if (release) {
            SummonController controller = effector.getSummon().getController();
            if ((controller instanceof SummonController)) {
                effector.getSummon().getController().setReleaseAfterSkill(petUseSkillId);
            }
        }
        effector.sendPck(new SM_SUMMON_USESKILL(effectorId, petUseSkillId, 1, targetId));
    }

    @Override
    public void calculate(Effect effect) {
        if (effect.getEffector() instanceof Player && effect.getEffected() != null) {
            super.calculate(effect, null, null);
        }
    }
}
