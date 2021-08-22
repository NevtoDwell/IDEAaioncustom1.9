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

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_TRANSFORM;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.TransformType;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author Sweetkr, kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TransformEffect")
public abstract class TransformEffect extends EffectTemplate {

    @XmlAttribute
    protected int model;

    @XmlAttribute
    protected TransformType type = TransformType.NONE;

    @XmlAttribute
    protected int panelid;

    @Override
    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
    }

    public void endEffect(Effect effect, AbnormalState state) {
        Creature effected = effect.getEffected();

        if (state != null) {
            effected.getEffectController().unsetAbnormal(state.getId());
        }

        if (effected instanceof Player) {
            int newModel = 0;
            TransformType transformType = TransformType.PC;
            for (Effect tmp : effected.getEffectController().getAbnormalEffects()) {
                for (EffectTemplate template : tmp.getEffectTemplates()) {
                    if (template instanceof TransformEffect) {
                        if (((TransformEffect) template).getTransformId() == model) {
                            continue;
                        }
                        newModel = ((TransformEffect) template).getTransformId();
                        transformType = ((TransformEffect) template).getTransformType();
                        break;
                    }
                }
            }
            effected.getTransformModel().setModelId(newModel);
            effected.getTransformModel().setTransformType(transformType);
        } else if (effected instanceof Summon) {
            effected.getTransformModel().setModelId(0);
        } else if (effected instanceof Npc) {
            effected.getTransformModel().setModelId(effected.getObjectTemplate().getTemplateId());
        }
        effected.getTransformModel().setPanelId(0);
        PacketSendUtility.broadcastPacketAndReceive(effected, new SM_TRANSFORM(effected, 0, false));

        if (effected instanceof Player) {
            ((Player) effected).setTransformed(false);
        }
    }

    public void startEffect(Effect effect, AbnormalState effectId) {
        Creature effected = effect.getEffected();

        if (effectId != null) {
            effect.setAbnormal(effectId.getId());
            effected.getEffectController().setAbnormal(effectId.getId());
        }

        effected.getTransformModel().setModelId(model);
        effected.getTransformModel().setPanelId(panelid);
        effected.getTransformModel().setTransformType(effect.getTransformType());
        PacketSendUtility.broadcastPacketAndReceive(effected, new SM_TRANSFORM(effected, panelid, true));

        if (effected instanceof Player) {
            ((Player) effected).setTransformed(true);
        }
    }

    public TransformType getTransformType() {
        return type;
    }

    public int getTransformId() {
        return model;
    }

    public int getPanelId() {
        return panelid;
    }
}
