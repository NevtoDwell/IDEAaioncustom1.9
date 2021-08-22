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
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.skillengine.model.Effect;

/**
 * @author antness
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "XPBoostEffect")
public class XPBoostEffect extends BufEffect {

    @Override
    public void calculate(Effect effect) {
        effect.setXpBoost(true);
        effect.addSucessEffect(this);
    }

}
