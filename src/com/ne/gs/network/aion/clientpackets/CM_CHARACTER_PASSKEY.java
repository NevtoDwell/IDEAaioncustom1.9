/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.database.GDB;
import com.ne.gs.configs.main.SecurityConfig;
import com.ne.gs.database.dao.PlayerPasskeyDAO;
import com.ne.gs.model.account.CharacterPasskey;
import com.ne.gs.model.account.CharacterPasskey.ConnectType;
import com.ne.gs.model.account.PlayerAccountData;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.serverpackets.SM_CHARACTER_SELECT;
import com.ne.gs.network.aion.serverpackets.SM_DELETE_CHARACTER;
import com.ne.gs.network.loginserver.LoginServer;
import com.ne.gs.services.player.PlayerEnterWorldService;
import com.ne.gs.services.player.PlayerService;

/**
 * @author ginho1
 */
public class CM_CHARACTER_PASSKEY extends AionClientPacket {

    private int type;
    private String passkey;
    private String newPasskey;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        type = readH(); // 0:new, 2:update, 3:input
        try {
            passkey = new String(readB(32), "UTF-16le");
            if (type == 2) {
                newPasskey = new String(readB(32), "UTF-16le");
            }
        } catch (Exception e) {
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        AionConnection client = getConnection();
        CharacterPasskey chaPasskey = client.getAccount().getCharacterPasskey();

        switch (type) {
            case 0:
                chaPasskey.setIsPass(false);
                chaPasskey.setWrongCount(0);
                GDB.get(PlayerPasskeyDAO.class).insertPlayerPasskey(client.getAccount().getId(), passkey);
                client.sendPacket(new SM_CHARACTER_SELECT(2, type, chaPasskey.getWrongCount()));
                break;
            case 2:
                boolean isSuccess = GDB.get(PlayerPasskeyDAO.class).updatePlayerPasskey(client.getAccount().getId(), passkey, newPasskey);

                chaPasskey.setIsPass(false);
                if (isSuccess) {
                    chaPasskey.setWrongCount(0);
                    client.sendPacket(new SM_CHARACTER_SELECT(2, type, chaPasskey.getWrongCount()));
                } else {
                    chaPasskey.setWrongCount(chaPasskey.getWrongCount() + 1);
                    checkBlock(client.getAccount().getId(), chaPasskey.getWrongCount());
                    client.sendPacket(new SM_CHARACTER_SELECT(2, type, chaPasskey.getWrongCount()));
                }
                break;
            case 3:
                boolean isPass = GDB.get(PlayerPasskeyDAO.class).checkPlayerPasskey(client.getAccount().getId(), passkey);

                if (isPass) {
                    chaPasskey.setIsPass(true);
                    chaPasskey.setWrongCount(0);
                    client.sendPacket(new SM_CHARACTER_SELECT(2, type, chaPasskey.getWrongCount()));

                    if (chaPasskey.getConnectType() == ConnectType.ENTER) {
                        PlayerEnterWorldService.startEnterWorld(chaPasskey.getObjectId(), client);
                    } else if (chaPasskey.getConnectType() == ConnectType.DELETE) {
                        PlayerAccountData playerAccData = client.getAccount().getPlayerAccountData(chaPasskey.getObjectId());

                        PlayerService.deletePlayer(playerAccData);
                        client.sendPacket(new SM_DELETE_CHARACTER(chaPasskey.getObjectId(), playerAccData.getDeletionTimeInSeconds()));
                    }
                } else {
                    chaPasskey.setIsPass(false);
                    chaPasskey.setWrongCount(chaPasskey.getWrongCount() + 1);
                    checkBlock(client.getAccount().getId(), chaPasskey.getWrongCount());
                    client.sendPacket(new SM_CHARACTER_SELECT(2, type, chaPasskey.getWrongCount()));
                }
                break;
        }
    }

    /**
     * @param accountId
     * @param wrongCount
     */
    private void checkBlock(int accountId, int wrongCount) {
        if (wrongCount >= SecurityConfig.PASSKEY_WRONG_MAXCOUNT) {
            // TODO : Change the account to be blocked
            LoginServer.getInstance().sendBanPacket((byte) 2, accountId, "", 60 * 8, 0);
        }
    }
}
