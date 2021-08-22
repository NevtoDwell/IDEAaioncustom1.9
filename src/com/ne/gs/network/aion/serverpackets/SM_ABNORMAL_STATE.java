/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.Collection;

import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author Avol, ATracer
 */
public class SM_ABNORMAL_STATE extends AionServerPacket {

    private final Collection<Effect> effects;
    private final int abnormals;

    public SM_ABNORMAL_STATE(Collection<Effect> effects, int abnormals) {
        this.effects = effects;
        this.abnormals = abnormals;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(abnormals);
        writeD(0);
        writeH(effects.size());

        for (Effect effect : effects) {
            writeD(effect.getEffectorId());
            writeH(effect.getSkillId());
            writeC(effect.getSkillLevel());
            writeC(effect.getTargetSlot());
            writeD(effect.getRemainingTime());
        }
    }
}
