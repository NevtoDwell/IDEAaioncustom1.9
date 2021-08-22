/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.RequestResponseHandler;
import com.ne.gs.model.items.storage.StorageType;
import com.ne.gs.model.templates.CubeExpandTemplate;
import com.ne.gs.network.aion.serverpackets.SM_CUBE_UPDATE;
import com.ne.gs.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;

/**
 * @author ATracer
 * @author Simple
 * @reworked Luzien
 */
public final class CubeExpandService {

    private static final Logger log = LoggerFactory.getLogger(CubeExpandService.class);

    private static final int MIN_EXPAND = 0;
    private static final int MAX_EXPAND = 12;

    /**
     * Shows Question window and expands on positive response
     *
     * @param player
     * @param npc
     */
    public static void expandCube(final Player player, final Npc npc) {
        CubeExpandTemplate expandTemplate = DataManager.CUBEEXPANDER_DATA.getCubeExpandListTemplate(npc.getNpcId());

        if (expandTemplate == null) {
            log.error("Cube Expand Template could not be found for Npc ID: " + npc.getObjectId());
            return;
        }

        if (npcCanExpandLevel(expandTemplate, player.getNpcExpands() + 1) && canExpand(player)) {
            /**
             * Check if player is allowed to expand by buying
             */
            if (player.getNpcExpands() >= CustomConfig.NPC_CUBE_EXPANDS_SIZE_LIMIT) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_EXTEND_INVENTORY_CANT_EXTEND_MORE);
                return;
            }
            /**
             * Check if our player can pay the cubic expand price
             */
            final int price = getPriceByLevel(expandTemplate, player.getNpcExpands() + 1);

            RequestResponseHandler responseHandler = new RequestResponseHandler(npc) {

                @Override
                public void acceptRequest(Creature requester, Player responder) {
                    if (price > player.getInventory().getKinah()) {
                        player.sendPck(SM_SYSTEM_MESSAGE.STR_WAREHOUSE_EXPAND_NOT_ENOUGH_MONEY);
                        return;
                    }
                    expand(responder, true);
                    player.getInventory().decreaseKinah(price);
                }

                @Override
                public void denyRequest(Creature requester, Player responder) {
                    // nothing to do
                }
            };

            boolean result = player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_WAREHOUSE_EXPAND_WARNING, responseHandler);
            if (result) {
                player.sendPck(new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_WAREHOUSE_EXPAND_WARNING, 0, 0, String.valueOf(price)));
            }
        } else {
            player.sendPck(new SM_SYSTEM_MESSAGE(1300430));
        }
    }

    /**
     * Expands the cubes
     *
     * @param player
     * @param isNpcExpand
     *     TODO
     */
    public static void expand(Player player, boolean isNpcExpand) {
        if (!canExpand(player)) {
            return;
        }
        player.sendPck(new SM_SYSTEM_MESSAGE(1300431, "9"));
        if (isNpcExpand) {
            player.setNpcExpands(player.getNpcExpands() + 1);
        } else {
            player.setQuestExpands(player.getQuestExpands() + 1);
        }
        player.sendPck(SM_CUBE_UPDATE.cubeSize(StorageType.CUBE, player));
    }

    /**
     * @param player
     *
     * @return
     */
    public static boolean canExpand(Player player) {
        return validateNewSize(player.getNpcExpands() + player.getQuestExpands() + 1);
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
     * Checks if npc can expand level
     *
     * @param clist
     * @param level
     *
     * @return true or false
     */
    private static boolean npcCanExpandLevel(CubeExpandTemplate clist, int level) {
        // check if level exists in template
        if (!clist.contains(level)) {
            return false;
        }
        return true;
    }

    /**
     * The guy who created cube template should blame himself :) One day I will rewrite them
     *
     * @param clist
     * @param level
     *
     * @return
     */
    private static int getPriceByLevel(CubeExpandTemplate clist, int level) {
        return clist.get(level).getPrice();
    }
}
