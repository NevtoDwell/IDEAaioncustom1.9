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
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.ne.gs.services.CubeExpandService;
import com.ne.gs.services.WarehouseService;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExpandInventoryAction")
public class ExpandInventoryAction extends AbstractItemAction {

    @XmlAttribute(name = "level")
    private int level;
    @XmlAttribute(name = "storage")
    private StorageType storage;

    @Override
    public boolean canAct(Player player, Item parentItem, Item targetItem) {
        switch (storage) {
            case CUBE:
                return CubeExpandService.canExpand(player);
            case WAREHOUSE:
                return WarehouseService.canExpand(player);
        }
        return false;
    }

    @Override
    public void act(Player player, Item parentItem, Item targetItem) {
        if (!player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1)) {
            return;
        }
        ItemTemplate itemTemplate = parentItem.getItemTemplate();
        PacketSendUtility.broadcastPacket(player,
            new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), itemTemplate.getTemplateId()), true);

        switch (storage) {
            case CUBE:
                CubeExpandService.expand(player, false);
                break;
            case WAREHOUSE:
                WarehouseService.expand(player);
                break;
        }
    }

}
