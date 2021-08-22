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

import com.ne.commons.network.util.ThreadPoolManager;
import com.ne.gs.controllers.ItemUseObserver;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.DescId;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.AssemblyItem;
import com.ne.gs.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.utils.PacketSendUtility;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AssemblyItemAction")
public class AssemblyItemAction extends AbstractItemAction {

    @XmlAttribute
    private int item;

    @Override
    public boolean canAct(Player player, Item parentItem, Item targetItem) {
        AssemblyItem assemblyItem = getAssemblyItem();
        if (assemblyItem == null) {
            return false;
        }
        for (Integer itemId : assemblyItem.getParts()) {
            if (player.getInventory().getFirstItemByItemId(itemId) == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void act(final Player player, final Item parentItem, Item targetItem) {
        PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 1000, 0,
            0), true);

        final ItemUseObserver observer = new ItemUseObserver() {

            @Override
            public void abort() {
                player.getController().cancelTask(TaskId.ITEM_USE);
                player.removeItemCoolDown(parentItem.getItemTemplate().getUseLimits().getDelayId());
                player.sendPck(SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED(DescId.of(parentItem.getItemTemplate().getNameId())));
                PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem
                    .getItemTemplate().getTemplateId(), 0, 2, 0), true);

                player.getObserveController().removeObserver(this);
            }
        };
        player.getObserveController().attach(observer);
        player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                player.getObserveController().removeObserver(observer);
                player.getController().cancelTask(TaskId.ITEM_USE);
                AssemblyItem assemblyItem = getAssemblyItem();
                for (Integer itemId : assemblyItem.getParts()) {
                    if (!player.getInventory().decreaseByItemId(itemId, 1)) {
                        return;
                    }
                }
                PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem
                    .getItemTemplate().getTemplateId(), 0, 1, 0), true);

                player.sendPck(new SM_SYSTEM_MESSAGE(1401122));
                ItemService.addItem(player, assemblyItem.getId(), 1);
            }
        }, 1000));
    }

    public AssemblyItem getAssemblyItem() {
        return DataManager.ASSEMBLY_ITEM_DATA.getAssemblyItem(item);
    }
}
