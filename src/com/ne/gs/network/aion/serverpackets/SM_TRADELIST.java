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
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.limiteditems.LimitedItem;
import com.ne.gs.model.limiteditems.LimitedTradeNpc;
import com.ne.gs.model.templates.tradelist.TradeListTemplate;
import com.ne.gs.model.templates.tradelist.TradeListTemplate.TradeTab;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.services.LimitedItemTradeService;

/**
 * @author alexa026, ATracer, Sarynth, xTz
 */
public class SM_TRADELIST extends AionServerPacket {

    private final Integer playerObj;
    private final int npcObj;
    private final int npcId;
    private final TradeListTemplate tlist;
    private final int buyPriceModifier;

    public SM_TRADELIST(Player player, Npc npc, TradeListTemplate tlist, int buyPriceModifier) {
        playerObj = player.getObjectId();
        npcObj = npc.getObjectId();
        npcId = npc.getNpcId();
        this.tlist = tlist;
        this.buyPriceModifier = buyPriceModifier;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        if ((tlist != null) && (tlist.getNpcId() != 0) && (tlist.getCount() != 0)) {
            writeD(npcObj);
            writeC(tlist.getTradeNpcType().index()); // reward, abyss or normal
            writeD(buyPriceModifier); // Vendor Buy Price Modifier
            writeH(tlist.getCount());
            for (TradeTab tradeTabl : tlist.getTradeTablist()) {
                writeD(tradeTabl.getId());
            }

            int i = 0;
            LimitedTradeNpc limitedTradeNpc = null;
            if (LimitedItemTradeService.getInstance().isLimitedTradeNpc(npcId)) {
                limitedTradeNpc = LimitedItemTradeService.getInstance().getLimitedTradeNpc(npcId);
                i = limitedTradeNpc.getLimitedItems().size();
            }
            writeH(i);
            if (limitedTradeNpc != null) {
                for (LimitedItem limitedItem : limitedTradeNpc.getLimitedItems()) {
                    writeD(limitedItem.getItemId());
                    writeH(limitedItem.getBuyCount().get(playerObj) == null ? 0 : limitedItem.getBuyCount().get(playerObj));
                    writeH(limitedItem.getSellLimit());
                }
            }
        }
    }
}
