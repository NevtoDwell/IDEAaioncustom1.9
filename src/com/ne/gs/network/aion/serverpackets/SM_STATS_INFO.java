/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.model.stats.container.PlayerGameStats;
import com.ne.gs.model.stats.container.PlayerLifeStats;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.utils.gametime.GameTimeManager;

/**
 * In this packet Server is sending User Info?
 *
 * @author -Nemesiss-
 * @author Luno
 */
public class SM_STATS_INFO extends AionServerPacket {

    /**
     * Player that stats info will be send
     */
    private final Player player;
    private final PlayerGameStats pgs;
    private final PlayerLifeStats pls;
    private final PlayerCommonData pcd;

    /**
     * Constructs new <tt>SM_UI</tt> packet
     *
     * @param player
     */
    public SM_STATS_INFO(Player player) {
        this.player = player;
        pcd = player.getCommonData();
        pgs = player.getGameStats();
        pls = player.getLifeStats();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(player.getObjectId());
        writeD(GameTimeManager.getGameTime().getTime()); // Minutes since 1/1/00 00:00:00

        writeH(pgs.getPower().getCurrent());
        writeH(pgs.getHealth().getCurrent());
        writeH(pgs.getAccuracy().getCurrent());
        writeH(pgs.getAgility().getCurrent());
        writeH(pgs.getKnowledge().getCurrent());
        writeH(pgs.getWill().getCurrent());

        writeH(pgs.getStat(StatEnum.WATER_RESISTANCE, 0).getCurrent());
        writeH(pgs.getStat(StatEnum.WIND_RESISTANCE, 0).getCurrent());
        writeH(pgs.getStat(StatEnum.EARTH_RESISTANCE, 0).getCurrent());
        writeH(pgs.getStat(StatEnum.FIRE_RESISTANCE, 0).getCurrent());
        writeH(pgs.getStat(StatEnum.ELEMENTAL_RESISTANCE_LIGHT, 0).getCurrent());
        writeH(pgs.getStat(StatEnum.ELEMENTAL_RESISTANCE_DARK, 0).getCurrent());

        writeH(player.getLevel());

        writeH(0);
        writeH(0);
        writeH(0);

        writeQ(pcd.getExpNeed());
        writeQ(pcd.getExpRecoverable());
        writeQ(pcd.getExpShown());

        writeD(0);

        writeD(pgs.getMaxHp().getCurrent());
        writeD(pls.getCurrentHp());

        writeD(pgs.getMaxMp().getCurrent());
        writeD(pls.getCurrentMp());

        writeH(pgs.getMaxDp().getCurrent());
        writeH(pls.getCurrentDp());

        writeD(pgs.getFlyTime().getCurrent());
        writeD(pls.getCurrentFp());

        writeH(player.getFlyState());

        writeH(pgs.getMainHandPAttack().getCurrent());
        writeH(pgs.getOffHandPAttack().getCurrent());

        writeH(0);

        writeD(pgs.getPDef().getCurrent());
        writeH(pgs.getMAttack().getCurrent());
        writeH(0);
        writeD(pgs.getMDef().getCurrent());
        writeH(pgs.getMResist().getCurrent());
        writeH(0);
        writeF(pgs.getAttackRange().getCurrent() / 1000.0F);
        writeH(pgs.getAttackSpeed().getCurrent());
        writeH(pgs.getEvasion().getCurrent());
        writeH(pgs.getParry().getCurrent());
        writeH(pgs.getBlock().getCurrent());
        writeH(pgs.getMainHandPCritical().getCurrent());
        writeH(pgs.getOffHandPCritical().getCurrent());
        writeH(pgs.getMainHandPAccuracy().getCurrent());
        writeH(pgs.getOffHandPAccuracy().getCurrent());

        writeH(0);

        writeH(pgs.getMAccuracy().getCurrent());
        writeH(pgs.getMCritical().getCurrent());

        writeH(0);

        writeF(pgs.getReverseStat(StatEnum.BOOST_CASTING_TIME, 1000).getCurrent() / 1000.0F);
        writeH(pgs.getStat(StatEnum.CONCENTRATION, 0).getCurrent());
        writeH(pgs.getMBoost().getCurrent());
        writeH(pgs.getMBResist().getCurrent());
        writeH(pgs.getStat(StatEnum.HEAL_BOOST, 0).getCurrent());

        writeH(pgs.getPCDef().getCurrent());

        writeH(pgs.getStat(StatEnum.MAGICAL_CRITICAL_RESIST, 0).getCurrent());
        writeH(pgs.getStat(StatEnum.PHYSICAL_CRITICAL_DAMAGE_REDUCE, 0).getCurrent());
        writeH(pgs.getStat(StatEnum.MAGICAL_CRITICAL_DAMAGE_REDUCE, 0).getCurrent());

        writeD(player.getInventory().getLimit());
        writeD(player.getInventory().size());
        writeD(0);
        writeD(0);
        writeD(pcd.getPlayerClass().getClassId());

        writeH(0);
        writeH(0);

        writeQ(pcd.getCurrentReposteEnergy());
        writeQ(pcd.getMaxReposteEnergy());
        writeQ(pcd.getCurrentSalvationPercent());

        writeH(pgs.getPower().getBase());
        writeH(pgs.getHealth().getBase());
        writeH(pgs.getAccuracy().getBase());
        writeH(pgs.getAgility().getBase());
        writeH(pgs.getKnowledge().getBase());
        writeH(pgs.getWill().getBase());
        writeH(pgs.getStat(StatEnum.WATER_RESISTANCE, 0).getBase());
        writeH(pgs.getStat(StatEnum.WIND_RESISTANCE, 0).getBase());
        writeH(pgs.getStat(StatEnum.EARTH_RESISTANCE, 0).getBase());
        writeH(pgs.getStat(StatEnum.FIRE_RESISTANCE, 0).getBase());
        writeH(pgs.getStat(StatEnum.ELEMENTAL_RESISTANCE_LIGHT, 0).getBase());
        writeH(pgs.getStat(StatEnum.ELEMENTAL_RESISTANCE_DARK, 0).getBase());
        writeD(pgs.getMaxHp().getBase());
        writeD(pgs.getMaxMp().getBase());
        writeD(pgs.getMaxDp().getBase());
        writeD(pgs.getFlyTime().getBase());
        writeH(pgs.getMainHandPAttack().getBase());
        writeH(pgs.getOffHandPAttack().getBase());
        writeD(pgs.getMAttack().getBase());
        writeD(pgs.getPDef().getBase());
        writeD(pgs.getMDef().getBase());
        writeD(pgs.getMResist().getBase());
        writeF(pgs.getAttackRange().getBase() / 1000.0F);
        writeH(pgs.getEvasion().getBase());
        writeH(pgs.getParry().getBase());
        writeH(pgs.getBlock().getBase());
        writeH(pgs.getMainHandPCritical().getBase());
        writeH(pgs.getOffHandPCritical().getBase());
        writeH(pgs.getMCritical().getBase());

        writeH(0);
        writeH(pgs.getMainHandPAccuracy().getBase());
        writeH(pgs.getOffHandPAccuracy().getBase());

        writeH(0);
        writeH(pgs.getMAccuracy().getBase());
        writeH(pgs.getStat(StatEnum.CONCENTRATION, 0).getBase());
        writeH(pgs.getMBoost().getBase());
        writeH(pgs.getMBResist().getBase());
        writeH(pgs.getStat(StatEnum.HEAL_BOOST, 0).getBase());
        writeH(pgs.getPCDef().getBase());
        writeH(pgs.getStat(StatEnum.MAGICAL_CRITICAL_RESIST, 0).getBase());
        writeH(pgs.getStat(StatEnum.PHYSICAL_CRITICAL_DAMAGE_REDUCE, 0).getBase());
        writeH(pgs.getStat(StatEnum.MAGICAL_CRITICAL_DAMAGE_REDUCE, 0).getBase());
        writeH(0);
    }
}
