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

import com.ne.commons.utils.Rnd;
import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.controllers.ItemUseObserver;
import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.ObserverType;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.DescId;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.actions.PlayerMode;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.model.templates.ride.RideInfo;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.skillengine.effect.AbnormalState;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.WorldMapType;
import com.ne.gs.world.WorldType;
import com.ne.gs.world.zone.ZoneInstance;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RideAction")
public class RideAction extends AbstractItemAction {

    @XmlAttribute(name = "npc_id")
    protected int npcId;

    @Override
    public boolean canAct(Player player, Item parentItem, Item targetItem) {
        if (parentItem == null) {
            return false;
        }

        if (CustomConfig.ENABLE_RIDE_RESTRICTION) {
            // TODO hex1r0: move to proper place
            switch (WorldMapType.of(player.getWorldId())) {
                case ARENA_OF_CHAOS:
                case ARENA_OF_DISCIPLINE:
                    return false;
            }

            for (ZoneInstance zone : player.getPosition().getMapRegion().getZones(player)) {
                if (!zone.canRide()) {
                    player.sendPck(new SM_SYSTEM_MESSAGE(1401099));
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void act(final Player player, final Item parentItem, Item targetItem) {
        player.getController().cancelUseItem();
        if (player.isInPlayerMode(PlayerMode.RIDE)) {
            player.unsetPlayerMode(PlayerMode.RIDE);
            return;
        }
        if(player.isInInstance() || player.getWorldId()== 600040000){
            player.sendMsg("Вы не можете использовать маунтов в этой зоне");
            return;
        }

        PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 3000, 0,
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
                player.unsetState(CreatureState.ACTIVE);
                player.getObserveController().removeObserver(observer);
                ItemTemplate itemTemplate = parentItem.getItemTemplate();
                player.setPlayerMode(PlayerMode.RIDE, getRideInfo());
                player.sendPck(SM_SYSTEM_MESSAGE.STR_USE_ITEM(DescId.of(itemTemplate.getNameId())));
                PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_EMOTE2, 0, 0), true);
                PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.RIDE, 0, getRideInfo().getNpcId()), true);

                PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(),
                    0, 1, 1), true);

                player.getController().cancelTask(TaskId.ITEM_USE);
            }
        }, 3000));

        ActionObserver rideObserver = new ActionObserver(ObserverType.ABNORMALSETTED) {

            @Override
            public void abnormalsetted(AbnormalState state) {
                if ((state.getId() & AbnormalState.DISMOUT_RIDE.getId()) > 0) {
                    player.unsetPlayerMode(PlayerMode.RIDE);
                }
            }
        };
        player.getObserveController().addObserver(rideObserver);
        player.setRideObservers(rideObserver);

        ActionObserver attackedObserver = new ActionObserver(ObserverType.ATTACKED) {

            @Override
            public void attacked(Creature creature) {
                if (Rnd.get(1000) < 200) {
                    player.unsetPlayerMode(PlayerMode.RIDE);
                }
            }
        };
        player.getObserveController().addObserver(attackedObserver);
        player.setRideObservers(attackedObserver);

        ActionObserver dotAttackedObserver = new ActionObserver(ObserverType.DOT_ATTACKED) {

            @Override
            public void dotattacked(Creature creature, Effect dotEffect) {
                if (Rnd.get(1000) < 200) {
                    player.unsetPlayerMode(PlayerMode.RIDE);
                }
            }
        };
        player.getObserveController().addObserver(dotAttackedObserver);
        player.setRideObservers(dotAttackedObserver);
    }

    public RideInfo getRideInfo() {
        return DataManager.RIDE_DATA.getRideInfo(npcId);
    }
}
