/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.skillengine.model.TransformType;

/**
 * @author Sweetkr, xTz
 */
public class SM_TRANSFORM extends AionServerPacket {

    private final Creature creature;
    private final int state;
    private final int modelId;
    private final boolean applyEffect;
    private int panelId;

    public SM_TRANSFORM(Creature creature, boolean applyEffect) {
        this.creature = creature;
        state = creature.getState();
        modelId = creature.getTransformModel().getModelId();
        this.applyEffect = applyEffect;
    }

    public SM_TRANSFORM(Creature creature, int panelId, boolean applyEffect) {
        this.creature = creature;
        state = creature.getState();
        modelId = creature.getTransformModel().getModelId();
        this.panelId = panelId;
        this.applyEffect = applyEffect;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(creature.getObjectId());
        writeD(modelId);
        writeH(state);
        writeF(0.25f);
        writeF(2.0f);
        writeC(applyEffect && creature.getTransformModel().getType() == TransformType.NONE ? 1 : 0);
        writeD(creature.getTransformModel().getType().getId());
        writeC(0);
        writeC(0);
        writeC(0);
        writeD(panelId); // display panel
    }
}
