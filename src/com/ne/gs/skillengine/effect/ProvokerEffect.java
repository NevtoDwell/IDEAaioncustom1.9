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

import com.ne.commons.utils.Rnd;
import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.ObserverType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.siege.SiegeNpc;
import com.ne.gs.skillengine.SkillEngine;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.ProvokeTarget;
import com.ne.gs.utils.MathUtil;

/**
 * @author ATracer modified by kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProvokerEffect")
public class ProvokerEffect extends ShieldEffect {

    @XmlAttribute(name = "provoke_target")
    protected ProvokeTarget provokeTarget;
    @XmlAttribute(name = "skill_id")
    protected int skillId;

    @Override
    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
    }

    @Override
    public void startEffect(Effect effect) {
        ActionObserver observer = null;
        final Creature effector = effect.getEffector();
        final int prob2 = this.hitTypeProb;
        final int radius = this.radius;
        switch (this.hitType) {
            case NMLATK:// ATTACK
                observer = new ActionObserver(ObserverType.ATTACK) {

                    @Override
                    public void attack(Creature creature) {
                        if (Rnd.chance(prob2)) {
                            Creature target = getProvokeTarget(provokeTarget, effector, creature);
                            createProvokedEffect(effector, target);
                        }
                    }

                };
                break;
            case EVERYHIT:// ATTACKED
                observer = new ActionObserver(ObserverType.ATTACKED) {

                    @Override
                    public void attacked(Creature creature) {
                        if (radius > 0) {
                            if (!MathUtil.isIn3dRange(effector, creature, radius)) {
                                return;
                            }
                        }
                        if (Rnd.chance(prob2)) {
                            Creature target = getProvokeTarget(provokeTarget, effector, creature);
                            createProvokedEffect(effector, target);
                        }
                    }
                };
                break;
            // TODO MAHIT and PHHIT
        }

        if (observer == null) {
            return;
        }

        effect.setActionObserver(observer, position);
        effect.getEffected().getObserveController().addObserver(observer);
    }

    /**
     * @param effector
     * @param target
     */
    private void createProvokedEffect(Creature effector, Creature target) {
        /**
         * I dont see a reason for such code boolean isTargetRelationEnemy = (template.getProperties() == null) ? false :
         * (template.getProperties().getTargetRelation() == TargetRelationAttribute.ENEMY); Effect e = null; if
         * ((isTargetRelationEnemy) && (provokeTarget == ProvokeTarget.ME)) e = new Effect(attacker, target, template,
         * template.getLvl(), template.getEffectsDuration()); else e = new Effect(effector, target, template,
         * template.getLvl(), template.getEffectsDuration());
         */
    	if(effector instanceof SiegeNpc) {
    		if(effector.getAi2().getName().equalsIgnoreCase("siege_protector"))
        		return;
    	}
        SkillEngine.getInstance().applyEffectDirectly(skillId, effector, target, 0);
    }

    /**
     * @param provokeTarget
     * @param effector
     * @param target
     *
     * @return
     */
    private Creature getProvokeTarget(ProvokeTarget provokeTarget, Creature effector, Creature target) {
        switch (provokeTarget) {
            case ME:
                return effector;
            case OPPONENT:
                return target;
        }
        throw new IllegalArgumentException("Provoker target is invalid " + provokeTarget);
    }

    @Override
    public void endEffect(Effect effect) {
        ActionObserver observer = effect.getActionObserver(position);
        if (observer != null) {
            effect.getEffected().getObserveController().removeObserver(observer);
        }
    }
}
