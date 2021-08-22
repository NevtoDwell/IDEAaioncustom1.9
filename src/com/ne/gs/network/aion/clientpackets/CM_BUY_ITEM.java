/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.tradelist.TradeListTemplate;
import com.ne.gs.model.templates.tradelist.TradeNpcType;
import com.ne.gs.model.trade.RepurchaseList;
import com.ne.gs.model.trade.TradeList;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.services.PrivateStoreService;
import com.ne.gs.services.RepurchaseService;
import com.ne.gs.services.TradeService;
import com.ne.gs.utils.audit.AuditLogger;
import com.ne.gs.world.World;

/**
 * @author orz, ATracer, Simple, xTz
 */
public class CM_BUY_ITEM extends AionClientPacket {

    private static final Logger log = LoggerFactory.getLogger(CM_BUY_ITEM.class);

    private int sellerObjId;
    private int tradeActionId;
    private int amount;
    private int itemId;
    private long count;
    private boolean isAudit;
    private TradeList tradeList;
    private RepurchaseList repurchaseList;

    @Override
    protected void readImpl() {
        Player player = getConnection().getActivePlayer();
        sellerObjId = readD();
        tradeActionId = readH();
        amount = readH(); // total no of items

        if (amount < 0 || amount > 36) {
            isAudit = true;
            AuditLogger.info(player, "Player might be abusing CM_BUY_ITEM amount: " + amount);
            return;
        }
        if (tradeActionId == 2) {
            repurchaseList = new RepurchaseList(sellerObjId);
        } else {
            tradeList = new TradeList(sellerObjId);
        }

        for (int i = 0; i < amount; i++) {
            itemId = readD();
            count = readQ();

            // prevent exploit packets
            if (count < 0 || itemId <= 0 && tradeActionId != 0 || itemId == 190000073 || itemId == 190000074 || count > 20000) {
                isAudit = true;
                AuditLogger.info(player, "Player might be abusing CM_BUY_ITEM item: " + itemId + " count: " + count);
                break;
            }

            switch (tradeActionId) {
                case 0:
                case 1:
                    tradeList.addSellItem(itemId, count);
                    break;
                case 2:
                    repurchaseList.addRepurchaseItem(player, itemId, count);
                    break;
                case 13:
                case 14:
                case 15:
                    tradeList.addBuyItem(itemId, count);
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        if (isAudit || player == null) {
            return;
        }
        VisibleObject target = player.getTarget();

        if (target == null) {
            return;
        }

        if (target.getObjectId() != sellerObjId) {
            AuditLogger.info(player, "Trade exploit, send fake");
            return;
        }

        switch (tradeActionId) {
            case 0:
                Player targetPlayer = (Player) World.getInstance().findVisibleObject(sellerObjId);
                PrivateStoreService.sellStoreItem(targetPlayer, player, tradeList);
                break;
            case 1:
                TradeService.performSellToShop(player, tradeList);
                break;
            case 2:
                RepurchaseService.getInstance().repurchaseFromShop(player, repurchaseList);
                break;
            case 13:
                Npc npc = (Npc) World.getInstance().findVisibleObject(sellerObjId);
                TradeListTemplate tlist = DataManager.TRADE_LIST_DATA.getTradeListTemplate(npc.getNpcId());
                if (tlist.getTradeNpcType() == TradeNpcType.NORMAL) {
                    TradeService.performBuyFromShop(player, tradeList);
                }
                break;
            case 14:
                Npc npc1 = (Npc) World.getInstance().findVisibleObject(sellerObjId);
                TradeListTemplate tlist1 = DataManager.TRADE_LIST_DATA.getTradeListTemplate(npc1.getNpcId());
                if (tlist1.getTradeNpcType() == TradeNpcType.ABYSS) {
                    TradeService.performBuyFromAbyssShop(player, tradeList);
                }
                break;
            case 15:
                Npc npc2 = (Npc) World.getInstance().findVisibleObject(sellerObjId);
                TradeListTemplate tlist2 = DataManager.TRADE_LIST_DATA.getTradeListTemplate(npc2.getNpcId());
                if (tlist2.getTradeNpcType() == TradeNpcType.REWARD) {
                    TradeService.performBuyFromRewardShop(player, tradeList);
                }
                break;
            default:
                log.info(String.format("Unhandle shop action unk1: %d", tradeActionId));
                break;
        }
    }
}
