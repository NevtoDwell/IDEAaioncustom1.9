/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.configs.main;

import com.ne.commons.configuration.Property;

public final class CraftConfig {

    /**
     * Enable craft skills unrestricted level-up
     */
    @Property(key = "gameserver.craft.skills.unrestricted.levelup.enable", defaultValue = "false")
    public static boolean UNABLE_CRAFT_SKILLS_UNRESTRICTED_LEVELUP;

    /**
     * Maximum number of expert skills a player can have
     */
    @Property(key = "gameserver.craft.max.expert.skills", defaultValue = "2")
    public static int MAX_EXPERT_CRAFTING_SKILLS;

    /**
     * Maximum number of master skills a player can have
     */
    @Property(key = "gameserver.craft.max.master.skills", defaultValue = "1")
    public static int MAX_MASTER_CRAFTING_SKILLS;
    @Property(key = "gameserver.craft.critical.rate.regular", defaultValue = "15")
    public static int CRAFT_CRIT_RATE;

    @Property(key = "gameserver.craft.critical.rate.premium", defaultValue = "15")
    public static int PREMIUM_CRAFT_CRIT_RATE;

    @Property(key = "gameserver.craft.critical.rate.vip", defaultValue = "15")
    public static int VIP_CRAFT_CRIT_RATE;

    @Property(key = "gameserver.craft.combo.rate.regular", defaultValue = "25")
    public static int CRAFT_COMBO_RATE;

    @Property(key = "gameserver.craft.combo.rate.premium", defaultValue = "25")
    public static int PREMIUM_CRAFT_COMBO_RATE;

    @Property(key = "gameserver.craft.combo.rate.vip", defaultValue = "25")
    public static int VIP_CRAFT_COMBO_RATE;
}
