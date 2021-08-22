/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.loginserver.clientpackets;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.loginserver.LsClientPacket;
import com.ne.gs.world.World;

/**
 * @author Watson
 */
public class CM_BAN_RESPONSE extends LsClientPacket {

    public CM_BAN_RESPONSE(int opCode) {
        super(opCode);
    }

    private byte type;
    private int accountId;
    private String ip;
    private int time;
    private int adminObjId;
    private boolean result;

    @Override
    public void readImpl() {
        type = (byte) readC();
        accountId = readD();
        ip = readS();
        time = readD();
        adminObjId = readD();
        result = readC() == 1;
    }

    @Override
    public void runImpl() {
        Player admin = World.getInstance().findPlayer(adminObjId);

        if (admin == null) {
            return;
        }

        // Some messages stuff
        String message;
        if (type == 1 || type == 3) {
            if (result) {
                if (time < 0) {
                    message = "Account ID " + accountId + " was successfully unbanned";
                } else if (time == 0) {
                    message = "Account ID " + accountId + " was successfully banned";
                } else {
                    message = "Account ID " + accountId + " was successfully banned for " + time + " minutes";
                }
            } else {
                message = "Error occurred while banning player's account";
            }
            admin.sendMsg(message);
        }
        if (type == 2 || type == 3) {
            if (result) {
                if (time < 0) {
                    message = "IP mask " + ip + " was successfully removed from block list";
                } else if (time == 0) {
                    message = "IP mask " + ip + " was successfully added to block list";
                } else {
                    message = "IP mask " + ip + " was successfully added to block list for " + time + " minutes";
                }
            } else {
                message = "Error occurred while adding IP mask " + ip;
            }
            admin.sendMsg(message);
        }
    }
}
