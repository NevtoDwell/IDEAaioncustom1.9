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

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_FIND_GROUP;
import com.ne.gs.services.FindGroupService;

/**
 * @author cura, MrPoke
 */
public class CM_FIND_GROUP extends AionClientPacket {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(CM_FIND_GROUP.class);

    private int action;
    private int playerObjId;
    private String message;
    private int groupType;
    @SuppressWarnings("unused")
    private int classId;
    @SuppressWarnings("unused")
    private int level;
    private int unk;

    @Override
    protected void readImpl() {
        action = readC();

        switch (action) {
            case 0x00: // recruit list
                break;
            case 0x01: // offer delete
                playerObjId = readD();
                unk = readD(); // unk(65557)
                break;
            case 0x02: // send offer
                playerObjId = readD();
                message = readS();
                groupType = readC();
                break;
            case 0x03: // recruit update
                playerObjId = readD();
                unk = readD(); // unk(65557)
                message = readS();
                groupType = readC();
                break;
            case 0x04: // apply list
                break;
            case 0x05: // post delete
                playerObjId = readD();
                break;
            case 0x06: // apply create
                playerObjId = readD();
                message = readS();
                groupType = readC();
                classId = readC();
                level = readC();
                break;
            case 0x07: // apply update
                // TODO need packet check
                break;
            default:
                log.error("Unknown find group packet? 0x" + Integer.toHexString(action).toUpperCase());
                break;
        }
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        switch (action) {
            case 0x00:
            case 0x04:
                FindGroupService.getInstance().sendFindGroups(player, action);
                break;
            case 0x01:
            case 0x05:
                FindGroupService.getInstance().removeFindGroup(player.getRace(), action - 1, playerObjId);
                break;
            case 0x02:
            case 0x06:
                FindGroupService.getInstance().addFindGroupList(player, action, message, groupType);
                break;
            case 0x03:
                FindGroupService.getInstance().updateFindGroupList(player, message, playerObjId);
            default:
                player.sendPck(new SM_FIND_GROUP(action, playerObjId, unk));
                break;
        }
    }
}
