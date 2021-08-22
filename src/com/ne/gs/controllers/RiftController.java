/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.RequestResponseHandler;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.ne.gs.network.aion.serverpackets.SM_RIFT_ANNOUNCE;
import com.ne.gs.services.RespawnService;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.spawnengine.RiftSpawnManager.RiftEnum;
import com.ne.gs.utils.audit.AuditLogger;
import com.ne.gs.world.WorldMapInstance;
import com.ne.gs.world.knownlist.Visitor;

/**
 * @author ATracer
 */
public class RiftController extends NpcController {

    private boolean isMaster = false;
    private SpawnTemplate slaveSpawnTemplate;
    private Npc slave;

    private final Integer maxEntries;
    private Integer minLevel;
    private Integer maxLevel;

    private int usedEntries = 0;
    private boolean isAccepting;

    private final RiftEnum riftTemplate;

    private final int deSpawnedTime;

    /**
     * Used to create master rifts or slave rifts (slave == null)
     *
     */

    public RiftController(Npc slave, RiftEnum riftTemplate) {
        deSpawnedTime = ((int) (System.currentTimeMillis() / 1000)) + 60 * 60;
        this.riftTemplate = riftTemplate;
        maxEntries = riftTemplate.getEntries();
        if (slave != null)// master rift should be created
        {
            this.slave = slave;
            slaveSpawnTemplate = slave.getSpawn();
            minLevel = riftTemplate.getMinLevel();
            maxLevel = riftTemplate.getMaxLevel();
            isMaster = true;
            isAccepting = true;
        }
    }

    @Override
    public void onDialogRequest(Player player) {
        if (!isMaster && !isAccepting) {
            return;
        }

        RequestResponseHandler responseHandler = new RequestResponseHandler(getOwner()) {

            @Override
            public void acceptRequest(Creature requester, Player responder) {
                if (!isAccepting) {
                    return;
                }

                if (!getOwner().isSpawned()) {
                    return;
                }
                if (responder.getLevel() > getMaxLevel() || responder.getLevel() < getMinLevel()) {
                    AuditLogger.info(responder, "Rift level restriction hack detected.");
                    return;
                }

                int worldId = slaveSpawnTemplate.getWorldId();
                float x = slaveSpawnTemplate.getX();
                float y = slaveSpawnTemplate.getY();
                float z = slaveSpawnTemplate.getZ();

                TeleportService.teleportTo(responder, worldId, x, y, z);
                usedEntries++;

                if (usedEntries >= maxEntries) {
                    isAccepting = false;

                    RespawnService.scheduleDecayTask(getOwner());
                    RespawnService.scheduleDecayTask(slave);
                }

                WorldMapInstance worldInstance = getOwner().getPosition().getMapRegion().getParent();
                final SM_RIFT_ANNOUNCE masterPacket = new SM_RIFT_ANNOUNCE(getThis(), true);
                worldInstance.doOnAllPlayers(new Visitor<Player>() {
                    @Override
                    public void visit(Player player) {
                        player.sendPck(masterPacket);
                    }
                });
            }

            @Override
            public void denyRequest(Creature requester, Player responder) {
                // do nothing
            }
        };

        boolean requested = player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_ASK_PASS_BY_DIRECT_PORTAL, responseHandler);
        if (requested) {
            player.sendPck(new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_ASK_PASS_BY_DIRECT_PORTAL, 0, 0));
        }
    }

    @Override
    public void onDelete() {

        WorldMapInstance worldInstance = getOwner().getPosition().getMapRegion().getParent();
        final SM_RIFT_ANNOUNCE packet = new SM_RIFT_ANNOUNCE(getOwner().getObjectId());
        worldInstance.doOnAllPlayers(new Visitor<Player>() {
            @Override
            public void visit(Player player) {
                if (player.isSpawned()) {
                    player.sendPck(packet);
                }
            }
        });

        super.onDelete();
    }

    /**
     * @param activePlayer
     */
    public void sendMessage(Player activePlayer) {
        if (!getOwner().isSpawned()) {
            return;
        }
        if (isMaster) {
            activePlayer.sendPck(new SM_RIFT_ANNOUNCE(this, isMaster));
            activePlayer.sendPck(new SM_RIFT_ANNOUNCE(riftTemplate.getDestination()));
        } else {
            activePlayer.sendPck(new SM_RIFT_ANNOUNCE(this, isMaster));
        }
    }

    /**
     *
     */
    public void sendAnnounce() {
        if (getOwner().isSpawned()) {
            WorldMapInstance worldInstance = getOwner().getPosition().getMapRegion().getParent();
            final SM_RIFT_ANNOUNCE masterPacket = new SM_RIFT_ANNOUNCE(this, true);
            final SM_RIFT_ANNOUNCE slavePacket = new SM_RIFT_ANNOUNCE(this, false);
            final SM_RIFT_ANNOUNCE announcePacket = new SM_RIFT_ANNOUNCE(riftTemplate.getDestination());
            worldInstance.doOnAllPlayers(new Visitor<Player>() {
                @Override
                public void visit(Player player) {
                    if (player.isSpawned()) {
                        if (isMaster) {
                            player.sendPck(masterPacket);
                            player.sendPck(announcePacket);
                        } else {
                            player.sendPck(slavePacket);
                        }
                    }
                }
            });
        }
    }

    /**
     * @return the maxEntries
     */
    public Integer getMaxEntries() {
        return maxEntries;
    }

    /**
     * @return the minLevel
     */
    public Integer getMinLevel() {
        return minLevel;
    }

    /**
     * @return the maxLevel
     */
    public Integer getMaxLevel() {
        return maxLevel;
    }

    /**
     * @return the riftTemplate
     */
    public RiftEnum getRiftTemplate() {
        return riftTemplate;
    }

    /**
     * @return the usedEntries
     */
    public int getUsedEntries() {
        return usedEntries;
    }

    private RiftController getThis() {
        return this;
    }

    public int getRemainTime() {
        return deSpawnedTime - (int) (System.currentTimeMillis() / 1000);
    }
}
