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
import java.util.concurrent.Future;

import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.controllers.ItemUseObserver;
import com.ne.gs.model.DescId;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.conds.SpawnObjCond;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.Kisk;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.KiskService;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.spawnengine.VisibleObjectSpawner;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author Sarynth, Source
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ToyPetSpawnAction")
public class ToyPetSpawnAction extends AbstractItemAction {

    @XmlAttribute
    protected int npcid;

    @XmlAttribute
    protected int time;

    public int getNpcId() {
        return npcid;
    }

    public int getTime() {
        return time;
    }

    @Override
    public boolean canAct(Player player, Item parentItem, Item targetItem) {
        return player.getConditioner().check(SpawnObjCond.class, new SpawnObjCond.Env(player, parentItem, targetItem));
    }

    @Override
    public void act(final Player player, final Item parentItem, Item targetItem) {
        // ShowAction     
        player.getController().cancelUseItem();
        PacketSendUtility.broadcastPacket(player,
            new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), CustomConfig.TOYPETSPAWN_ACTION_TIME, 0, 0), true);
        final ItemUseObserver observer = new ItemUseObserver() {
            @Override
            public void abort() {
                player.getController().cancelTask(TaskId.ITEM_USE);
                player.removeItemCoolDown(parentItem.getItemTemplate().getUseLimits().getDelayId());
                player.sendPck(SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED(DescId.of(parentItem.getItemTemplate().getNameId())));
                PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), 0, 2, 0), true);
            }
        };

        player.getObserveController().attach(observer);
        player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, 1, 1), true);
                player.getObserveController().removeObserver(observer);
                // RemoveKisk
                if (!player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1)) {
                    return;
                }
                player.sendPck(SM_SYSTEM_MESSAGE.STR_USE_ITEM(DescId.of(parentItem.getItemTemplate().getNameId())));
                float x = player.getX();
                float y = player.getY();
                float z = player.getZ();
                byte heading = (byte) ((player.getHeading() + 60) % 120);
                int worldId = player.getWorldId();
                int instanceId = player.getInstanceId();
                SpawnTemplate spawn = SpawnEngine.addNewSingleTimeSpawn(worldId, npcid, x, y, z, heading);

                final Kisk kisk = VisibleObjectSpawner.spawnKisk(spawn, instanceId, player);
                Integer objOwnerId = player.getObjectId();

                // Schedule Despawn Action
                Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        kisk.getController().onDelete();
                    }
                }, 7200000);
                // Fixed 2 hours 2 * 60 * 60 * 1000
                kisk.getController().addTask(TaskId.DESPAWN, task);

                // ShowFinalAction
                //TODO Bad idea...
                //player.getController().cancelUseItem();
                player.getController().cancelTask(TaskId.ITEM_USE);
                KiskService.getInstance().regKisk(kisk, objOwnerId);

                if (kisk.getMaxMembers() > 1) {
                    kisk.getController().onDialogRequest(player);
                } else {
                    KiskService.getInstance().onBind(kisk, player);
                }
            }
        }, CustomConfig.TOYPETSPAWN_ACTION_TIME));
    }
}
