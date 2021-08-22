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

import com.ne.gs.model.DescId;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.motion.Motion;
import com.ne.gs.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.ne.gs.network.aion.serverpackets.SM_MOTION;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AnimationAddAction")
public class AnimationAddAction
    extends AbstractItemAction {

    @XmlAttribute
    protected Integer idle;
    @XmlAttribute
    protected Integer run;
    @XmlAttribute
    protected Integer jump;
    @XmlAttribute
    protected Integer rest;
    @XmlAttribute
    protected Integer minutes;

    @Override
    public boolean canAct(Player player, Item parentItem, Item targetItem) {
        if (parentItem == null) { // no item selected.
            player.sendPck(SM_SYSTEM_MESSAGE.STR_ITEM_COLOR_ERROR);
            return false;
        }

        return true;
    }

    @Override
    public void act(final Player player, final Item parentItem, Item targetItem) {
        player.getController().cancelUseItem();
        player.sendPck(new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(),
            parentItem.getItemTemplate().getTemplateId(), 1000, 0, 0));
        player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if (player.getInventory().decreaseItemCount(parentItem, 1) != 0) {
                    return;
                }
                if (idle != null) {
                    addMotion(player, idle);
                }
                if (run != null) {
                    addMotion(player, run);
                }
                if (jump != null) {
                    addMotion(player, jump);
                }
                if (rest != null) {
                    addMotion(player, rest);
                }
                PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(),
                    parentItem.getObjectId(), parentItem.getItemId(), 0, 1, 0));
                player.sendPck(new SM_SYSTEM_MESSAGE(1300423, DescId.of(parentItem.getItemTemplate().getNameId())));
                PacketSendUtility.broadcastPacket(player, new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()), false);
            }
        }, 1000));
    }

    private void addMotion(Player player, int motionId) {
        Motion motion = new Motion(motionId, minutes == null ? 0 : (int) (System.currentTimeMillis() / 1000) + minutes * 60, true);
        player.getMotions().add(motion, true);
        player.sendPck(new SM_MOTION((short) motion.getId(), motion.getRemainingTime()));
    }
}
