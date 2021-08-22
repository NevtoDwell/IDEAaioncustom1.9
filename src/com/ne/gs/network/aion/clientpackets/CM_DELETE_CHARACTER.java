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
import com.ne.gs.model.account.CharacterPasskey.ConnectType;
import com.ne.gs.model.account.PlayerAccountData;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.serverpackets.SM_CHARACTER_SELECT;
import com.ne.gs.network.aion.serverpackets.SM_DELETE_CHARACTER;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.player.PlayerService;

/**
 * In this packets aion client is requesting deletion of character.
 *
 * @author -Nemesiss-
 */
public class CM_DELETE_CHARACTER extends AionClientPacket {

    /**
     * PlayOk2 - we dont care...
     */
    @SuppressWarnings("unused")
    private int playOk2;
    /**
     * ObjectId of character that should be deleted.
     */
    private int chaOid;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        playOk2 = readD();
        chaOid = readD();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        AionConnection client = getConnection();
        PlayerAccountData playerAccData = client.getAccount().getPlayerAccountData(chaOid);
        if (client.getAccount().sizeNotDelete() == 1 &&
                (client.getAccount().getAccountWarehouse().size() != 0 ||
                 client.getAccount().getAccountWarehouse().getKinah() != 0)) {
            client.sendPacket(SM_SYSTEM_MESSAGE.STR_LOGIN_ERROR_DEL_CHAR_FAIL);
            return;
        }
        if (playerAccData != null && !playerAccData.isLegionMember()) {
            // passkey check
            if (SecurityConfig.PASSKEY_ENABLE && !client.getAccount().getCharacterPasskey().isPass()) {
                client.getAccount().getCharacterPasskey().setConnectType(ConnectType.DELETE);
                client.getAccount().getCharacterPasskey().setObjectId(chaOid);
                boolean isExistPasskey = GDB.get(PlayerPasskeyDAO.class).existCheckPlayerPasskey(client.getAccount().getId());

                if (!isExistPasskey) {
                    client.sendPacket(new SM_CHARACTER_SELECT(0));
                } else {
                    client.sendPacket(new SM_CHARACTER_SELECT(1));
                }
            } else {
                PlayerService.deletePlayer(playerAccData);
                client.sendPacket(new SM_DELETE_CHARACTER(chaOid, playerAccData.getDeletionTimeInSeconds()));
            }
        } else {
            client.sendPacket(SM_SYSTEM_MESSAGE.STR_GUILD_DISPERSE_STAYMODE_CANCEL_1);
        }
    }
}
