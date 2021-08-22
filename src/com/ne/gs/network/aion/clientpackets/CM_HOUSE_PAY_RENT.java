/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.configs.main.HousingConfig;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

public class CM_HOUSE_PAY_RENT extends AionClientPacket {

    int weekCount;

    @Override
    protected void readImpl() {
        weekCount = readC();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        if (!HousingConfig.RENT_STATUS) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_F2P_CASH_HOUSE_FEE_FREE);
            return;
        }

        // final House house = player.getActiveHouse();
        // if (!house.isFeePaid()) {
        // final long toPay = house.getLand().getMaintenanceFee() * weekCount;
        // if (toPay <= 0L) {
        // return;
        // }
        // if (player.getInventory().getKinah() < toPay) {
        // PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_NOT_ENOUGH_MONEY);
        // return;
        // }
        // player.getInventory().decreaseKinah(toPay);
        //
        // long payTime = MaintenanceTask.getInstance().getRunTime() * 1000L + System.currentTimeMillis();
        // while (--weekCount > 0) {
        // payTime += MaintenanceTask.getInstance().getPeriod();
        // }
        // house.setNextPay(new Timestamp(payTime));
        // house.setFeePaid(true);
        // house.save();
        // }
    }
}
