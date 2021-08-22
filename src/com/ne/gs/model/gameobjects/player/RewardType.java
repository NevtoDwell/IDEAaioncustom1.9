/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player;

import com.ne.gs.model.stats.container.StatEnum;

/**
 * @author antness
 */
public enum RewardType {
    HUNTING {
        @Override
        public long calcReward(Player player, long reward) {
            float statRate = player.getGameStats().getStat(StatEnum.BOOST_HUNTING_XP_RATE, 100).getCurrent() / 100f;
            return (long) (reward * player.getRates().getXpRate() * statRate);
        }
    },
    GROUP_HUNTING {
        @Override
        public long calcReward(Player player, long reward) {
            float statRate = player.getGameStats().getStat(StatEnum.BOOST_GROUP_HUNTING_XP_RATE, 100).getCurrent() / 100f;
            return (long) (reward * player.getRates().getGroupXpRate() * statRate);
        }
    },
    PVP_KILL {
        @Override
        public long calcReward(Player player, long reward) {
            return (reward);
        }
    },
    QUEST {
        @Override
        public long calcReward(Player player, long reward) {
            float statRate = player.getGameStats().getStat(StatEnum.BOOST_QUEST_XP_RATE, 100).getCurrent() / 100f;
            return (long) (reward * player.getRates().getQuestXpRate() * statRate);
        }
    },
    CRAFTING {
        @Override
        public long calcReward(Player player, long reward) {
            float statRate = player.getGameStats().getStat(StatEnum.BOOST_CRAFTING_XP_RATE, 100).getCurrent() / 100f;
            return (long) (reward * player.getRates().getCraftingXPRate() * statRate);
        }
    },
    GATHERING {
        @Override
        public long calcReward(Player player, long reward) {
            float statRate = player.getGameStats().getStat(StatEnum.BOOST_GATHERING_XP_RATE, 100).getCurrent() / 100f;
            return (long) (reward * player.getRates().getGatheringXPRate() * statRate);
        }
    };

    public abstract long calcReward(Player player, long reward);
}
