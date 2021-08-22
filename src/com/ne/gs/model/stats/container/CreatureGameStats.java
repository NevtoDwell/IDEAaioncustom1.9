/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.stats.container;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import gnu.trove.map.hash.THashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.SkillElement;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.items.ManaStone;
import com.ne.gs.model.stats.calc.AdditionStat;
import com.ne.gs.model.stats.calc.ReverseStat;
import com.ne.gs.model.stats.calc.Stat2;
import com.ne.gs.model.stats.calc.StatCapUtil;
import com.ne.gs.model.stats.calc.StatOwner;
import com.ne.gs.model.stats.calc.functions.IStatFunction;
import com.ne.gs.model.stats.calc.functions.StatFunction;
import com.ne.gs.model.stats.calc.functions.StatFunctionProxy;

/**
 * @author xavier, hex1r0
 */
public abstract class CreatureGameStats<T extends Creature> {

    protected static final Logger log = LoggerFactory.getLogger(CreatureGameStats.class);

    private static final int ATTACK_MAX_COUNTER = Integer.MAX_VALUE;
    private long lastGeoUpdate = 0;

    private final Map<StatEnum, TreeSet<IStatFunction>> stats = new THashMap<>(0);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private int attackCounter = 0;
    protected T owner = null;

    private Stat2 cachedHPStat;
    private Stat2 cachedMPStat;

    protected CreatureGameStats(T owner) {
        this.owner = owner;
    }

    /**
     * @return the atcount
     */
    public int getAttackCounter() {
        return attackCounter;
    }

    public void increaseAttackCounter() {
        if (attackCounter == ATTACK_MAX_COUNTER) {
            this.attackCounter = 1;
        } else {
            this.attackCounter++;
        }
    }

    public final void addEffectOnly(StatOwner statOwner, List<? extends IStatFunction> functions) {
        lock.writeLock().lock();
        try {
            for (IStatFunction function : functions) {
                TreeSet<IStatFunction> mods = stats.get(function.getName());
                if (mods == null) {
                    mods = new TreeSet<>();
                    stats.put(function.getName(), mods);
                }

                IStatFunction func = function;
                if (function instanceof StatFunction) {
                    func = new StatFunctionProxy(statOwner, function);
                }

                if (mods.contains(func)) {
                    log.warn("Effect " + statOwner + " already active" + func);
                }

                mods.add(func);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public final void addEffect(StatOwner statOwner, List<? extends IStatFunction> functions) {
        addEffectOnly(statOwner, functions);
        onStatsChange();
    }

    public final void endEffect(StatOwner statOwner) {
        lock.writeLock().lock();
        try {
            for (TreeSet<IStatFunction> e : stats.values()) {
                for (Iterator<IStatFunction> iter = e.iterator(); iter.hasNext(); ) {
                    IStatFunction ownedMod = iter.next();
                    if (ownedMod.getOwner() != null && ownedMod.getOwner().equals(statOwner)) {
                        iter.remove();
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
        onStatsChange();
    }

    public int getPositiveReverseStat(StatEnum statEnum, int base) {
        Stat2 stat = getReverseStat(statEnum, base);
        int value = stat.getCurrent();
        return value > 0 ? value : 0;
    }

    public Stat2 getStat(StatEnum statEnum, int base) {
        Stat2 stat = new AdditionStat(statEnum, base, owner);
        return getStat(statEnum, stat);
    }

    public Stat2 getStat(StatEnum statEnum, int base, float bonusRate) {
        Stat2 stat = new AdditionStat(statEnum, base, owner, bonusRate);
        return getStat(statEnum, stat);
    }

    public Stat2 getReverseStat(StatEnum statEnum, int base) {
        Stat2 stat = new ReverseStat(statEnum, base, owner);
        return getStat(statEnum, stat);
    }

    public Stat2 getStat(StatEnum statEnum, Stat2 stat) {
        lock.readLock().lock();
        try {
            TreeSet<IStatFunction> functions = stats.get(statEnum);
            if (functions == null) {
                return stat;
            }
            for (IStatFunction func : functions) {
                if (func.validate(stat, func)) {
                    func.apply(stat);
                }
            }
            StatCapUtil.calculateBaseValue(stat, owner.isPlayer());
            return stat;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Stat2 getItemStatBoost(StatEnum statEnum, Stat2 stat) {
        lock.readLock().lock();
        try {
            TreeSet<IStatFunction> functions = stats.get(statEnum);
            if (functions == null || functions.isEmpty()) {
                return stat;
            }
            for (IStatFunction func : functions) {
                if (func.validate(stat, func) && (func.getOwner() instanceof Item || func
                    .getOwner() instanceof ManaStone)) {
                    func.apply(stat);
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        return stat;
    }

    public abstract Stat2 getMaxHp();

    public abstract Stat2 getMaxMp();

    public abstract Stat2 getAttackSpeed();

    public abstract Stat2 getMovementSpeed();

    public abstract Stat2 getAttackRange();

    public abstract Stat2 getPDef();

    public abstract Stat2 getPCDef();

    public abstract Stat2 getMDef();

    public abstract Stat2 getMResist();

    public abstract Stat2 getPower();

    public abstract Stat2 getHealth();

    public abstract Stat2 getAccuracy();

    public abstract Stat2 getAgility();

    public abstract Stat2 getKnowledge();

    public abstract Stat2 getWill();

    public abstract Stat2 getEvasion();

    public abstract Stat2 getParry();

    public abstract Stat2 getBlock();

    public abstract Stat2 getMainHandPAttack();

    public abstract Stat2 getMainHandPCritical();

    public abstract Stat2 getMainHandPAccuracy();

    public abstract Stat2 getMAttack();

    public abstract Stat2 getMBoost();

    public abstract Stat2 getMBResist();

    public abstract Stat2 getMAccuracy();

    public abstract Stat2 getMCritical();

    public abstract Stat2 getHpRegenRate();

    public abstract Stat2 getMpRegenRate();

    public int getMagicalDefenseFor(SkillElement element) {
        switch (element) {
            case EARTH:
                return getStat(StatEnum.EARTH_RESISTANCE, 0).getCurrent();
            case FIRE:
                return getStat(StatEnum.FIRE_RESISTANCE, 0).getCurrent();
            case WATER:
                return getStat(StatEnum.WATER_RESISTANCE, 0).getCurrent();
            case WIND:
                return getStat(StatEnum.WIND_RESISTANCE, 0).getCurrent();
            case LIGHT:
                return getStat(StatEnum.ELEMENTAL_RESISTANCE_LIGHT, 0).getCurrent();
            case DARK:
                return getStat(StatEnum.ELEMENTAL_RESISTANCE_DARK, 0).getCurrent();
            default:
                return 0;
        }
    }

    public float getMovementSpeedFloat() {
        return getMovementSpeed().getCurrent() / 1000f;
    }

    /**
     * Send packet about stats info
     */
    public void updateStatInfo() {
    }

    /**
     * Send packet about speed info
     */
    public void updateSpeedInfo() {
    }

    public TreeSet<IStatFunction> getStatsByStatEnum(StatEnum stat) {
        lock.readLock().lock();
        try {
            return stats.get(stat);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean checkGeoNeedUpdate() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastGeoUpdate > 600) {
            lastGeoUpdate = currentTime;
            return true;
        }
        return false;
    }

    /**
     * Perform additional calculations after effects added/removed<br>
     * This method will be called outside of stats lock.
     */
    protected void onStatsChange() {
        checkHPStats();
        checkMPStats();
    }

    private void checkHPStats() {
        Stat2 oldHP = cachedHPStat;
        cachedHPStat = null;
        Stat2 newHP = this.getMaxHp();
        cachedHPStat = newHP;
        if (oldHP == null) {
            return;
        }
        if (oldHP.getCurrent() != newHP.getCurrent()) {
            float percent = 1f * newHP.getCurrent() / oldHP.getCurrent();
            owner.getLifeStats().setCurrentHp(Math.round(owner.getLifeStats().getCurrentHp() * percent));
        }
    }

    private void checkMPStats() {
        Stat2 oldMP = cachedMPStat;
        cachedMPStat = null;
        Stat2 newMP = this.getMaxMp();
        cachedMPStat = newMP;
        if (oldMP == null) {
            return;
        }
        if (oldMP.getCurrent() != newMP.getCurrent()) {
            float percent = 1f * newMP.getCurrent() / oldMP.getCurrent();
            owner.getLifeStats().setCurrentMp(Math.round(owner.getLifeStats().getCurrentMp() * percent));
        }
    }
}
