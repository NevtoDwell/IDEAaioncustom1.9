/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.utils.Callback;
import com.ne.gs.configs.main.InGameShopConfig;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.ingameshop.InGameShopEn;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_IN_GAME_SHOP_CATEGORY_LIST;
import com.ne.gs.network.aion.serverpackets.SM_IN_GAME_SHOP_ITEM;
import com.ne.gs.network.aion.serverpackets.SM_IN_GAME_SHOP_LIST;
import com.ne.gs.network.aion.serverpackets.SM_TOLL_INFO;

/**
 * @author xTz, KID
 */
public class CM_IN_GAME_SHOP_INFO extends AionClientPacket {

    private int actionId;
    private int categoryId;
    private int listInCategory;
    private String senderName;
    private String senderMessage;

    @Override
    protected void readImpl() {
        actionId = readC();
        categoryId = readD();
        listInCategory = readD();
        senderName = readS();
        senderMessage = readS();
    }

    @Override
    protected void runImpl() {
        if (InGameShopConfig.ENABLE_IN_GAME_SHOP) {
            final Player player = getConnection().getActivePlayer();

            switch (actionId) {
                case 0x01:
                    player.sendPck(new SM_IN_GAME_SHOP_ITEM(player, categoryId));
                    break;
                case 0x02:
                    player.sendPck(new SM_IN_GAME_SHOP_CATEGORY_LIST(2, categoryId));
                    player.inGameShop.setCategory((byte) categoryId);
                    break;
                case 0x04:
                    player.sendPck(new SM_IN_GAME_SHOP_CATEGORY_LIST(0, categoryId));
                    break;
                case 0x08:// showcat
                    if (categoryId > 1) {
                        player.inGameShop.setSubCategory((byte) categoryId);
                    }

                    player.sendPck(new SM_IN_GAME_SHOP_LIST(player, listInCategory, 1));
                    player.sendPck(new SM_IN_GAME_SHOP_LIST(player, listInCategory, 0));
                    break;
                case 0x10:
                    InGameShopEn.getInstance().querryToll(player, new InGameShopEn.TollQuerry() {
                        @Override
                        public Object onEvent(@NotNull InGameShopEn.TollQuerryResult env) {
                            player.sendPck(new SM_TOLL_INFO(env.toll));
                            return null;
                        }
                    });
                    break;
                case 0x20: // buy
                    InGameShopEn.getInstance().acceptRequest(player, categoryId);
                    break;
                case 0x40: // gift
                    InGameShopEn.getInstance().sendRequest(player, senderName, senderMessage, categoryId);
                    break;
            }
        }
    }
}
