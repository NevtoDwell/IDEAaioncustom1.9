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

import com.ne.gs.skillengine.action.DamageType;
import com.ne.gs.skillengine.change.Func;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SkillAttackInstantEffect")
public class SkillAttackInstantEffect extends DamageEffect {

    @XmlAttribute
    protected int rnddmg;// TODO should be enum and different types of random damage behaviour
    @XmlAttribute
    protected boolean cannotmiss;

    /**
     * @return the rnddmg
     */
    public int getRnddmg() {
        return rnddmg;
    }

    @Override
    public Func getMode() {
        return mode;
    }

    @Override
    public void calculate(Effect effect) {
        super.calculate(effect, DamageType.PHYSICAL);
    }

    /**
     * @return the cannotmiss
     */
    public boolean isCannotmiss() {
        return cannotmiss;
    }

}
