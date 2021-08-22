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

import com.ne.gs.configs.administration.AdminConfig;
import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.configs.main.LoggingConfig;
import com.ne.gs.model.ChatType;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_MESSAGE;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.restrictions.RestrictionsManager;
import com.ne.gs.services.NameRestrictionService;
import com.ne.gs.utils.ChatUtil;
import com.ne.gs.world.World;

/**
 * Packet that reads Whisper chat messages.<br>
 *
 * @author SoulKeeper
 */
public class CM_CHAT_MESSAGE_WHISPER extends AionClientPacket {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger("CHAT_LOG");

    /**
     * To whom this message is sent
     */
    private String name;

    /**
     * Message text
     */
    private String message;

    /**
     * Read message
     */
    @Override
    protected void readImpl() {
        name = readS();
        message = readS();
    }

    /**
     * Print debug info
     */
    @Override
    protected void runImpl() {
        name = name.replace("\uE024", "");
        name = name.replace("\uE023", "");
        if (name.contains(ChatUtil.HEART)) {
            name = name.split(ChatUtil.HEART)[0].trim();
        }

        name = ChatUtil.getRealAdminName(name);

        String formatname = name;

        Player sender = getConnection().getActivePlayer();
        Player receiver = World.getInstance().findPlayer(formatname);

        if (LoggingConfig.LOG_CHAT) {
            log.info(String.format("[MESSAGE] [%s] Whisper To: %s, Message: %s", sender.getName(), formatname, message));
        }

        if (receiver == null) {
            sendPacket(SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(formatname));
        } else if (!receiver.isWispable()) {
            sender.sendMsg("Вы не можете разговаривать с ГМ-ом. | You can't talk with this gm.");
        } else if (sender.getLevel() < CustomConfig.LEVEL_TO_WHISPER) {
            sendPacket(SM_SYSTEM_MESSAGE.STR_CANT_WHISPER_LEVEL(String.valueOf(CustomConfig.LEVEL_TO_WHISPER)));
        } else if (receiver.getBlockList().contains(sender.getObjectId())) {
            sendPacket(SM_SYSTEM_MESSAGE.STR_YOU_EXCLUDED(receiver.getName()));
        } else if ((!CustomConfig.SPEAKING_BETWEEN_FACTIONS) && (sender.getRace().getRaceId() != receiver.getRace().getRaceId())
            && (sender.getAccessLevel() < AdminConfig.GM_LEVEL) && (receiver.getAccessLevel() < AdminConfig.GM_LEVEL)) {
            sendPacket(SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(formatname));
        } else if (RestrictionsManager.canChat(sender)) {
            receiver.sendPck(new SM_MESSAGE(sender, NameRestrictionService.filterMessage(message), ChatType.WHISPER));
        }
    }
}
