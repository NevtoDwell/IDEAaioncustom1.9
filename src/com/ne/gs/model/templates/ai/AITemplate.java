/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.ai;

import com.ne.gs.model.ai.Ai;
import com.ne.gs.model.ai.Bombs;
import com.ne.gs.model.ai.Summons;

/**
 * @author xTz
 */
public class AITemplate {

    private int npcId;
    private Summons summons;
    private Bombs bombs;

    public AITemplate() {
    }

    public AITemplate(Ai template) {
        this.summons = template.getSummons();
        this.bombs = template.getBombs();
        this.npcId = template.getNpcId();
    }

    public int getNpcId() {
        return npcId;
    }

    public Summons getSummons() {
        return summons;
    }

    public Bombs getBombs() {
        return bombs;
    }
}
