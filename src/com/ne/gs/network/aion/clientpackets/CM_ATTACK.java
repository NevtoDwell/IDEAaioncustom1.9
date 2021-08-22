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
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;

/**
 * @author alexa026, Avol, ATracer, KID
 */
public class CM_ATTACK extends AionClientPacket {

    private static final Logger log = LoggerFactory.getLogger(CM_ATTACK.class);
    /**
     * Target object id that client wants to TALK WITH or 0 if wants to unselect
     */
    private int targetObjectId;
    // TODO: Question, are they really needed?
    @SuppressWarnings("unused")
    private int attackno;

    private int time;
    @SuppressWarnings("unused")
    private int type;

    @Override
    protected void readImpl() {
        targetObjectId = readD();// empty
        attackno = readC();// empty
        time = readH();// empty
        type = readC();// empty
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        if (player.getLifeStats().isAlreadyDead()) {
            return;
        }

        if (player.isProtectionActive()) {
            player.getController().stopProtectionActiveTask();
        }

        VisibleObject obj = player.getKnownList().getObject(targetObjectId);
        if (obj != null && obj instanceof Creature) {
            player.getController().attackTarget((Creature) obj, time);
        } else if (obj != null) {
            log.warn("Attacking unsupported target" + obj + " id " + obj.getObjectTemplate().getTemplateId());
        }
    }
}
