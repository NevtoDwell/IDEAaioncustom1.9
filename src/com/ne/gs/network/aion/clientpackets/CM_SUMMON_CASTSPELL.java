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
 * @author ATracer, KID
 */
public class CM_SUMMON_CASTSPELL extends AionClientPacket {

    private static final Logger log = LoggerFactory.getLogger(CM_SUMMON_CASTSPELL.class);
    private int summonObjId;
    private int targetObjId;
    private int skillId;
    @SuppressWarnings("unused")
    private int skillLvl;
    @SuppressWarnings("unused")
    private float unk;

    @Override
    protected void readImpl() {
        summonObjId = readD();
        skillId = readH();
        skillLvl = readC();
        targetObjId = readD();
        unk = readF();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        long currentTime = System.currentTimeMillis();
        if (player.getNextSummonSkillUse() > currentTime) {
            return;
        }

        Summon summon = player.getSummon();
        if (summon == null) {
            log.warn("summon castspell without active summon on " + player.getName() + ".");
            return;
        }
        if (summon.getObjectId() != summonObjId) {
            log.warn("summon castspell from a different summon instance on " + player.getName() + ".");
            return;
        }

        Creature target = null;
        if (targetObjId == summon.getObjectId()) {
            target = summon;
        } else {
            VisibleObject obj = summon.getKnownList().getObject(targetObjId);
            if (obj instanceof Creature) {
                target = (Creature) obj;
            }
        }

        if (target != null) {
            player.setNextSummonSkillUse(currentTime + 1100);
            summon.getController().useSkill(skillId, target);
        } else {
            log.warn("summon castspell on a wrong target on " + player.getName());
        }
    }
}
