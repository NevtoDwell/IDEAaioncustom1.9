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

public class SanctuaryEffect extends EffectTemplate {

    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
    }

    public void startEffect(Effect effect) {
    }

    public void endEffect(Effect effect) {
    }
}
