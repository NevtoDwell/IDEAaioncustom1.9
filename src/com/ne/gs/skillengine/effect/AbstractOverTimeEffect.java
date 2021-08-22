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
import java.util.concurrent.Future;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractOverTimeEffect")
public abstract class AbstractOverTimeEffect extends EffectTemplate {

    @XmlAttribute(required = true)
    protected int checktime;
    @XmlAttribute
    protected boolean percent;

    public int getValue() {
        return value;
    }

    @Override
    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
    }

    @Override
    public void startEffect(Effect effect) {
        this.startEffect(effect, null);
    }

    public void startEffect(final Effect effect, AbnormalState abnormal) {
        Creature effected = effect.getEffected();

        if (abnormal != null) {
            effect.setAbnormal(abnormal.getId());
            effected.getEffectController().setAbnormal(abnormal.getId());
        }

        if (checktime == 0) {
            return;
        }
        try {
            Future<?> task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    onPeriodicAction(effect);
                }
            }, checktime, checktime);
            effect.setPeriodicTask(task, position);
        } catch (Exception e) {
            log.warn("Exception in skillId: " + effect.getSkillId());
            e.printStackTrace();
        }
    }

    public void endEffect(Effect effect, AbnormalState abnormal) {
        if (abnormal != null) {
            effect.getEffected().getEffectController().unsetAbnormal(abnormal.getId());
        }
    }

}
