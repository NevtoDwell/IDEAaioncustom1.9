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

public final class HousingConfig {

    @Property(key = "gameserver.housing.visibility.distance", defaultValue = "200")
    public static float VISIBILITY_DISTANCE = 200.0F;

    @Property(key = "modules.housing.enter.accesslevel", defaultValue = "3")
    public static int ENTER_ACCESSLEVEL;

    @Property(key = "modules.housing.auction", defaultValue = "false")
    public static boolean AUCTION_STATUS;

    @Property(key = "modules.housing.auctiontime", defaultValue = "0 5 12 ? * SUN")
    public static String AUCTION_TIME;

    @Property(key = "modules.housing.auction.lotcount", defaultValue = "25")
    public static int AUCTION_LOT_COUNT;

    @Property(key = "modules.housing.auction.registerend", defaultValue = "0 0 0 ? * SAT")
    public static String HOUSE_REGISTER_END;

    @Property(key = "modules.housing.rent", defaultValue = "false")
    public static boolean RENT_STATUS;

    @Property(key = "modules.housing.renttime", defaultValue = "0 0 23 * * ?")
    public static String RENT_TIME;

    @Property(key = "modules.housing.auction.default_refund", defaultValue = "0.3f")
    public static float BID_REFUND_PERCENT;

    @Property(key = "modules.housing.auction.steplimit", defaultValue = "100")
    public static float HOUSE_AUCTION_BID_LIMIT;

    @Property(key = "modules.housing.scripts.debug", defaultValue = "false")
    public static boolean HOUSE_SCRIPT_DEBUG;
}
