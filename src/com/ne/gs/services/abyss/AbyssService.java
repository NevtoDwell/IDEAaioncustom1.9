/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.abyss;

import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.utils.stats.AbyssRankEnum;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.Visitor;

/**
 * @author ATracer
 */
public final class AbyssService {

    private static final int[] abyssMapList = {210010000, 210020000, 210030000, 210040000, 210050000,
        220010000, 220020000, 220030000, 220040000, 220070000, 400010000, 600010000, 600020000, 600030000, 600040000, 210060000, 220050000};

    public static final boolean isOnPvpMap(Player player) {
        for (int i : abyssMapList) {
            if (i == player.getWorldId()) {
                return true;
            }
        }
        return false;
    }

    public static void rankedKillAnnounce(final Player victim) {
        World.getInstance().doOnAllPlayers(new Visitor<Player>() {
            @Override
            public void visit(Player p) {
                if (p != victim) {
                    p.sendPck(SM_SYSTEM_MESSAGE.STR_ABYSS_ORDER_RANKER_DIE(victim, AbyssRankEnum.getRankDescriptionId(victim)));
                }
            }
        });
    }

    public static final void rankerSkillAnnounce(final Player player, final int nameId) {
        World.getInstance().doOnAllPlayers(new Visitor<Player>() {
            @Override
            public void visit(Player p) {
                if (p != player) {
                    p.sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_ABYSS_SKILL_IS_FIRED(player, new DescId(nameId)));
                }
            }
        });
    }
}
