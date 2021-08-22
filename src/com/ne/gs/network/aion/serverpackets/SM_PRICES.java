/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.services.trade.PricesService;

/**
 * @author xavier, Sarynth modified by Wakizashi Price/tax in Influence ration dialog
 */
public class SM_PRICES extends AionServerPacket {

    @Override
    protected void writeImpl(AionConnection con) {
        writeC(PricesService.getGlobalPrices(con.getActivePlayer().getRace())); // Display Buying Price
        // %
        writeC(PricesService.getGlobalPricesModifier()); // Buying Modified Price %
        writeC(PricesService.getTaxes(con.getActivePlayer().getRace())); // Tax = -100 + C %
    }
}
