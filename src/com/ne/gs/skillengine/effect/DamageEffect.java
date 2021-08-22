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

import com.ne.gs.controllers.attack.AttackUtil;
import com.ne.gs.skillengine.action.DamageType;
import com.ne.gs.skillengine.change.Func;
import com.ne.gs.skillengine.effect.modifier.ActionModifier;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DamageEffect")
public abstract class DamageEffect extends EffectTemplate {

    @XmlAttribute
    protected Func mode = Func.ADD;

    @Override
    public void applyEffect(Effect effect) {
        effect.getEffected().getController().onAttack(effect.getEffector(), effect.getSkillId(), effect.getReserved1(), true, effect.getAttackStatus());
        effect.getEffector().getObserveController().notifyAttackObservers(effect.getEffected());
    }

    public boolean calculate(Effect effect, DamageType damageType) {
        return calculate(effect, damageType, false);
    }

    public boolean calculate(Effect effect, DamageType damageType, boolean ignoreResist) {
        if (!super.calculate(effect, null, null, ignoreResist)) {
            return false;
        }

        int skillLvl = effect.getSkillLevel();
        int valueWithDelta = value + delta * skillLvl;
        ActionModifier modifier = getActionModifiers(effect);
        int accMod = this.accMod2 + this.accMod1 * skillLvl;
        int critAddDmg = critAddDmg2 + critAddDmg1 * skillLvl;

        switch (damageType) {
            case PHYSICAL:
                int rndDmg = (this instanceof SkillAttackInstantEffect ? ((SkillAttackInstantEffect) this).getRnddmg() : 0);
                AttackUtil.calculateSkillResult(effect, valueWithDelta, modifier, getMode(), rndDmg, accMod, critProbMod2, critAddDmg);
                break;
            case MAGICAL:
                boolean useKnowledge = true;
                if (this instanceof ProcAtkInstantEffect) {
                    useKnowledge = false;
                }
                AttackUtil.calculateMagicalSkillResult(effect, valueWithDelta, modifier, getElement(), true, useKnowledge, false, getMode(), critProbMod2, critAddDmg);
                break;
            default:
                AttackUtil.calculateSkillResult(effect, 0, null, getMode(), 0, accMod, 100, 0);
        }

        return true;
    }

    public Func getMode() {
        return mode;
    }

}
