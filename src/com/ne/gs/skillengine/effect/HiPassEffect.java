/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.effect;

import com.ne.gs.skillengine.model.Effect;

public class HiPassEffect extends BufEffect {

    public void calculate(Effect effect) {
        effect.addSucessEffect(this);
    }

    public void startEffect(Effect effect) {
        effect.setHiPass(true);
    }
}
