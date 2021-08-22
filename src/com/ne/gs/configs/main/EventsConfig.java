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

/**
 * @author Rolandas
 */
public final class EventsConfig {

    /**
     * Event Enabled
     */
    @Property(key = "gameserver.event.enable", defaultValue = "false")
    public static boolean EVENT_ENABLED;

    /**
     * Event Rewarding Membership
     */
    @Property(key = "gameserver.event.membership", defaultValue = "0")
    public static int EVENT_REWARD_MEMBERSHIP;

    @Property(key = "gameserver.event.membership.rate", defaultValue = "false")
    public static boolean EVENT_REWARD_MEMBERSHIP_RATE;

    /**
     * Event Rewarding Period
     */
    @Property(key = "gameserver.event.period", defaultValue = "60")
    public static int EVENT_PERIOD;

    /**
     * Event Reward Values
     */
    @Property(key = "gameserver.event.item.elyos", defaultValue = "141000001")
    public static int EVENT_ITEM_ELYOS;

    @Property(key = "gameserver.event.item.asmo", defaultValue = "141000001")
    public static int EVENT_ITEM_ASMO;

    @Property(key = "gameserver.events.givejuice", defaultValue = "160009017")
    public static int EVENT_GIVEJUICE;

    @Property(key = "gameserver.events.givecake", defaultValue = "160010073")
    public static int EVENT_GIVECAKE;

    @Property(key = "gameserver.event.count", defaultValue = "1")
    public static int EVENT_ITEM_COUNT;

    @Property(key = "gameserver.event.service.enable", defaultValue = "false")
    public static boolean ENABLE_EVENT_SERVICE;

}
