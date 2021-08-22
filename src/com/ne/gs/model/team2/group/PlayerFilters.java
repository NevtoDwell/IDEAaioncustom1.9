/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team2.group;

import com.google.common.base.Predicate;

import com.ne.gs.model.gameobjects.Pet;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.pet.PetFunctionType;

/**
 * @author ATracer
 */
public final class PlayerFilters {

    public static final Predicate<Player> ONLINE = new Predicate<Player>() {

        @Override
        public boolean apply(Player member) {
            return member.isOnline();
        }
    };

    public static final Predicate<Player> HAS_LOOT_PET = new Predicate<Player>() {

        @Override
        public boolean apply(Player member) {
            Pet pet = member.getPet();
            if (pet == null) {
                return false;
            }
            return pet.getPetTemplate().getPetFunction(PetFunctionType.LOOT) != null;
        }
    };

    public static final class ExcludePlayerFilter implements Predicate<Player> {

        private final Player player;

        public ExcludePlayerFilter(Player player) {
            this.player = player;
        }

        @Override
        public boolean apply(Player member) {
            return !player.getObjectId().equals(member.getObjectId());
        }

    }

    public static final class SameInstanceFilter implements Predicate<Player> {

        private final Player player;

        public SameInstanceFilter(Player player) {
            this.player = player;
        }

        @Override
        public boolean apply(Player member) {
            return member.getInstanceId() == player.getInstanceId();
        }

    }

    public static final class MentorSuiteFilter implements Predicate<Player> {

        private final Player player;

        public MentorSuiteFilter(Player player) {
            this.player = player;
        }

        @Override
        public boolean apply(Player member) {
            return member.getLevel() + 9 < player.getLevel();
        }

    }
}
