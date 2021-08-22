/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.siegeservice;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.EventNotifier;
import com.ne.gs.configs.main.SiegeConfig;
import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.siege.SiegeNpc;
import com.ne.gs.model.siege.FortressLocation;
import com.ne.gs.model.siege.SiegeLocation;
import com.ne.gs.model.siege.SiegeModType;
import com.ne.gs.model.siege.SiegeRace;
import com.ne.gs.model.templates.npc.AbyssNpcType;
import com.ne.gs.network.aion.serverpackets.SM_SIEGE_LOCATION_STATE;
import com.ne.gs.services.SiegeService;
import com.ne.gs.world.World;

public abstract class Siege<SL extends SiegeLocation> {

    private static final Logger log = LoggerFactory.getLogger(Siege.class);
    private final SiegeBossDeathListener siegeBossDeathListener = new SiegeBossDeathListener(this);
    private final SiegeBossDoAddDamageListener siegeBossDoAddDamageListener = new SiegeBossDoAddDamageListener(this);
    private final AtomicBoolean finished = new AtomicBoolean();
    private final SiegeCounter siegeCounter = new SiegeCounter();
    private final SL siegeLocation;
    private boolean bossKilled;
    private SiegeNpc boss;
    private Date startTime;
    private boolean started;

    public Siege(SL siegeLocation) {
        this.siegeLocation = siegeLocation;
    }

    public final void startSiege() {

        boolean doubleStart = false;

        // keeping synchronization as minimal as possible
        synchronized (this) {
            if (started) {
                doubleStart = true;
            } else {
                startTime = new Date();
                started = true;
            }
        }

        if (doubleStart) {
            log.error("Attempt to start siege of SiegeLocation#" + siegeLocation.getLocationId() + " for 2 times");
            return;
        }

        onSiegeStart();
        if (SiegeConfig.BALAUR_AUTO_ASSAULT) {
            BalaurAssaultService.getInstance().onSiegeStart(this);
        }
    }

    public final void startSiege(int locationId) {
        SiegeService.getInstance().startSiege(locationId);
    }

    public final void stopSiege() {
        if (finished.compareAndSet(false, true)) {
            onSiegeFinish();
            if (SiegeConfig.BALAUR_AUTO_ASSAULT) {
                BalaurAssaultService.getInstance().onSiegeFinish(this);
            }
        } else {
            log.error("Attempt to stop siege of SiegeLocation#" + siegeLocation.getLocationId() + " for 2 times");
        }
    }

    public SL getSiegeLocation() {
        return siegeLocation;
    }

    public int getSiegeLocationId() {
        return siegeLocation.getLocationId();
    }

    public boolean isBossKilled() {
        return bossKilled;
    }

    public void setBossKilled(boolean bossKilled) {
        this.bossKilled = bossKilled;
    }

    public SiegeNpc getBoss() {
        return boss;
    }

    public void setBoss(SiegeNpc boss) {
        this.boss = boss;
    }

    public SiegeCounter getSiegeCounter() {
        return siegeCounter;
    }

    protected abstract void onSiegeStart();

    protected abstract void onSiegeFinish();

    public void addBossDamage(Creature attacker, int damage) {
        // We don't have to add damage anymore if siege is finished
        if (isFinished()) {
            return;
        }

        // Just to be sure that attacker exists.
        // if don't - dunno what to do
        if (attacker == null) {
            return;
        }

        // Actually we don't care if damage was done from summon.
        // We should threat all the damage like it was done from the owner
        attacker = attacker.getMaster();
        getSiegeCounter().addDamage(attacker, damage);
    }

    /**
     * Returns siege duration in seconds or -1 if it's endless
     *
     * @return siege duration in seconnd or -1 if siege should never end using timer
     */
    public abstract boolean isEndless();

    public abstract void addAbyssPoints(Player player, int abysPoints);

    public boolean isStarted() {
        return started;
    }

    public boolean isFinished() {
        return finished.get();
    }

    public Date getStartTime() {
        return startTime;
    }

    protected void registerSiegeBossListeners() {
        // Add hate listener - we should know when someone attacked general
        boss.getNotifier().attach(siegeBossDoAddDamageListener);

        // Add die listener - we should stop the siege when general dies
        boss.getNotifier().attach(siegeBossDeathListener);
    }

    protected void unregisterSiegeBossListeners() {
        // Add hate listener - we should know when someone attacked general
        boss.getNotifier().detach(siegeBossDoAddDamageListener);

        // Add die listener - we should stop the siege when general dies
        boss.getNotifier().detach(siegeBossDeathListener);
    }

    /**
     * TODO: This should be done in some other, more "gentle" way...
     *
     * @deprecated This should be removed
     */
    @Deprecated
    protected void initSiegeBoss() {

        SiegeNpc boss = null;

        Collection<SiegeNpc> npcs = World.getInstance().getLocalSiegeNpcs(getSiegeLocationId());
        for (SiegeNpc npc : npcs) {
            if (npc.getObjectTemplate().getAbyssNpcType().equals(AbyssNpcType.BOSS)) {
                if (boss != null) {
                    throw new SiegeException("Found 2 siege bosses for outpost " + getSiegeLocationId());
                }

                boss = npc;
            }
        }

        if (boss == null) {
            throw new SiegeException("Siege Boss not found for siege " + getSiegeLocationId());
        }

        setBoss(boss);
        registerSiegeBossListeners();
    }

    protected void spawnNpcs(int locationId, SiegeRace race, SiegeModType type) {
        SiegeService.getInstance().spawnNpcs(locationId, race, type);
    }

	protected void spawnNpcsWithoutBoss(int locationId, SiegeRace race, SiegeModType type) {
		SiegeService.getInstance().spawnNpcsWithoutType(locationId, race, type, AbyssNpcType.BOSS);
	}

	protected void spawnNpcs(int locationId, SiegeRace race, SiegeModType type, AbyssNpcType npcType) {
		SiegeService.getInstance().spawnNpcByType(locationId, race, type, npcType);
	}

    protected void deSpawnNpcs(int locationId) {
        SiegeService.getInstance().deSpawnNpcs(locationId);
    }

    protected void broadcastState(SiegeLocation location) {
        SiegeService.getInstance().broadcast(new SM_SIEGE_LOCATION_STATE(location), null);
    }

    protected void broadcastUpdate(SiegeLocation location) {
        SiegeService.getInstance().broadcastUpdate(location);
    }

    protected void broadcastUpdate(SiegeLocation location, int nameId) {
        SiegeService.getInstance().broadcastUpdate(location, DescId.of(nameId));
    }

    protected void updateOutpostStatusByFortress(FortressLocation location) {
        SiegeService.getInstance().updateOutpostStatusByFortress(location);
    }

    protected void updateTiamarantaRiftsStatus(boolean isPreparation, boolean isSync) {
        SiegeService.getInstance().updateTiamarantaRiftsStatus(isPreparation, isSync);
    }
}
