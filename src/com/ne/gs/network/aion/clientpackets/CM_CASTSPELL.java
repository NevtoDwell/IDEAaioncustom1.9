/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.skillengine.model.SkillTemplate;

/**
 * @author alexa026
 * @author rhys2002
 */
public class CM_CASTSPELL extends AionClientPacket {

    private int spellid;
    // 0 - obj id, 1 - point location, 2 - unk, 3 - object not in sight(skill 1606)? 4 - unk
    private int targetType;
    private float x, y, z;

    @SuppressWarnings("unused")
    private int targetObjectId;
    private int hitTime;
    private int level;

    @Override
    protected void readImpl() {
        spellid = readH();
        level = readC();

        targetType = readC();

        switch (targetType) {
            case 0:
            case 3:
            case 4:
                targetObjectId = readD();
                break;
            case 1:
                x = readF();
                y = readF();
                z = readF();
                break;
            case 2:
                x = readF();
                y = readF();
                z = readF();
                readF();// unk1
                readF();// unk2
                readF();// unk3
                readF();// unk4
                readF();// unk5
                readF();// unk6
                readF();// unk7
                readF();// unk8
                break;
        }

        hitTime = readH();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(spellid);
        if (template == null || template.isPassive()) {
            return;
        }

        if (player.isProtectionActive()) {
            player.getController().stopProtectionActiveTask();
        }

        long currentTime = System.currentTimeMillis();
        if (player.getNextSkillUse() > currentTime) {
            player.sendPck(new SM_SYSTEM_MESSAGE(1300021));
            return;
        }

        if (!player.getLifeStats().isAlreadyDead()) {
            player.getController().useSkill(template, targetType, x, y, z, hitTime, level);
        }
    }
}
