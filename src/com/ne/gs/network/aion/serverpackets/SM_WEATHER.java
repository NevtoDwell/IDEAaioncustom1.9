/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.Arrays;
import java.util.List;

import com.ne.gs.model.templates.world.WeatherEntry;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author ATracer
 * @author Kwazar
 * @author Nemesiss :D:D
 */
public class SM_WEATHER extends AionServerPacket {

    private WeatherEntry[] weatherEntries;

    public SM_WEATHER(WeatherEntry[] weatherEntries) {
        this.weatherEntries = weatherEntries;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeC(0x00);// unk
        writeC(weatherEntries.length);
        for (WeatherEntry entry : weatherEntries)
            writeC(entry.getCode());
    }
}
