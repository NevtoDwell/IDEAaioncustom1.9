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

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author ATracer
 */
public class SM_ABNORMAL_EFFECT extends AionServerPacket {

    private final int effectedId;
    private int effectType = 1;// 1: creature 2: effected is player
    private final int abnormals;
    private final Collection<Effect> filtered;

    public SM_ABNORMAL_EFFECT(Creature effected, int abnormals, Collection<Effect> effects) {
        this.abnormals = abnormals;
        effectedId = effected.getObjectId();
        filtered = effects;

        if (effected instanceof Player) {
            effectType = 2;
        }
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(effectedId);
        writeC(effectType); // unk
        writeD(0); // unk
        writeD(abnormals); // unk
        writeD(0); // unk
        writeH(filtered.size()); // effects size

        for (Effect effect : filtered) {
            switch (effectType) {
                case 2:
                    writeD(effect.getEffectorId());
                case 1:
                    writeH(effect.getSkillId());
                    writeC(effect.getSkillLevel());
                    writeC(effect.getTargetSlot());
                    writeD(effect.getRemainingTime());
                    break;
                default:
                    writeH(effect.getSkillId());
                    writeC(effect.getSkillLevel());
            }
        }
    }
}
