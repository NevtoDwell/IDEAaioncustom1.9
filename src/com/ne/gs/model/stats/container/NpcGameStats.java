/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.stats.container;

import com.ne.commons.utils.Rnd;
import com.ne.gs.ai2.AI2Logger;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.SummonedObject;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.stats.calc.Stat2;
import com.ne.gs.model.templates.stats.NpcStatsTemplate;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.taskmanager.tasks.PacketBroadcaster.BroadcastMode;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author xavier
 */
public class NpcGameStats extends CreatureGameStats<Npc> {

    int currentRunSpeed = 0;
    private long lastAttackTime = 0;
    private long lastAttackedTime = 0;
    private long nextAttackTime = 0;
    private long lastSkillTime = 0;
    private long fightStartingTime = 0;
    private int cachedState;
    private Stat2 cachedSpeedStat;
    private long lastGeoZUpdate;
    private long lastChangeTarget = 0;
    private int pAccuracy = 0;
    private int mRes = 0;

    public NpcGameStats(Npc owner) {
        super(owner);
    }

    @Override
    protected void onStatsChange() {
        checkSpeedStats();
    }

    private void checkSpeedStats() {
        Stat2 oldSpeed = cachedSpeedStat;
        cachedSpeedStat = null;
        Stat2 newSpeed = getMovementSpeed();
        cachedSpeedStat = newSpeed;
        if (oldSpeed == null || oldSpeed.getCurrent() != newSpeed.getCurrent()) {
            owner.addPacketBroadcastMask(BroadcastMode.UPDATE_SPEED);
        }
    }

    @Override
    public Stat2 getMaxHp() {
        return getStat(StatEnum.MAXHP, owner.getObjectTemplate().getStatsTemplate().getMaxHp());
    }

    @Override
    public Stat2 getMaxMp() {
        return getStat(StatEnum.MAXMP, owner.getObjectTemplate().getStatsTemplate().getMaxMp());
    }

    @Override
    public Stat2 getAttackSpeed() {
        return getStat(StatEnum.ATTACK_SPEED, owner.getObjectTemplate().getAttackDelay());
    }

    @Override
    public Stat2 getMovementSpeed() {
        int currentState = owner.getState();
        Stat2 cachedSpeed = cachedSpeedStat;
        if (cachedSpeed != null && cachedState == currentState) {
            return cachedSpeed;
        }
        Stat2 newSpeedStat = null;
        if (owner.isFlying()) {
            newSpeedStat = getStat(StatEnum.FLY_SPEED, Math.round(owner.getObjectTemplate().getStatsTemplate().getRunSpeed() * 1.3f * 1000));
        } else if (owner.isInState(CreatureState.WEAPON_EQUIPPED)) {
            newSpeedStat = getStat(StatEnum.SPEED, Math.round(owner.getObjectTemplate().getStatsTemplate().getRunSpeedFight() * 1000));
        } else if (owner.isInState(CreatureState.WALKING)) {
            newSpeedStat = getStat(StatEnum.SPEED, Math.round(owner.getObjectTemplate().getStatsTemplate().getWalkSpeed() * 1000));
        } else {
            newSpeedStat = getStat(StatEnum.SPEED, Math.round(owner.getObjectTemplate().getStatsTemplate().getRunSpeed() * 1000));
        }
        cachedState = currentState;
        cachedSpeedStat = newSpeedStat;
        return newSpeedStat;
    }

    @Override
    public Stat2 getAttackRange() {
        return getStat(StatEnum.ATTACK_RANGE, owner.getObjectTemplate().getAttackRange() * 1000);
    }

    @Override
    public Stat2 getPDef() {
        return getStat(StatEnum.PHYSICAL_DEFENSE, owner.getObjectTemplate().getStatsTemplate().getPdef());
    }

    @Override
    public Stat2 getPCDef() {
        return getStat(StatEnum.PHYSICAL_CRITICAL_RESIST, 0);
    }

    @Override
    public Stat2 getMDef() {
        return getStat(StatEnum.MAGICAL_DEFEND, 0);
    }

    @Override
    public Stat2 getMResist() {
        if (mRes == 0) {
            mRes = Math.round(owner.getLevel() * 17.5f + 75);
        }
        return getStat(StatEnum.MAGICAL_RESIST, mRes);
    }

    @Override
    public Stat2 getMBResist() {
        int base = 0;
        return getStat(StatEnum.MAGIC_SKILL_BOOST_RESIST, base);
    }

    @Override
    public Stat2 getPower() {
        return getStat(StatEnum.POWER, 100);
    }

    @Override
    public Stat2 getHealth() {
        return getStat(StatEnum.HEALTH, 100);
    }

    @Override
    public Stat2 getAccuracy() {
        return getStat(StatEnum.ACCURACY, 100);
    }

    @Override
    public Stat2 getAgility() {
        return getStat(StatEnum.AGILITY, 100);
    }

    @Override
    public Stat2 getKnowledge() {
        return getStat(StatEnum.KNOWLEDGE, 100);
    }

    @Override
    public Stat2 getWill() {
        return getStat(StatEnum.WILL, 100);
    }

    @Override
    public Stat2 getEvasion() {
        if (pAccuracy == 0) {
            calcStats();
        }
        return getStat(StatEnum.EVASION, pAccuracy);
    }

    @Override
    public Stat2 getParry() {
        return getStat(StatEnum.PARRY, 100);
    }

    @Override
    public Stat2 getBlock() {
        return getStat(StatEnum.BLOCK, 0);
    }

    @Override
    public Stat2 getMainHandPAttack() {
        return getStat(StatEnum.PHYSICAL_ATTACK, owner.getObjectTemplate().getStatsTemplate().getMainHandAttack());
    }

    @Override
    public Stat2 getMainHandPCritical() {
        return getStat(StatEnum.PHYSICAL_CRITICAL, 10);
    }

    @Override
    public Stat2 getMainHandPAccuracy() {
        if (pAccuracy == 0) {
            calcStats();
        }
        return getStat(StatEnum.PHYSICAL_ACCURACY, pAccuracy);
    }

    @Override
    public Stat2 getMAttack() {
        return getStat(StatEnum.MAGICAL_ATTACK, 100);
    }

    @Override
    public Stat2 getMBoost() {
        return getStat(StatEnum.BOOST_MAGICAL_SKILL, 100);
    }

    @Override
    public Stat2 getMAccuracy() {
        if (pAccuracy == 0) {
            calcStats();
        }
        // Trap's MAccuracy is being calculated into TrapGameStats and is related to master's MAccuracy
        if (owner instanceof SummonedObject) {
            return getStat(StatEnum.MAGICAL_ACCURACY, pAccuracy);
        }
        return getMainHandPAccuracy();
    }

    @Override
    public Stat2 getMCritical() {
        return getStat(StatEnum.MAGICAL_CRITICAL, 50);
    }

    @Override
    public Stat2 getHpRegenRate() {
        NpcStatsTemplate nst = owner.getObjectTemplate().getStatsTemplate();
        return getStat(StatEnum.REGEN_HP, nst.getMaxHp() / 4);
    }

    @Override
    public Stat2 getMpRegenRate() {
        throw new IllegalStateException("No mp regen for NPC");
    }

    public int getLastAttackTimeDelta() {
        return Math.round((System.currentTimeMillis() - lastAttackTime) / 1000f);
    }

    public int getLastAttackedTimeDelta() {
        return Math.round((System.currentTimeMillis() - lastAttackedTime) / 1000f);
    }

    public void renewLastAttackTime() {
        lastAttackTime = System.currentTimeMillis();
    }

    public void renewLastAttackedTime() {
        lastAttackedTime = System.currentTimeMillis();
    }

    public boolean isNextAttackScheduled() {
        return nextAttackTime - System.currentTimeMillis() > 50;
    }

    public void setFightStartingTime() {
        fightStartingTime = System.currentTimeMillis();
    }

    public long getFightStartingTime() {
        return fightStartingTime;
    }

    public void setNextAttackTime(long nextAttackTime) {
        this.nextAttackTime = nextAttackTime;
    }

    /**
     * @return next possible attack time depending on stats
     */
    public int getNextAttackInterval() {
        long attackDelay = System.currentTimeMillis() - lastAttackTime;
        int attackSpeed = getAttackSpeed().getCurrent();
        if (attackSpeed == 0) {
            attackSpeed = 2000;
        }
        if (owner.getAi2().isLogging()) {
            AI2Logger.info(owner.getAi2(), "adelay = " + attackDelay + " aspeed = " + attackSpeed);
        }
        int nextAttack = 0;
        if (attackDelay < attackSpeed) {
            nextAttack = (int) (attackSpeed - attackDelay);
        }
        return nextAttack;
    }

    /**
     * @return next possible skill time depending on time
     */

    public void renewLastSkillTime() {
        lastSkillTime = System.currentTimeMillis();
    }

    public void renewLastChangeTargetTime() {
        lastChangeTarget = System.currentTimeMillis();
    }

    public int getLastSkillTimeDelta() {
        return Math.round((System.currentTimeMillis() - lastSkillTime) / 1000f);
    }

    public int getLastChangeTargetTimeDelta() {
        return Math.round((System.currentTimeMillis() - lastChangeTarget) / 1000f);
    }

    public boolean canUseNextSkill() {
        if (getLastSkillTimeDelta() >= 6 + Rnd.get(-3, 3)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void updateSpeedInfo() {
        PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.START_EMOTE2, 0, 0));
    }

    public final long getLastGeoZUpdate() {
        return lastGeoZUpdate;
    }

    /**
     * @param lastGeoZUpdate
     *     the lastGeoZUpdate to set
     */
    public void setLastGeoZUpdate(long lastGeoZUpdate) {
        this.lastGeoZUpdate = lastGeoZUpdate;
    }

    private void calcStats() {
        int lvl = owner.getLevel();
        double accuracy = lvl * (33.6f - (0.16 * lvl)) + 5;
        switch (owner.getObjectTemplate().getRank()) {
            case NOVICE:
            case DISCIPLINED:
                break;
            case SEASONED:
                accuracy *= 1.05f;
                break;
            case EXPERT:
                accuracy *= 1.15f;
                break;
            case MASTER:
                accuracy *= 1.25f;
                break;
            case VETERAN:
                accuracy *= 1.35f;
                break;
        }
        pAccuracy = (int) Math.round(accuracy);
    }
}
