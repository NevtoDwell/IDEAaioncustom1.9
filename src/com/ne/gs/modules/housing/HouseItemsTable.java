/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.housing;

import com.ne.commons.database.SqlTable;

/**
 * @author hex1r0
 */
@SqlTable(name = "house_items")
enum HouseItemsTable implements com.ne.commons.database.TableColumn {
    item_uid("item_uid", Integer.class),
    player_id("player_id", Integer.class),
    item_id("item_id", Integer.class),
    installed("installed", Boolean.class),
    x("x", Float.class),
    y("y", Float.class),
    z("z", Float.class),
    h("h", Short.class),
    installedtime("installedtime", Integer.class),
    cooldown("cooldown", Integer.class),
    usages("usages", Short.class);

    private final String _name;
    private final Class<?> _type;

    HouseItemsTable(String name, Class<?> type) {
        _name = name;
        _type = type;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public Class<?> getType() {
        return _type;
    }
}
