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

import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.skillengine.model.DispelCategoryType;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.SkillTargetSlot;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractDispelEffect")
public class AbstractDispelEffect extends EffectTemplate {

    @XmlAttribute
    protected int dpower;

    @XmlAttribute
    protected int power;

    @XmlAttribute(name = "dispel_level")
    protected int dispelLevel;

    public void applyEffect(Effect effect) {
    }

    public void applyEffect(Effect effect, DispelCategoryType type, SkillTargetSlot slot) {
        boolean isItemTriggered = effect.getItemTemplate() != null;
        int count = value + delta * effect.getSkillLevel();
        int finalPower = power + dpower * effect.getSkillLevel();

        int size = effect.getEffected().getEffectController().getAbnormalEffects().size();
        effect.getEffected().getEffectController().removeEffectByDispelCat(type, slot, count, dispelLevel, finalPower, isItemTriggered);

        if(size == effect.getEffected().getEffectController().getAbnormalEffects().size()){
            com.ne.gs.utils.PacketSendUtility.sendPck(effect.getEffector(), SM_SYSTEM_MESSAGE.STR_MSG_NOT_ENOUGH_DISPELCOUNT);
        }
    }
}
