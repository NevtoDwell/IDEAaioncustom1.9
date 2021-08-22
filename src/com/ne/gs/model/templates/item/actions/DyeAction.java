/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.PersistentState;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.network.aion.serverpackets.SM_UPDATE_PLAYER_APPEARANCE;
import com.ne.gs.services.item.ItemPacketService;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author IceReaper
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DyeAction")
public class DyeAction extends AbstractItemAction {

    @XmlAttribute(name = "color")
    protected String color;

    @Override
    public boolean canAct(Player player, Item parentItem, Item targetItem) {
        if (targetItem == null) { // no item selected.
            player.sendPck(SM_SYSTEM_MESSAGE.STR_ITEM_COLOR_ERROR);
            return false;
        }
        return true;
    }

    @Override
    public void act(Player player, Item parentItem, Item targetItem) {
        if (targetItem.getItemTemplate().isItemDyePermitted()) {
            if (player.getInventory().getItemCountByItemId(parentItem.getItemId()) < 1) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_ITEM_COLOR_ERROR);
                return;
            }
            // Painting
            if (color.equals("no")) {
                targetItem.setItemColor(0);
                player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1);
                player.sendPck(SM_SYSTEM_MESSAGE.STR_ITEM_COLOR_REMOVE_SUCCEED(targetItem.getNameID()));
            } else {
                int rgb = Integer.parseInt(color, 16);
                int bgra = 0xFF | ((rgb & 0xFF) << 24) | ((rgb & 0xFF00) << 8) | ((rgb & 0xFF0000) >>> 8);
                if (bgra == targetItem.getItemColor()) {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_ITEM_COLOR_CHANGE_ERROR_CANNOTDYE(targetItem.getNameID()));
                    return;
                } else {
                    player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1);
                    targetItem.setItemColor(bgra);
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_ITEM_COLOR_CHANGE_SUCCEED(targetItem.getNameID(), parentItem.getNameID()));
                }
            }

            // item is equipped, so need broadcast packet
            if (player.getEquipment().getEquippedItemByObjId(targetItem.getObjectId()) != null) {
                PacketSendUtility.broadcastPacket(player, new SM_UPDATE_PLAYER_APPEARANCE(player.getObjectId(), player.getEquipment().getEquippedForAppearance()), true);
                player.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);
            }
            // item is not equipped
            else {
                player.getInventory().setPersistentState(PersistentState.UPDATE_REQUIRED);
            }
            ItemPacketService.updateItemAfterInfoChange(player, targetItem);
        }
    }
}
