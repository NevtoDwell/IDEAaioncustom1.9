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
import java.util.Collection;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.services.item.ItemChargeService;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChargeItemAction")
public class ChargeAction extends AbstractItemAction {

    @XmlAttribute
    protected int capacity;

    @Override
    public boolean canAct(Player player, Item parentItem, Item targetItem) {
        Collection<Item> conditioningItems = ItemChargeService.filterItemsToCondition(player, null, parentItem.getImprovement().getChargeWay());
        return conditioningItems.size() > 0;
    }

    @Override
    public void act(Player player, Item parentItem, Item targetItem) {
        if (!player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1)) {
            return;
        }
        Collection<Item> conditioningItems = ItemChargeService.filterItemsToCondition(player, null, parentItem.getImprovement().getChargeWay());
        ItemChargeService.chargeItems(player, conditioningItems, capacity);
    }

}
