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

import com.ne.gs.controllers.ItemUseObserver;
import com.ne.gs.model.DescId;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_INSTANCE_INFO;
import com.ne.gs.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author Tiger
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InstanceTimeClear")
public class InstanceTimeClear extends AbstractItemAction {

    @XmlAttribute
    protected int mapid;

    @Override
    public boolean canAct(Player player, Item parentItem, Item targetItem) {
        if (player.getPortalCooldownList().getPortalCooldown(mapid) == 0L) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_CANT_INSTANCE_COOL_TIME_INIT);
            return false;
        }
        return true;
    }

    @Override
    public void act(final Player player, final Item parentItem, Item targetItem) {
        PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 1000, 0, 0));

        final ItemUseObserver observer = new ItemUseObserver() {

            @Override
            public void abort() {
                player.getController().cancelTask(TaskId.ITEM_USE);
                player.removeItemCoolDown(parentItem.getItemTemplate().getUseLimits().getDelayId());
                player.sendPck(SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED(DescId.of(parentItem.getItemTemplate().getNameId())));
                PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), 0, 2, 0), true);

                player.getObserveController().removeObserver(this);
            }
        };
        player.getObserveController().attach(observer);
        player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                player.getObserveController().removeObserver(observer);
                if (parentItem.getActivationCount() > 1) {
                    parentItem.setActivationCount(parentItem.getActivationCount() - 1);
                } else {
                    player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1);
                }

                player.getPortalCooldownList().removePortalCoolDown(mapid);

                if (player.isInTeam()) {
                    player.getCurrentTeam().sendPacket(new SM_INSTANCE_INFO(player, mapid));
                } else {
                    player.sendPck(new SM_INSTANCE_INFO(player, mapid));
                }
                PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, 1, 0));
            }
        }, 1000));
    }
}
