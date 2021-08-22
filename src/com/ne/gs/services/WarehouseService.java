/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.RequestResponseHandler;
import com.ne.gs.model.items.storage.StorageType;
import com.ne.gs.model.templates.WarehouseExpandTemplate;
import com.ne.gs.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.network.aion.serverpackets.SM_WAREHOUSE_INFO;

/**
 * @author Simple
 */
public final class WarehouseService {

    private static final Logger log = LoggerFactory.getLogger(WarehouseService.class);

    private static final int MIN_EXPAND = 0;
    private static final int MAX_EXPAND = 10;

    /**
     * Shows Question window and expands on positive response
     *
     * @param player
     * @param npc
     */
    public static void expandWarehouse(final Player player, final Npc npc) {
        WarehouseExpandTemplate expandTemplate = DataManager.WAREHOUSEEXPANDER_DATA.getWarehouseExpandListTemplate(npc.getNpcId());

        if (expandTemplate == null) {
            log.error("Warehouse Expand Template could not be found for Npc ID: " + npc.getObjectTemplate().getTemplateId());
            return;
        }

        if (npcCanExpandLevel(expandTemplate, player.getWarehouseSize() + 1) && validateNewSize(player.getWarehouseSize() + 1)) {
            if (validateNewSize(player.getWarehouseSize() + 1)) {
                /**
                 * Check if our player can pay the warehouse expand price
                 */
                final int price = getPriceByLevel(expandTemplate, player.getWarehouseSize() + 1);
                RequestResponseHandler responseHandler = new RequestResponseHandler(npc) {

                    @Override
                    public void acceptRequest(Creature requester, Player responder) {
                        if (player.getInventory().getKinah() < price) {
                            player.sendPck(new SM_SYSTEM_MESSAGE(1300831));
                            return;
                        }
                        expand(responder);
                        player.getInventory().decreaseKinah(price);
                    }

                    @Override
                    public void denyRequest(Creature requester, Player responder) {
                        // nothing to do
                    }
                };

                boolean result = player.getResponseRequester().putRequest(900686, responseHandler);
                if (result) {
                    player.sendPck(new SM_QUESTION_WINDOW(900686, 0, 0, String.valueOf(price)));
                }
            }
        } else {
            player.sendPck(new SM_SYSTEM_MESSAGE(1300432));
        }
    }

    /**
     * @param player
     */
    public static void expand(Player player) {
        if (!canExpand(player)) {
            return;
        }
        player.sendPck(new SM_SYSTEM_MESSAGE(1300433, "8"));
        player.setWarehouseSize(player.getWarehouseSize() + 1);

        sendWarehouseInfo(player, false);
    }

    /**
     * Checks if new player cube is not max
     *
     * @param level
     *
     * @return true or false
     */
    private static boolean validateNewSize(int level) {
        // check min and max level
        if (level < MIN_EXPAND || level > MAX_EXPAND) {
            return false;
        }
        return true;
    }

    /**
     * @param player
     *
     * @return
     */
    public static boolean canExpand(Player player) {
        return validateNewSize(player.getWarehouseSize() + 1);
    }

    /**
     * Checks if npc can expand level
     *
     * @param clist
     * @param level
     *
     * @return true or false
     */
    private static boolean npcCanExpandLevel(WarehouseExpandTemplate clist, int level) {
        // check if level exists in template
        if (!clist.contains(level)) {
            return false;
        }
        return true;
    }

    /**
     * The guy who created cube template should blame himself :) One day I will rewrite them
     *
     * @param level
     *
     * @return
     */
    private static int getPriceByLevel(WarehouseExpandTemplate clist, int level) {
        return clist.get(level).getPrice();
    }

    /**
     * Sends correctly warehouse packets
     *
     * @param player
     */
    public static void sendWarehouseInfo(Player player, boolean sendAccountWh) {
        List<Item> items = player.getStorage(StorageType.REGULAR_WAREHOUSE.getId()).getItems();

        int whSize = player.getWarehouseSize();
        int itemsSize = items.size();

        /**
         * Regular warehouse
         */
        boolean firstPacket = true;
        if (itemsSize != 0) {
            int index = 0;

            while (index + 10 < itemsSize) {
                player.sendPck(new SM_WAREHOUSE_INFO(items.subList(index, index + 10), StorageType.REGULAR_WAREHOUSE.getId(), whSize,
                    firstPacket, player));
                index += 10;
                firstPacket = false;
            }
            player.sendPck(new SM_WAREHOUSE_INFO(items.subList(index, itemsSize), StorageType.REGULAR_WAREHOUSE.getId(), whSize,
                firstPacket, player));
        }

        player.sendPck(new SM_WAREHOUSE_INFO(null, StorageType.REGULAR_WAREHOUSE.getId(), whSize, false, player));

        if (sendAccountWh) {
            /**
             * Account warehouse
             */
            player.sendPck(new SM_WAREHOUSE_INFO(player.getStorage(StorageType.ACCOUNT_WAREHOUSE.getId()).getItemsWithKinah(),
                StorageType.ACCOUNT_WAREHOUSE.getId(), 0, true, player));
        }

        player.sendPck(new SM_WAREHOUSE_INFO(null, StorageType.ACCOUNT_WAREHOUSE.getId(), 0, false, player));
    }
}
