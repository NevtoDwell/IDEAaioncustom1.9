/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import com.ne.commons.annotations.NotNull;
import com.ne.gs.database.GDB;
import com.ne.gs.configs.main.WeddingsConfig;
import com.ne.gs.database.dao.WeddingDAO;
import com.ne.gs.model.Wedding;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.ingameshop.InGameShopEn;
import com.ne.gs.network.aion.serverpackets.SM_TOLL_INFO;
import com.ne.gs.network.loginserver.LoginServer;
import com.ne.gs.network.loginserver.serverpackets.SM_ACCOUNT_TOLL_INFO;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.world.World;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author synchro2
 */

public class WeddingService {

    private final Map<Integer, Wedding> weddings = new HashMap<>();

    public static WeddingService getInstance() {
        return SingletonHolder.instance;
    }

    public void registerOffer(Player partner1, Player partner2, Player priest) {
        if (!canRegister(partner1, partner2)) {
            priest.sendMsg("Один из игроков уже находится в браке.");
            return;
        }
        weddings.put(partner1.getObjectId(), new Wedding(partner1, partner2, priest));
        weddings.put(partner2.getObjectId(), new Wedding(partner2, partner1, priest));
    }

    private boolean canRegister(Player partner1, Player partner2) {
        return (getWedding(partner1) == null && getWedding(partner2) == null && !partner1.isMarried() && !partner2.isMarried());
    }

    public void acceptWedding(Player player) {
        final Player partner = getPartner(player);
        final Player requested = player;

        Wedding playersWedding = getWedding(player);
        Wedding partnersWedding = getWedding(partner);

        playersWedding.setAccept();
        if (partnersWedding.isAccepted()) {
            if (!checkConditions(player, partner)) {
                cleanWedding(player, partner);
            } else {

                if(WeddingsConfig.WEDDINGS_TOLLS > 0){

                    InGameShopEn.getInstance().querryToll(partner, new InGameShopEn.TollQuerry() {
                        @Override
                        public Object onEvent(@NotNull InGameShopEn.TollQuerryResult env) {

                            long playerToll = env.toll;
                            long toll = playerToll - WeddingsConfig.WEDDINGS_TOLLS;

                            if (toll < 0) {

                                partner.sendMsg("У вас недостаточно кредитов! Свадебный налог: " + WeddingsConfig.WEDDINGS_TOLLS + " кредитов.");
                                requested.sendMsg("У партнера недостаточно кредитов!");
                                cleanWedding(requested, partner);

                            } else if (LoginServer.getInstance().sendPacket(new SM_ACCOUNT_TOLL_INFO(toll, partner.getAcountName()))){

                                partner.sendPck(new SM_TOLL_INFO(toll));

                                doWedding(requested, partner);

                                if (WeddingsConfig.WEDDINGS_GIFT_ENABLE)
                                    giveGifts(requested, partner);
                                if (WeddingsConfig.WEDDINGS_ANNOUNCE)
                                    announceWedding(requested, partner);

                                partner.sendMsg("С вашего счета был снят свадебный налог в размере "+ WeddingsConfig.WEDDINGS_TOLLS + " кредитов.");

                            } else {
                                partner.sendMsg("Не удалось выполнить запрос, попробуйте позднее");
                            }
                            return null;
                        }
                    });

                    return;
                }

                doWedding(player, partner);
                if (WeddingsConfig.WEDDINGS_GIFT_ENABLE) {
                    giveGifts(player, partner);
                }
                if (WeddingsConfig.WEDDINGS_ANNOUNCE) {
                    announceWedding(player, partner);
                }
            }
        }
    }

    private void doWedding(Player player, Player partner) {
        GDB.get(WeddingDAO.class).storeWedding(player, partner);
        player.setPartnerId(partner.getObjectId());
        partner.setPartnerId(player.getObjectId());
        player.sendMsg("Обряд прошел успешно.");
        partner.sendMsg("Обряд прошел успешно.");
        cleanWedding(player, partner);
    }

    public void unDoWedding(Player player, Player partner) {
        GDB.get(WeddingDAO.class).deleteWedding(player, partner);
        player.setPartnerId(0);
        partner.setPartnerId(0);
        player.sendMsg("Обряд был отменен.");
        partner.sendMsg("Обряд был отменен.");
    }

    private boolean checkConditions(Player player, Player partner) {
        if (player.isMarried() || partner.isMarried()) {
            player.sendMsg("Один из игроков уже находится в браке.");
            partner.sendMsg("Один из игроков уже находится в браке.");
        }
        if (WeddingsConfig.WEDDINGS_SUIT_ENABLE) {
            String[] suits = WeddingsConfig.WEDDINGS_SUITS.split(",");
            boolean success1 = false;
            boolean success2 = false;
            try {
                for (String suit : suits) {
                    int suitId = Integer.parseInt(suit);
                    if (!player.getEquipment().getEquippedItemsByItemId(suitId).isEmpty()) {
                        success1 = true;
                    }
                    if (!partner.getEquipment().getEquippedItemsByItemId(suitId).isEmpty()) {
                        success2 = true;
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } finally {
                if (!success1 || !success2) {
                    player.sendMsg("One of players not have required suit.");
                    partner.sendMsg("One of players not equip required suit.");
                    getPriest(player).sendMsg("One of players not equip required suit.");
                    return false;
                }
            }
        }

        if (player.getKnownList().getObject(partner.getObjectId()) == null) {
            player.sendMsg("Персонаж должен видеть своего избранника.");
            partner.sendMsg("Персонаж должен видеть своего избранника.");
            return false;
        }

        if (!player.havePermission(WeddingsConfig.WEDDINGS_MEMBERSHIP) || !partner.havePermission(WeddingsConfig.WEDDINGS_MEMBERSHIP)) {
            player.sendMsg("One of players not have required membership.");
            partner.sendMsg("One of players not have required membership.");
            getPriest(player).sendMsg("One of players not have required membership.");
            return false;
        }

        if (!WeddingsConfig.WEDDINGS_SAME_SEX && player.getCommonData().getGender().equals(partner.getCommonData().getGender())) {
            player.sendMsg("Попробуйте найти себе супруга противоположного пола.");
            partner.sendMsg("Попробуйте найти себе супруга противоположного пола.");
            return false;
        }

        if (!WeddingsConfig.WEDDINGS_DIFF_RACES && !player.getCommonData().getRace().equals(partner.getCommonData().getRace())) {
            player.sendMsg("Попробуйте найти себе супруга своей рассы.");
            partner.sendMsg("Попробуйте найти себе супруга своей рассы.");
            return false;
        }

        if (WeddingsConfig.WEDDINGS_KINAH != 0) {
            if (!player.getInventory().tryDecreaseKinah(WeddingsConfig.WEDDINGS_KINAH)
                || !partner.getInventory().tryDecreaseKinah(WeddingsConfig.WEDDINGS_KINAH)) {
                player.sendMsg("У одного из игроков недостаточно денег.");
                partner.sendMsg("У одного из игроков недостаточно денег.");
                return false;
            }
        }
        return true;
    }

    private void giveGifts(Player player, Player partner) {
        for (String pair : WeddingsConfig.WEDDINGS_GIFT.split(";")) {
            String[] ts = pair.split(",");
            int itemId = Integer.decode(ts[0]);
            int count = Integer.decode(ts[1]);
            ItemService.addItem(player, itemId, count);
            ItemService.addItem(partner, itemId, count);
        }
    }

    private void announceWedding(Player player, Player partner) {
        String message = player.getName() + " и " + partner.getName() + " вступили в брак.";
        Iterator<Player> iter = World.getInstance().getPlayersIterator();
        while (iter.hasNext()) {
            PacketSendUtility.sendBrightYellowMessage(iter.next(), message);
        }
    }

    public void cancelWedding(Player player) {
        player.sendMsg("Wedding canceled.");
        getPartner(player).sendMsg("Player " + player.getName() + " declined from a wedding.");
        getPriest(player).sendMsg("Player " + player.getName() + " declined from a wedding.");
        cleanWedding(player, getPartner(player));
    }

    private void cleanWedding(Player player, Player partner) {
        weddings.remove(player.getObjectId());
        weddings.remove(partner.getObjectId());
    }

    public Wedding getWedding(Player player) {
        return weddings.get(player.getObjectId());
    }

    private Player getPartner(Player player) {
        Wedding wedding = weddings.get(player.getObjectId());
        return wedding.getPartner();
    }

    private Player getPriest(Player player) {
        Wedding wedding = weddings.get(player.getObjectId());
        return wedding.getPriest();
    }

    @SuppressWarnings("synthetic-access")
    private static final class SingletonHolder {

        protected static final WeddingService instance = new WeddingService();
    }
}
