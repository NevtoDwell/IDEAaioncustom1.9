/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.player;

import com.ne.gs.configs.administration.AdminConfig;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.Kisk;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.templates.item.ItemUseLimits;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.ne.gs.network.aion.serverpackets.SM_MOTION;
import com.ne.gs.network.aion.serverpackets.SM_PLAYER_INFO;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.network.aion.serverpackets.SM_TARGET_SELECTED;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.taskmanager.tasks.TeamMoveUpdater;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.audit.AuditLogger;
import com.ne.gs.world.World;
import com.ne.gs.world.WorldMap;
import com.ne.gs.world.WorldPosition;
import com.ne.gs.world.knownlist.Visitor;

/**
 * @author Jego, xTz
 */
public final class PlayerReviveService {

    public static void duelRevive(Player player) {
        revive(player, 30, 30, false, 0);
        PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.RESURRECT), true);
        player.getGameStats().updateStatsAndSpeedVisually();
        player.unsetResPosState();
    }

    public static void skillRevive(Player player) {
        if (!(player.getResStatus())) {
            cancelRes(player);
            return;
        }

        revive(player, 10, 10, true, player.getResurrectionSkill());

        if (player.getIsFlyingBeforeDeath()) {
            player.setState(CreatureState.FLYING);
        }

        PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.RESURRECT), true);
        player.sendPck(SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME);

        if (player.getIsFlyingBeforeDeath()) {
            player.getFlyController().startFly();
        }
        player.getGameStats().updateStatsAndSpeedVisually();

        if (player.isInPrison()) {
            TeleportService.teleportToPrison(player);
        }

        if (player.isInResPostState()) {
            TeleportService.teleportTo(player, player.getWorldId(), player.getInstanceId(), player.getResPosX(), player.getResPosY(), player.getResPosZ());
        }
        player.unsetResPosState();

        player.setIsFlyingBeforeDeath(false);
    }

    public static void rebirthRevive(Player player) { // саморес
        if (!player.canUseRebirthRevive()) {
            return;
        }
        if (player.getRebirthResurrectPercent() <= 0) {
            player.sendMsg("Error: Rebirth effect missing percent.");
            player.setRebirthResurrectPercent(5);
        }
        boolean soulSickness = true;
        int rebirthResurrectPercent = player.getRebirthResurrectPercent();
        if (player.getAccessLevel() >= AdminConfig.ADMIN_AUTO_RES) {
            rebirthResurrectPercent = 100;
            soulSickness = false;
        }
        // if (player.getRebirthResurrectPercent() > 0) {
        // player.setRebirthResurrectPercent(rebirthResurrectPercent);
        // soulSickness = false;
        // }
        // boolean isFlyingBeforeDeath = player.getIsFlyingBeforeDeath();
        revive(player, rebirthResurrectPercent, rebirthResurrectPercent, soulSickness, player.getRebirthSkill());

        if (player.getIsFlyingBeforeDeath()) {
            player.setState(CreatureState.FLYING);
        }
        PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.RESURRECT), true);
        player.sendPck(SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME);
        if (player.getIsFlyingBeforeDeath()) {
            player.getFlyController().startFly();
        }
        player.getGameStats().updateStatsAndSpeedVisually();

        if (player.isInPrison()) {
            TeleportService.teleportToPrison(player);
        }
        player.unsetResPosState();

        // if player was flying before res, start flying
        player.setIsFlyingBeforeDeath(false);
    }

    public static void bindRevive(Player player) {
        bindRevive(player, 0);
    }

    public static void bindRevive(Player player, int skillId) {
        revive(player, 25, 25, true, skillId);
        player.sendPck(SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME);
        // TODO: It is not always necessary.
        // sendPacket(new SM_QUEST_LIST(activePlayer));
        player.getGameStats().updateStatsAndSpeedVisually();
        player.sendPck(new SM_PLAYER_INFO(player, false));
        player.sendPck(new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()));
        if (player.isInPrison()) {
            TeleportService.teleportToPrison(player);
        } else {
            TeleportService.moveToBindLocation(player, true);
        }
        player.unsetResPosState();
    }

    public static void kiskRevive(Player player) {
        kiskRevive(player, 0);
    }

    public static void kiskRevive(Player player, int skillId) {
        Kisk kisk = player.getKisk();
        if (kisk == null) {
            return;
        }
        if (player.isInPrison()) {
            TeleportService.teleportToPrison(player);
        } else if (kisk.isActive()) {
            WorldPosition bind = kisk.getPosition();
            kisk.resurrectionUsed();
            player.sendPck(SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME);
            revive(player, 25, 25, false, skillId);
            player.getGameStats().updateStatsAndSpeedVisually();

            player.unsetResPosState();
            TeleportService.moveToKiskLocation(player, bind);
        }
    }

    public static void instanceRevive(Player player) {
        instanceRevive(player, 0);
    }

    public static void instanceRevive(Player player, int skillId) {
        // Revive in Instances
        if (player.getPosition().getWorldMapInstance().getInstanceHandler().onReviveEvent(player)) {
            return;
        }
        WorldMap map = World.getInstance().getWorldMap(player.getWorldId());
        if (map == null) {
            bindRevive(player);
            return;
        }
        revive(player, 25, 25, true, skillId);
        player.sendPck(SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME);
        player.getGameStats().updateStatsAndSpeedVisually();
        player.sendPck(new SM_PLAYER_INFO(player, false));
        player.sendPck(new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()));
        if (map.isInstanceType() && (player.getInstanceStartPosX() != 0 && player.getInstanceStartPosY() != 0 && player.getInstanceStartPosZ() != 0)) {
            TeleportService
                .teleportTo(player, player.getWorldId(), player.getInstanceStartPosX(), player.getInstanceStartPosY(), player.getInstanceStartPosZ());
        } else {
            bindRevive(player);
        }
        player.unsetResPosState();
    }

    public static void revive(Player player, int hpPercent, int mpPercent, boolean setSoulsickness, int resurrectionSkill) {
        player.getKnownList().doOnAllPlayers(new Visitor<Player>() {

            @Override
            public void visit(Player visitor) { // -фикс таргета после реса
                VisibleObject target = visitor.getTarget();
                if (target != null && target.getObjectId() == player.getObjectId()) {
                    visitor.setTarget(null); 
                    PacketSendUtility.sendPck(visitor, new SM_TARGET_SELECTED(null));
                }
            }

        });
        boolean isNoResurrectPenalty = player.getController().isNoResurrectPenaltyInEffect();
        player.setPlayerResActivate(false);
        player.getLifeStats().setCurrentHpPercent(isNoResurrectPenalty ? 100 : hpPercent);
        player.getLifeStats().setCurrentMpPercent(isNoResurrectPenalty ? 100 : mpPercent);
        if (player.getLifeStats().getCurrentDp() > 0 && !isNoResurrectPenalty) {
            player.getLifeStats().setCurrentDp(0);
        }
        player.getLifeStats().triggerRestoreOnRevive();
        if ((!isNoResurrectPenalty) && (setSoulsickness)) {
            player.getController().updateSoulSickness(resurrectionSkill);
        }
        player.setResurrectionSkill(0);
        player.getAggroList().clear();
        player.getController().onBeforeSpawn();
        if (player.isInGroup2()) {
            TeamMoveUpdater.getInstance().startTask(player);
        }
    }

    public static void itemSelfRevive(Player player) {
        Item item = player.getSelfRezStone();
        if (item == null) {
            cancelRes(player);
            return;
        }

        // Add Cooldown and use item
        ItemUseLimits useLimits = item.getItemTemplate().getUseLimits();
        int useDelay = useLimits.getDelayTime();
        player.addItemCoolDown(useLimits.getDelayId(), System.currentTimeMillis() + useDelay, useDelay / 1000);

        player.getController().cancelUseItem();
        PacketSendUtility.broadcastPacket(player,
            new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), item.getObjectId(), item.getItemTemplate().getTemplateId()), true);
        if (!player.getInventory().decreaseByObjectId(item.getObjectId(), 1)) {
            cancelRes(player);
            return;
        }

        // Tombstone Self-Rez retail verified 15%
        revive(player, 15, 15, true, player.getResurrectionSkill());

        if (player.getIsFlyingBeforeDeath()) {
            player.setState(CreatureState.FLYING);
        }

        PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.RESURRECT), true);
        player.sendPck(SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME);
        if (player.getIsFlyingBeforeDeath()) {
            player.getFlyController().startFly();
        }
        player.getGameStats().updateStatsAndSpeedVisually();

        if (player.isInPrison()) {
            TeleportService.teleportToPrison(player);
        }
        player.unsetResPosState();

        player.setIsFlyingBeforeDeath(false);
    }

    private static void cancelRes(Player player) {
        AuditLogger.info(player, "Possible selfres hack.");
        player.getController().sendDie();
    }
}
