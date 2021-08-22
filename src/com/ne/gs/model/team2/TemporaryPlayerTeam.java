/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2;

import java.util.Collection;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.common.legacy.LootGroupRules;
import com.ne.gs.model.team2.common.legacy.LootRuleType;
import com.ne.gs.model.team2.group.PlayerFilters;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.serverpackets.SM_PET;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author ATracer
 */
public abstract class TemporaryPlayerTeam<TM extends TeamMember<Player>> extends GeneralTeam<Player, TM> {

    private LootGroupRules lootGroupRules = new LootGroupRules();

    public TemporaryPlayerTeam(Integer objId) {
        super(objId);
    }

    /**
     * Level of the player with lowest exp
     */
    public abstract int getMinExpPlayerLevel();

    /**
     * Level of the player with highest exp
     */
    public abstract int getMaxExpPlayerLevel();

    @Override
    public Race getRace() {
        return getLeader().getObject().getRace();
    }

    @Override
    public void sendPacket(AionServerPacket packet) {
        applyOnMembers(new TeamMessageSender(packet, Predicates.<Player>alwaysTrue()));
    }

    @Override
    public void sendPacket(AionServerPacket packet, Predicate<Player> predicate) {
        applyOnMembers(new TeamMessageSender(packet, predicate));
    }

    @Override
    public final int onlineMembers() {
        return getOnlineMembers().size();
    }

    @Override
    public final Collection<Player> getOnlineMembers() {
        return filterMembers(PlayerFilters.ONLINE);
    }

    protected final void initializeTeam(TM leader) {
        setLeader(leader);
    }

    public final LootGroupRules getLootGroupRules() {
        return lootGroupRules;
    }

    public void setLootGroupRules(LootGroupRules lootGroupRules) {
        this.lootGroupRules = lootGroupRules;
        if (lootGroupRules != null && lootGroupRules.getLootRule() == LootRuleType.FREEFORALL) {
            applyOnMembers(new TeamPacketGroupSender(PlayerFilters.HAS_LOOT_PET, new AionServerPacket[]{SM_SYSTEM_MESSAGE.STR_MSG_LOOTING_PET_MESSAGE03,
                                                                                                        new SM_PET(13, false)}));
        }
    }

    public static final class TeamMessageSender implements Predicate<Player> {

        private final AionServerPacket packet;
        private final Predicate<Player> predicate;

        public TeamMessageSender(AionServerPacket packet, Predicate<Player> predicate) {
            this.packet = packet;
            this.predicate = predicate;
        }

        @Override
        public boolean apply(Player player) {
            if (predicate.apply(player)) {
                player.sendPck(packet);
            }
            return true;
        }
    }

    public static final class TeamPacketGroupSender implements Predicate<Player> {

        private final AionServerPacket[] packets;
        private final Predicate<Player> predicate;

        public TeamPacketGroupSender(Predicate<Player> predicate, AionServerPacket[] packets) {
            this.packets = packets;
            this.predicate = predicate;
        }

        @Override
        public boolean apply(Player player) {
            if (predicate.apply(player)) {
                for (AionServerPacket packet : packets) {
                    player.sendPck(packet);
                }
            }
            return true;
        }
    }
}
