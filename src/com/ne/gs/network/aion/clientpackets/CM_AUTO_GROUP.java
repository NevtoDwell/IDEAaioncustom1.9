/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.autogroup.EntryRequestType;
import com.ne.gs.model.gameobjects.player.Player;
//import com.ne.gs.modules.instanceentry.InstanceEntryManager;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_AUTO_GROUP;
import com.ne.gs.services.AutoGroupService2;

/**
 * @author Shepper, Guapo, nrg
 */
public class CM_AUTO_GROUP extends AionClientPacket {

    private byte instanceMaskId;
    private byte windowId;
    private byte entryRequestId;

    @Override
    protected void readImpl() {
        instanceMaskId = (byte) readD();
        windowId = (byte) readC();
        entryRequestId = (byte) readC();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        if (player.isGM()) {
            player.sendMsg("windowId: " + windowId);
        }
        switch (windowId) {
            case 100:
                EntryRequestType ert = EntryRequestType.getTypeById(entryRequestId);
                if (ert == null) {
                    return;
                }
                // FIXME temp. until all instance are reworked according to InstanceEntryRegistry
                switch (instanceMaskId) {
                    case 1:
                    case 2:
                    case 3:
                      //  InstanceEntryManager.RequestRegister2 r = new InstanceEntryManager.RequestRegister2(player, instanceMaskId, entryRequestId);
                       // InstanceEntryManager.tell(r);
                        return;
                }

                AutoGroupService2.getInstance().startLooking(player, instanceMaskId, ert);
                break;
            case 101:
                // FIXME temp. until all instance are reworked according to InstanceEntryRegistry
                switch (instanceMaskId) {
                    case 1:
                    case 2:
                    case 3:
                       // InstanceEntryManager.CancelEntry r = new InstanceEntryManager.CancelEntry(player, instanceMaskId);
                      //  InstanceEntryManager.tell(r);
                        return;
                }
                AutoGroupService2.getInstance().unregisterLooking(player, instanceMaskId);
                break;
            case 102:
                // FIXME temp. until all instance are reworked according to InstanceEntryRegistry
                switch (instanceMaskId) {
                    case 1:
                    case 2:
                    case 3:
                      //  InstanceEntryManager.RequestEnter2 r = new InstanceEntryManager.RequestEnter2(player, instanceMaskId, entryRequestId);
                      //  InstanceEntryManager.tell(r);
                        return;
                }

                AutoGroupService2.getInstance().enterToInstance(player, instanceMaskId);
                break;
            case 103:
                // FIXME temp. until all instance are reworked according to InstanceEntryRegistry
                switch (instanceMaskId) {
                    case 1:
                    case 2:
                    case 3:
                     //   InstanceEntryManager.CancelEntry r = new InstanceEntryManager.CancelEntry(player, instanceMaskId);
                     //   InstanceEntryManager.tell(r);
                        return;
                }
                AutoGroupService2.getInstance().cancelEnter(player, instanceMaskId);
                break;
            case 104:
                //DredgionService2.getInstance().showWindow(player, instanceMaskId);
                player.sendPck(new SM_AUTO_GROUP(instanceMaskId));
                break;
            case 105:
                // DredgionRegService.getInstance().failedEnterDredgion(player);
                break;
        }
    }
}
