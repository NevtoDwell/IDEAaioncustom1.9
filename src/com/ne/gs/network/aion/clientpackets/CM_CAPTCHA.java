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

import com.ne.gs.configs.main.SecurityConfig;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.network.aion.serverpackets.SM_CAPTCHA;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.PunishmentService;

/**
 * @author Cura
 */
public class CM_CAPTCHA extends AionClientPacket {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(CM_CAPTCHA.class);

    private int type;
    private int count;
    private String word;

    @Override
    protected void readImpl() {
        type = readC();

        switch (type) {
            case 0x02:
                count = readC();
                word = readS();
                break;
            default:
                log.warn("Unknown CAPTCHA packet type? 0x" + Integer.toHexString(type).toUpperCase());
                break;
        }
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        switch (type) {
            case 0x02:
                if (player.getCaptchaWord().equalsIgnoreCase(word)) {
                    player.sendPck(new SM_SYSTEM_MESSAGE(1400270));
                    player.sendPck(new SM_CAPTCHA(true, 0));

                    PunishmentService.setIsNotGatherable(player, 0, false, 0);

                    // fp bonus (like retail)
                    player.getLifeStats().increaseFp(TYPE.FP, SecurityConfig.CAPTCHA_BONUS_FP_TIME);
                } else {
                    int banTime = SecurityConfig.CAPTCHA_EXTRACTION_BAN_TIME + SecurityConfig.CAPTCHA_EXTRACTION_BAN_ADD_TIME * count;

                    if (count < 3) {
                        player.sendPck(new SM_SYSTEM_MESSAGE(1400271, 3 - count));
                        player.sendPck(new SM_CAPTCHA(false, banTime));
                        PunishmentService.setIsNotGatherable(player, count, true, banTime * 1000L);
                    } else {
                        player.sendPck(new SM_SYSTEM_MESSAGE(1400272));
                        PunishmentService.setIsNotGatherable(player, count, true, banTime * 1000L);
                    }
                }
                break;
        }
    }
}
