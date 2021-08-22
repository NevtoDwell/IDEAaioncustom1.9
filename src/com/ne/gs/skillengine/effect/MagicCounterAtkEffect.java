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
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.container.CreatureLifeStats;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.skillengine.model.SkillSubType;
import com.ne.gs.skillengine.model.SkillType;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author ViAl
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MagicCounterAtkEffect")
public class MagicCounterAtkEffect extends EffectTemplate {

    @XmlAttribute
    protected int maxdmg;

    private float modifiedValue;

    // TODO bosses are resistent to this?

    @Override
    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
    }

    @Override
    public void startEffect(final Effect effect) {
        final Creature effector = effect.getEffector();
        final Creature effected = effect.getEffected();
        final CreatureLifeStats<? extends Creature> cls = effect.getEffected().getLifeStats();
        applyModifier(effect);

        ActionObserver observer = new ActionObserver(ObserverType.SKILLUSE) {

            public void skilluse(final Skill skill) {
                ThreadPoolManager.getInstance().schedule(new Runnable() {

                    @Override
                    public void run() {

                        if (skill.getSkillTemplate().getType() == SkillType.MAGICAL) {
                            if ((int) (cls.getMaxHp() / 100f * modifiedValue) <= maxdmg) {
                                effected.getController().onAttack(effector, effect.getSkillId(), TYPE.DAMAGE, (int) (cls.getMaxHp() / 100f * modifiedValue), true, LOG.REGULAR,  effect.getAttackStatus());
                            } else {
                                effected.getController().onAttack(effector, maxdmg, true);
                            }
                        }
                    }
                }, 0);

            }
        };

        effect.setActionObserver(observer, position);
        effected.getObserveController().addObserver(observer);
    }

    @Override
    public void endEffect(Effect effect) {
        ActionObserver observer = effect.getActionObserver(position);
        if (observer != null) {
            effect.getEffected().getObserveController().removeObserver(observer);
        }
    }

    private void applyModifier(Effect effect) {
        modifiedValue = value;
        if(effect.getEffected() instanceof Player) {
            switch(effect.getSkillId()) {
                case 1560:
                    modifiedValue = 3.1f;
                    break;
                case 1561:
                    modifiedValue = 6.4f;
                    break;
                case 2196:
                    modifiedValue = 7.75f;
            }
        }
    }
}
