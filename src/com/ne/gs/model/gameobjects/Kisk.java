/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.ne.gs.model.NpcType;
import com.ne.gs.world.zone.ZoneInstance;
import javolution.util.FastList;
import javolution.util.FastSet;

import com.ne.gs.controllers.NpcController;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team.legion.Legion;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.model.templates.stats.KiskStatsTemplate;
import com.ne.gs.model.templates.zone.ZoneType;
import com.ne.gs.network.aion.serverpackets.SM_KISK_UPDATE;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.Visitor;

/**
 * @author Sarynth
 */
public class Kisk extends SummonedObject<Player> {

    private final Legion ownerLegion;
    private final Race ownerRace;

    private KiskStatsTemplate kiskStatsTemplate;

    private int remainingResurrections;
    private final long kiskSpawnTime;
    public final int KISK_LIFETIME_IN_SEC = 7200;
    private final Set<Integer> kiskMemberIds;

    /**
     * @param objId
     * @param controller
     * @param spawnTemplate
     */
    public Kisk(int objId, NpcController controller, SpawnTemplate spawnTemplate, NpcTemplate npcTemplate, Player owner) {
        super(objId, controller, spawnTemplate, npcTemplate, npcTemplate.getLevel());

        kiskStatsTemplate = npcTemplate.getKiskStatsTemplate();
        if (kiskStatsTemplate == null) {
            kiskStatsTemplate = new KiskStatsTemplate();
        }

        kiskMemberIds = new FastSet<>(kiskStatsTemplate.getMaxMembers());
        remainingResurrections = kiskStatsTemplate.getMaxResurrects();
        kiskSpawnTime = System.currentTimeMillis() / 1000;
        ownerLegion = owner.getLegion();
        ownerRace = owner.getRace();
    }

    @Override
    public boolean isAttackableNpc() {
        return !isInsideZoneType(ZoneType.NEUTRAL) && getNpcType() == NpcType.ATTACKABLE;
    }

    @Override
    public boolean isEnemy(Creature creature) {
        return creature.isEnemyFrom(this);
    }

    @Override
    public boolean isEnemyFrom(Npc npc) {
        return npc.isAttackableNpc() || npc.isAggressiveTo(this);
    }

    /**
     * Required so that the enemy race can attack the Kisk!
     */
    @Override
    public boolean isEnemyFrom(Player player) {
        int worldId = getPosition().getMapId();

        if(isInsideZoneType(ZoneType.NEUTRAL))
            return false;

        if ((worldId == 600020000 || worldId == 600030000) && !isInsideZoneType(ZoneType.PVP)) {
            return false;
        }
        return player.getRace() != ownerRace;
    }

    /**
     * @return NpcObjectType.NORMAL
     */
    @Override
    public NpcObjectType getNpcObjectType() {
        return NpcObjectType.NORMAL;
    }

    /**
     * 1 ~ race 2 ~ legion 3 ~ solo 4 ~ group 5 ~ alliance
     *
     * @return useMask
     */
    public int getUseMask() {
        return kiskStatsTemplate.getUseMask();
    }

    public List<Player> getCurrentMemberList() {
        List<Player> currentMemberList = new FastList<>();

        for (int memberId : kiskMemberIds) {
            Player member = World.getInstance().findPlayer(memberId);
            if (member != null) {
                currentMemberList.add(member);
            }
        }
        return currentMemberList;
    }

    /**
     * @return
     */
    public int getCurrentMemberCount() {
        return kiskMemberIds.size();
    }

    public Set<Integer> getCurrentMemberIds() {
        return kiskMemberIds;
    }

    /**
     * @return
     */
    public int getMaxMembers() {
        return kiskStatsTemplate.getMaxMembers();
    }

    /**
     * @return
     */
    public int getRemainingResurrects() {
        return remainingResurrections;
    }

    /**
     * @return
     */
    public int getMaxRessurects() {
        return kiskStatsTemplate.getMaxResurrects();
    }

    /**
     * @return
     */
    public int getRemainingLifetime() {
        long timeElapsed = (System.currentTimeMillis() / 1000) - kiskSpawnTime;
        int timeRemaining = (int) (7200 - timeElapsed); // Fixed 2 hours 2 * 60 * 60
        return (timeRemaining > 0 ? timeRemaining : 0);
    }

    /**
     * @param player
     *
     * @return
     */
    public boolean canBind(Player player) {

        if (!player.getName().equals(getMasterName())) {
            // Check if they fit the usemask
            switch (getUseMask()) {
                case 1: // Race
                    if (ownerRace != player.getRace()) {
                        return false;
                    }
                    break;

                case 2: // Legion
                    if (ownerLegion == null || !ownerLegion.isMember(player.getObjectId())) {
                        return false;
                    }
                    break;

                case 3: // Solo
                    return false; // Already Checked Name

                case 4: // Group (PlayerGroup or PlayerAllianceGroup)
                    if (!player.isInTeam() || !player.getCurrentGroup().hasMember(getCreatorId())) {
                        return false;
                    }
                    break;

                case 5: // Alliance
                    if (!player.isInTeam() || player.isInAlliance2() && !player.getPlayerAlliance2().hasMember(getCreatorId()) || player.isInGroup2()
                        && !player.getPlayerGroup2().hasMember(getCreatorId())) {
                        return false;
                    }
                    break;

                default:
                    return false;
            }
        }

        return getCurrentMemberCount() < getMaxMembers();
    }

    /**
     * @param player
     */
    public synchronized void addPlayer(Player player) {
        if (kiskMemberIds.add(player.getObjectId())) {
            broadcastKiskUpdate();
        } else {
            player.sendPck(new SM_KISK_UPDATE(this));
        }
        player.setKisk(this);
    }

    /**
     * @param player
     */
    public synchronized void removePlayer(Player player) {
        player.setKisk(null);
        if (kiskMemberIds.remove(player.getObjectId())) {
            broadcastKiskUpdate();
        }
    }

    /**
     * Sends SM_KISK_UPDATE to each member
     */
    private void broadcastKiskUpdate() {
        // Logic to prevent enemy race from knowing kisk information.
        for (Player member : getCurrentMemberList()) {
            if (!getKnownList().knowns(member)) {
                member.sendPck(new SM_KISK_UPDATE(this));
            }
        }

        final Kisk kisk = this;
        getKnownList().doOnAllPlayers(new Visitor<Player>() {

            @Override
            public void visit(Player object) {
                if (object.getRace() == ownerRace) {
                    object.sendPck(new SM_KISK_UPDATE(kisk));
                }
            }
        });
    }

    /**
     * @param message
     */
    public void broadcastPacket(SM_SYSTEM_MESSAGE message) {
        for (Player member : getCurrentMemberList()) {
            if (member != null) {
                member.sendPck(message);
            }
        }
    }

    /**
     */
    public void resurrectionUsed() {
        remainingResurrections--;
        broadcastKiskUpdate();
        if (remainingResurrections <= 0) {
            getController().onDelete();
        }
    }

    /**
     * @return ownerRace
     */
    public Race getOwnerRace() {
        return ownerRace;
    }

    public boolean isActive() {
        return !getLifeStats().isAlreadyDead() && getRemainingResurrects() > 0;
    }

}
