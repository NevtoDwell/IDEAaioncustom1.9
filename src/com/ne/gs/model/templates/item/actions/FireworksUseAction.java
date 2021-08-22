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
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author Tiger
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FireworksUseAction")
public class FireworksUseAction extends AbstractItemAction {

    @Override
    public boolean canAct(Player player, Item parentItem, Item targetItem) {
        return true;
    }

    @Override
    public void act(Player player, Item parentItem, Item targetItem) {
        if (parentItem.getActivationCount() > 1) {
            parentItem.setActivationCount(parentItem.getActivationCount() - 1);
        } else {
            player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1);
        }

        PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(),
                parentItem.getItemTemplate().getTemplateId(), 0, 1, 0));
    }
}
