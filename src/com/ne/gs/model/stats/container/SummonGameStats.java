/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.stats.container;

import com.ne.gs.model.EmotionType;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.stats.calc.Stat2;
import com.ne.gs.model.templates.stats.SummonStatsTemplate;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.network.aion.serverpackets.SM_SUMMON_UPDATE;
import com.ne.gs.taskmanager.tasks.PacketBroadcaster.BroadcastMode;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class SummonGameStats extends CreatureGameStats<Summon> {

    private int cachedSpeed;
    private final SummonStatsTemplate statsTemplate;

    /**
     * @param owner
     * @param statsTemplate
     */
    public SummonGameStats(Summon owner, SummonStatsTemplate statsTemplate) {
        super(owner);
        this.statsTemplate = statsTemplate;
    }

    @Override
    protected void onStatsChange() {
        updateStatsAndSpeedVisually();
    }

    public void updateStatsAndSpeedVisually() {
        updateStatsVisually();
        checkSpeedStats();
    }

    public void updateStatsVisually() {
        owner.addPacketBroadcastMask(BroadcastMode.UPDATE_STATS);
    }

    private void checkSpeedStats() {
        int current = getMovementSpeed().getCurrent();
        if (current != cachedSpeed) {
            owner.addPacketBroadcastMask(BroadcastMode.UPDATE_SPEED);
        }
        cachedSpeed = current;
    }

    @Override
    public Stat2 getStat(StatEnum statEnum, int base) {
        Stat2 stat = super.getStat(statEnum, base);
        if (owner.getMaster() == null) {
            return stat;
        }
        switch (statEnum) {
            case MAXHP:
                stat.setBonusRate(0.5F);
                return owner.getMaster().getGameStats().getItemStatBoost(statEnum, stat);
            case BOOST_MAGICAL_SKILL:
            case MAGIC_SKILL_BOOST_RESIST:
                stat.setBonusRate(0.8F);
                return owner.getMaster().getGameStats().getItemStatBoost(statEnum, stat);
            case PHYSICAL_ATTACK:
                stat.setBonusRate(0.3f);
                return owner.getMaster().getGameStats().getItemStatBoost(statEnum, stat);
            case PHYSICAL_DEFENSE:
            case EVASION:
            case MAGICAL_ACCURACY:
            case MAGICAL_RESIST:
                stat.setBonusRate(0.5f);
                return owner.getMaster().getGameStats().getItemStatBoost(statEnum, stat);
            case PHYSICAL_ACCURACY:
                stat.setBonusRate(0.5f);
                owner.getMaster().getGameStats().getItemStatBoost(StatEnum.MAIN_HAND_ACCURACY, stat);
                return owner.getMaster().getGameStats().getItemStatBoost(statEnum, stat);
            case PHYSICAL_CRITICAL:
                stat.setBonusRate(0.5f);
                owner.getMaster().getGameStats().getItemStatBoost(StatEnum.MAIN_HAND_CRITICAL, stat);
                return owner.getMaster().getGameStats().getItemStatBoost(statEnum, stat);

        }
        return stat;
    }

    @Override
    public Stat2 getMaxHp() {
        return getStat(StatEnum.MAXHP, statsTemplate.getMaxHp());
    }

    @Override
    public Stat2 getMaxMp() {
        return getStat(StatEnum.MAXHP, statsTemplate.getMaxMp());
    }

    @Override
    public Stat2 getAttackSpeed() {
        return getStat(StatEnum.ATTACK_SPEED, owner.getObjectTemplate().getAttackDelay());
    }

    @Override
    public Stat2 getMovementSpeed() {
        int bonusSpeed = 0;
        Player master = owner.getMaster();
        if (master != null && (master.isInFlyingState() || master.isInState(CreatureState.GLIDING))) {
            bonusSpeed += 3000;
        }
        return getStat(StatEnum.SPEED, Math.round(statsTemplate.getRunSpeed() * 1000) + bonusSpeed);
    }

    @Override
    public Stat2 getAttackRange() {
        return getStat(StatEnum.ATTACK_RANGE, owner.getObjectTemplate().getAttackRange() * 1000);
    }

    @Override
    public Stat2 getPDef() {
        return getStat(StatEnum.PHYSICAL_DEFENSE, statsTemplate.getPdefense());
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
        return getStat(StatEnum.MAGICAL_RESIST, statsTemplate.getMresist());
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
        return getStat(StatEnum.PHYSICAL_ACCURACY, 100);
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
        return getStat(StatEnum.EVASION, statsTemplate.getEvasion());
    }

    @Override
    public Stat2 getParry() {
        return getStat(StatEnum.PARRY, statsTemplate.getParry());
    }

    @Override
    public Stat2 getBlock() {
        return getStat(StatEnum.BLOCK, statsTemplate.getBlock());
    }

    @Override
    public Stat2 getMainHandPAttack() {
        return getStat(StatEnum.PHYSICAL_ATTACK, statsTemplate.getMainHandAttack());
    }

    @Override
    public Stat2 getMainHandPCritical() {
        return getStat(StatEnum.PHYSICAL_CRITICAL, statsTemplate.getMainHandCritRate());
    }

    @Override
    public Stat2 getMainHandPAccuracy() {
        return getStat(StatEnum.PHYSICAL_ACCURACY, statsTemplate.getMainHandAccuracy());
    }

    @Override
    public Stat2 getMAttack() {
        return getStat(StatEnum.MAGICAL_ATTACK, 100);
    }

    @Override
    public Stat2 getMBoost() {
        return getStat(StatEnum.BOOST_MAGICAL_SKILL, 0);
    }

    @Override
    public Stat2 getMAccuracy() {
        return getStat(StatEnum.MAGICAL_ACCURACY, statsTemplate.getMagicAccuracy());
    }

    @Override
    public Stat2 getMCritical() {
        return getStat(StatEnum.MAGICAL_CRITICAL, statsTemplate.getMcrit());
    }

    @Override
    public Stat2 getHpRegenRate() {
        int base = (int) (owner.getLifeStats().getMaxHp() * (owner.getMode().getId() == 2 ? 0.05F : 0.025F));
        return getStat(StatEnum.REGEN_HP, base);
    }

    @Override
    public Stat2 getMpRegenRate() {
        throw new IllegalStateException("No mp regen for Summon");
    }

    @Override
    public void updateStatInfo() {
        Player master = owner.getMaster();
        if (master != null) {
            master.sendPck(new SM_SUMMON_UPDATE(owner));
        }
    }

    @Override
    public void updateSpeedInfo() {
        PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.START_EMOTE2, 0, 0));
    }
}
