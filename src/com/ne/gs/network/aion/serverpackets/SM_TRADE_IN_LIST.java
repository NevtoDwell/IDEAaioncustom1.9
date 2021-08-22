/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.templates.tradelist.TradeListTemplate;
import com.ne.gs.model.templates.tradelist.TradeListTemplate.TradeTab;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author MrPoke
 */
public class SM_TRADE_IN_LIST extends AionServerPacket {

    private final Npc npc;
    private final TradeListTemplate tlist;
    private final int buyPriceModifier;

    public SM_TRADE_IN_LIST(Npc npc, TradeListTemplate tlist, int buyPriceModifier) {
        this.npc = npc;
        this.tlist = tlist;
        this.buyPriceModifier = buyPriceModifier;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        if ((tlist != null) && (tlist.getNpcId() != 0) && (tlist.getCount() != 0)) {
            writeD(npc.getObjectId());
            writeC(tlist.getTradeNpcType().index());
            writeD(buyPriceModifier); // Vendor Buy Price Modifier
            writeH(tlist.getCount());
            for (TradeTab tradeTabl : tlist.getTradeTablist()) {
                writeD(tradeTabl.getId());
            }
        }
    }
}
