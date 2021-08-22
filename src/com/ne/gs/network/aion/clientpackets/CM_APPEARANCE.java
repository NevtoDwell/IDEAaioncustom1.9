/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.actions.AbstractItemAction;
import com.ne.gs.model.templates.item.actions.CosmeticItemAction;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.RenameService;
import com.ne.gs.utils.Util;

/**
 * @author xTz
 */
public class CM_APPEARANCE extends AionClientPacket {

    private int type;

    private int itemObjId;

    private String name;

    @Override
    protected void readImpl() {
        type = readC();
        readC();
        readH();
        itemObjId = readD();
        switch (type) {
            case 0:
            case 1:
                name = readS();
                break;
        }

    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        switch (type) {
            case 0: // Change Char Name,
                name = Util.convertName(name);
                if (RenameService.renamePlayer(player, player.getName(), name, itemObjId)) {
                    player.sendPck(new SM_SYSTEM_MESSAGE(1400157, name));
                }
                break;
            case 1: // Change Legion Name
                if (RenameService.renameLegion(player, name, itemObjId)) {
                    player.sendPck(new SM_SYSTEM_MESSAGE(1400158, name));
                }
                break;
            case 2: // cosmetic items
                Item item = player.getInventory().getItemByObjId(itemObjId);
                if (item != null) {
                    for (AbstractItemAction action : item.getItemTemplate().getActions().getItemActions()) {
                        if (action instanceof CosmeticItemAction) {
                            if (!action.canAct(player, null, null)) {
                                return;
                            }
                            action.act(player, null, item);
                            break;
                        }
                    }
                }
                break;
        }
    }
}
