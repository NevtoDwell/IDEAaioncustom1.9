/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.database.GDB;
import com.ne.gs.database.dao.MailDAO;
import com.ne.gs.model.account.Account;
import com.ne.gs.model.account.CharacterBanInfo;
import com.ne.gs.model.account.PlayerAccountData;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.PlayerInfo;
import com.ne.gs.services.BrokerService;
import com.ne.gs.services.player.PlayerService;

/**
 * In this packet Server is sending Character List to client.
 *
 * @author Nemesiss, AEJTester
 */
public class SM_CHARACTER_LIST extends PlayerInfo {

    /**
     * PlayOk2 - we dont care...
     */
    private final int playOk2;

    /**
     * Constructs new <tt>SM_CHARACTER_LIST </tt> packet
     */
    public SM_CHARACTER_LIST(int playOk2) {
        this.playOk2 = playOk2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(playOk2);

        Account account = con.getAccount();
        writeC(account.size()); // characters count

        for (PlayerAccountData playerData : account.getSortedAccountsList()) {
            PlayerCommonData pcd = playerData.getPlayerCommonData();
            CharacterBanInfo cbi = playerData.getCharBanInfo();
            Player player = PlayerService.getPlayer(pcd.getPlayerObjId(), account);

            writePlayerInfo(playerData);

            writeD(player.getPlayerSettings().getDisplay());// display helmet 0 show, 5 dont show
            writeD(0);
            writeD(0);
            writeD(GDB.get(MailDAO.class).haveUnread(pcd.getPlayerObjId()) ? 1 : 0); // mail
            writeD(0); // unk
            writeD(0); // unk
            writeQ(BrokerService.getInstance().getCollectedMoney(pcd)); // collected money from broker
            writeD(0);

            if (cbi != null && cbi.getEnd() > System.currentTimeMillis() / 1000) {
                // client wants int so let's hope we do not reach long limit with timestamp while this server is used :P
                writeD((int) cbi.getStart()); // startPunishDate
                writeD((int) cbi.getEnd()); // endPunishDate
                writeS(cbi.getReason());
            } else {
                writeD(0);
                writeD(0);
                writeH(0);
            }
        }
    }
}
