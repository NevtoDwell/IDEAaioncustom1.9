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

import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.ObserverType;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.HealType;
import com.ne.gs.skillengine.model.SkillTemplate;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CondSkillLauncherEffect")
public class CondSkillLauncherEffect extends EffectTemplate {

    @XmlAttribute(name = "skill_id")
    protected int skillId;
    @XmlAttribute
    protected HealType type;

    // TODO what if you fall? effect is not applied? what if you use skill that consume hp?
    @Override
    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
    }

    @Override
    public void endEffect(Effect effect) {
        effect.getEffected().getGameStats().endEffect(effect);
        ActionObserver observer = effect.getActionObserver(position);
        effect.getEffected().getObserveController().removeObserver(observer);
    }

    @Override
    public void startEffect(final Effect effect) {
        ActionObserver observer = new ActionObserver(ObserverType.ATTACKED) {
            @Override
            public void attacked(Creature creature) {
                ThreadPoolManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        if (!effect.getEffected().getEffectController().hasAbnormalEffect(skillId)) {
                            if (effect.getEffected().getLifeStats().getCurrentHp() <= (int) (value / 100f * effect.getEffected()
                                    .getLifeStats().getMaxHp())) {
                                SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skillId);
                                Effect e = new Effect(effect.getEffector(), effect.getEffected(), template, template.getLvl(), 0);
                                e.initialize();
                                e.applyEffect();
                            }
                        }
                    }
                }, 500);
            }
        };
        effect.getEffected().getObserveController().addObserver(observer);
        effect.setActionObserver(observer, position);
    }
}
