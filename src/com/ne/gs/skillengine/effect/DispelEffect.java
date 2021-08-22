/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.effect;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.skillengine.model.DispelType;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.SkillTargetSlot;

/**
 * @author ATracer
 */
public class DispelEffect extends EffectTemplate {

    @XmlElement(type = Integer.class)
    protected List<Integer> effectids;
    @XmlElement
    protected List<String> effecttype;
    @XmlElement
    protected List<String> slottype;
    @XmlAttribute
    protected DispelType dispeltype;
    @XmlAttribute
    protected Integer value;

    @Override
    public void applyEffect(Effect effect) {
        if (effect.getEffected() == null || effect.getEffected().getEffectController() == null) {
            return;
        }

        if (dispeltype == null) {
            return;
        }

        if ((dispeltype == DispelType.EFFECTID || dispeltype == DispelType.EFFECTIDRANGE) && effectids == null) {
            return;
        }

        if (dispeltype == DispelType.EFFECTTYPE && effecttype == null) {
            return;
        }

        if (dispeltype == DispelType.SLOTTYPE && slottype == null) {
            return;
        }

        switch (dispeltype) {
            case EFFECTID:
                for (Integer effectId : effectids) {
                    effect.getEffected().getEffectController().removeEffectByEffectId(effectId);
                }
                break;
            case EFFECTIDRANGE:
                for (int i = effectids.get(0); i <= effectids.get(1); i++) {
                    effect.getEffected().getEffectController().removeEffectByEffectId(i);
                }
                break;
            case EFFECTTYPE:
                if (effecttype == null) {
                    return;
                }
                for (String type : effecttype) {
                    AbnormalState abnormalType = AbnormalState.getIdByName(type);
                    if (abnormalType != null && effect.getEffected().getEffectController().isAbnormalSet(abnormalType)) {
                        for (Effect ef : effect.getEffected().getEffectController().getAbnormalEffects()) {
                            if ((ef.getAbnormals() & abnormalType.getId()) == abnormalType.getId()) {
                                ef.endEffect();
                            }
                        }
                    }
                }
                break;
            case SLOTTYPE:
                for (String type : slottype) {
                    effect.getEffected().getEffectController().removeAbnormalEffectsByTargetSlot(SkillTargetSlot.valueOf(type));
                }
                break;
        }
    }
}
