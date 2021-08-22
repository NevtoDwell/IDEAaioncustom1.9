/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.storage.StorageType;
import com.ne.gs.model.team.legion.Legion;
import com.ne.gs.model.team.legion.LegionHistoryType;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.services.LegionService;
import com.ne.gs.services.item.ItemRestrictionService;

/**
 * @author ATracer
 */
public class CM_LEGION_WH_KINAH extends AionClientPacket {

    private long amount;
    private int operation;

    @Override
    protected void readImpl() {
        amount = readQ();
        operation = readC();
    }

    @Override
    protected void runImpl() {
        Player activePlayer = getConnection().getActivePlayer();

        Legion legion = activePlayer.getLegion();
        if (legion != null) {
            switch (operation) {
                case 0:
                    if (ItemRestrictionService.isItemRestrictedFrom(activePlayer, (byte) StorageType.LEGION_WAREHOUSE.getId())) {
                        return;
                    }

                    if (activePlayer.getStorage(StorageType.LEGION_WAREHOUSE.getId()).tryDecreaseKinah(amount)) {
                        activePlayer.getInventory().increaseKinah(amount);
                        LegionService.getInstance().addHistory(legion, activePlayer.getName(), LegionHistoryType.KINAH_WITHDRAW, 2, Long.toString(amount));
                    }
                    break;

                case 1:
                    if (activePlayer.getInventory().tryDecreaseKinah(amount)) {
                        activePlayer.getStorage(StorageType.LEGION_WAREHOUSE.getId()).increaseKinah(amount);
                        LegionService.getInstance().addHistory(legion, activePlayer.getName(), LegionHistoryType.KINAH_DEPOSIT, 2, Long.toString(amount));
                    }
                    break;
            }
        }
    }

}
