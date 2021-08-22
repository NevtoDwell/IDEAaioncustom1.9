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
import com.ne.gs.network.loginserver.LoginServer;
import com.ne.gs.network.loginserver.LsClientPacket;
import com.ne.gs.utils.rates.Rates;
import com.ne.gs.world.World;

/**
 * @author Aionchs-Wylovech
 */
public class CM_LS_CONTROL_RESPONSE extends LsClientPacket {

    public CM_LS_CONTROL_RESPONSE(int opCode) {
        super(opCode);
    }

    private int type;
    private boolean result;
    private String playerName;
    private byte param;
    private String adminName;
    private int accountId;

    @Override
    public void readImpl() {
        type = readC();
        result = readC() == 1;
        adminName = readS();
        playerName = readS();
        param = (byte) readC();
        accountId = readD();
    }

    @Override
    public void runImpl() {
        World world = World.getInstance();
        Player admin = world.findPlayer(adminName);
        Player player = world.findPlayer(playerName);
        LoginServer.getInstance().accountUpdate(accountId, param, type);
        switch (type) {
            case 1:
                if (result) {
                    if (admin != null) {
                        admin.sendMsg(playerName + " has been promoted Administrator with role " + param);
                    }
                    if (player != null) {
                        player.sendMsg("You have been promoted Administrator with role " + param + " by " + adminName);
                    }
                } else if (admin != null) {
                    admin.sendMsg(" Abnormal, the operation failed! ");
                }
                break;
            case 2:
                if (result) {
                    if (admin != null) {
                        admin.sendMsg(playerName + " has been promoted membership with level " + param);
                    }
                    if (player != null) {
                        player.setRates(Rates.getRatesFor(param));
                        player.sendMsg("Действие Вашего Премиум/VIP аккаунта окончено. Теперь у Вас стандартные рейты.");
                    }
                } else if (admin != null) {
                    admin.sendMsg(" Abnormal, the operation failed! ");
                }
                break;
        }
    }
}
