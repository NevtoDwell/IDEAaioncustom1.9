/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.services.CronService;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.dataholders.GoodsListData;
import com.ne.gs.dataholders.TradeListData;
import com.ne.gs.model.limiteditems.LimitedItem;
import com.ne.gs.model.limiteditems.LimitedTradeNpc;
import com.ne.gs.model.templates.goods.GoodsList;
import com.ne.gs.model.templates.tradelist.TradeListTemplate.TradeTab;

/**
 * @author xTz
 *         TYPE_A: BuyLimit == 0 && SellLimit != 0 TYPE_B: BuyLimit != 0 && SellLimit == 0 TYPE_C: BuyLimit != 0 && SellLimit != 0
 */
public class LimitedItemTradeService {

    private static final Logger log = LoggerFactory.getLogger(LimitedItemTradeService.class);
    private final GoodsListData goodsListData = DataManager.GOODSLIST_DATA;
    private final TradeListData tradeListData = DataManager.TRADE_LIST_DATA;
    private final FastMap<Integer, LimitedTradeNpc> limitedTradeNpcs = new FastMap<Integer, LimitedTradeNpc>().shared();

    public void start() {
        for (int npcId : tradeListData.getTradeListTemplate().keys()) {
            for (TradeTab list : tradeListData.getTradeListTemplate(npcId).getTradeTablist()) {
                GoodsList goodsList = goodsListData.getGoodsListById(list.getId());
                if (goodsList == null) {
                    log.warn("No goodslist for tradelist of npc " + npcId);
                    continue;
                }
                FastList<LimitedItem> limitedItems = goodsList.getLimitedItems();
                if (limitedItems.isEmpty()) {
                    continue;
                }
                if (!limitedTradeNpcs.containsKey(npcId)) {
                    limitedTradeNpcs.putIfAbsent(npcId, new LimitedTradeNpc(limitedItems));
                } else {
                    limitedTradeNpcs.get(npcId).putLimitedItems(limitedItems);
                }
            }
        }

        for (LimitedTradeNpc limitedTradeNpc : limitedTradeNpcs.values()) {
            for (final LimitedItem limitedItem : limitedTradeNpc.getLimitedItems()) {
                // FIXME Jenelli  попытка исправить org.quartz.ObjectAlreadyExistsException
                try {
                    Thread.sleep(3);
                } catch (InterruptedException e) {
                }
                CronService.getInstance().schedule(new Runnable() {

                    @Override
                    public void run() {
                        limitedItem.setToDefault();
                    }

                }, limitedItem.getSalesTime());
            }
        }
        log.info("Scheduled Limited Items based on cron expression size: " + limitedTradeNpcs.size());
    }

    public LimitedItem getLimitedItem(int itemId, int npcId) {
        if (limitedTradeNpcs.containsKey(npcId)) {
            for (LimitedItem limitedItem : limitedTradeNpcs.get(npcId).getLimitedItems()) {
                if (limitedItem.getItemId() == itemId) {
                    return limitedItem;
                }
            }
        }
        return null;
    }

    public boolean isLimitedTradeNpc(int npcId) {
        return limitedTradeNpcs.containsKey(npcId);
    }

    public LimitedTradeNpc getLimitedTradeNpc(int npcId) {
        return limitedTradeNpcs.get(npcId);
    }

    public static LimitedItemTradeService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static final class SingletonHolder {

        protected static final LimitedItemTradeService INSTANCE = new LimitedItemTradeService();
    }

}
