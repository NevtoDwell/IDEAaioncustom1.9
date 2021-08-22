/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;

/**
 * @author ATracer
 */
public class CM_SUMMON_ATTACK extends AionClientPacket {

    private static final Logger log = LoggerFactory.getLogger(CM_SUMMON_ATTACK.class);

    private int summonObjId;
    private int targetObjId;
    @SuppressWarnings("unused")
    private int unk1;

    private int time;
    @SuppressWarnings("unused")
    private int unk3;

    @Override
    protected void readImpl() {
        summonObjId = readD();
        targetObjId = readD();
        unk1 = readC();
        time = readH();
        unk3 = readC();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        Summon summon = player.getSummon();
        if (summon == null) {
            log.warn("summon attack without active summon on " + player.getName() + ".");
            return;
        }

        if (summon.getObjectId() != summonObjId) {
            log.warn("summon attack from a different summon instance on " + player.getName() + ".");
            return;
        }

        VisibleObject obj = summon.getKnownList().getObject(targetObjId);
        if (obj != null && obj instanceof Creature) {
            summon.getController().attackTarget((Creature) obj, time);
        } else {
            log.warn("summon attack on a wrong target on " + player.getName());
        }
    }
}
