/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.ingameshop;

import com.ne.gs.database.GDB;
import gnu.trove.map.hash.THashMap;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.Callback;
import com.ne.gs.configs.ingameshop.InGameShopProperty;
import com.ne.gs.configs.main.AdvCustomConfig;
import com.ne.gs.configs.main.InGameShopConfig;
import com.ne.gs.configs.main.LoggingConfig;
import com.ne.gs.database.dao.InGameShopDAO;
import com.ne.gs.database.dao.InGameShopLogDAO;
import com.ne.gs.database.dao.PlayerDAO;
import com.ne.gs.model.gameobjects.LetterType;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.model.templates.mail.MailMessage;
import com.ne.gs.network.aion.serverpackets.SM_MAIL_SERVICE;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.network.aion.serverpackets.SM_TOLL_INFO;
import com.ne.gs.network.loginserver.LoginServer;
import com.ne.gs.network.loginserver.serverpackets.SM_PREMIUM_CONTROL;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.services.mail.SystemMailService;
import com.ne.gs.world.World;

/**
 * @author KID
 */
public class InGameShopEn {

    private static final InGameShopEn instance = new InGameShopEn();
    private final Logger log = LoggerFactory.getLogger("INGAMESHOP_LOG");
    private FastMap<Byte, List<IGItem>> items;
    private InGameShopDAO dao;
    private InGameShopProperty iGProperty;
    private int lastRequestId = 0;
    private FastList<IGRequest> activeRequests;
    private static final Map<Integer, Long> lastUsage = new FastMap<>();
    private final Map<Integer, List<TollQuerry>> callbacks = new THashMap<>();

    public static InGameShopEn getInstance() {
        return instance;
    }

    public InGameShopEn() {
        if (!InGameShopConfig.ENABLE_IN_GAME_SHOP) {
            log.info("InGameShop is disabled.");
            return;
        }
        iGProperty = InGameShopProperty.load();
        dao = GDB.get(InGameShopDAO.class);
        items = FastMap.newInstance();
        activeRequests = FastList.newInstance();
        items = dao.loadInGameShopItems();
        log.info("Loaded with " + items.size() + " items.");
    }

    public InGameShopProperty getIGSProperty() {
        return iGProperty;
    }

    public void reload() {
        if (!InGameShopConfig.ENABLE_IN_GAME_SHOP) {
            log.info("InGameShop is disabled.");
            return;
        }
        iGProperty.clear();
        iGProperty = InGameShopProperty.load();
        items = GDB.get(InGameShopDAO.class).loadInGameShopItems();
        log.info("Loaded with " + items.size() + " items.");
    }

    public IGItem getIGItem(int id) {
        for (byte key : items.keySet()) {
            for (IGItem item : items.get(key)) {
                if (item.getObjectId() == id) {
                    return item;
                }
            }
        }
        return null;
    }

    public Collection<IGItem> getItems(byte category) {
        if (!items.containsKey(category)) {
            return Collections.emptyList();
        }
        return items.get(category);
    }

    public FastList<Integer> getTopSales(int subCategory, byte category) {
        byte max = 6;
        TreeMap<Integer, Integer> map = new TreeMap<>(new DescFilter());
        if (!items.containsKey(category)) {
            return FastList.newInstance();
        }
        for (IGItem item : items.get(category)) {
            if (item.getSalesRanking() != 0 && (subCategory == 2 || item.getSubCategory() == subCategory)) {
                map.put(item.getSalesRanking(), item.getObjectId());
            }
        }
        FastList<Integer> top = FastList.newInstance();
        byte cnt = 0;
        for (Iterator<Integer> i = map.values().iterator(); i.hasNext(); ) {
            int objId = i.next();
            if (cnt > max) {
                break;
            }
            top.add(objId);
            cnt++;
        }

        map.clear();
        return top;
    }

    public int getMaxList(byte subCategoryId, byte category) {
        int id = 0;
        if (!items.containsKey(category)) {
            return id;
        }
        for (IGItem item : items.get(category)) {
            if (item.getSubCategory() == subCategoryId) {
                if (item.getList() > id) {
                    id = item.getList();
                }
            }
        }

        return id;
    }

    public void acceptRequest(Player player, int itemObjId) {
        if (player.getInventory().isFull()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_DICE_INVEN_ERROR);
            return;
        }

        IGItem item = getInstance().getIGItem(itemObjId);
        if (AdvCustomConfig.GAMESHOP_LIMIT) {
            if (item.getCategory() == AdvCustomConfig.GAMESHOP_CATEGORY) {
                if (lastUsage.containsKey(player.getObjectId())) {
                    if ((System.currentTimeMillis() - lastUsage.get(player.getObjectId())) < AdvCustomConfig.GAMESHOP_LIMIT_TIME * 60 * 1000) {
                        player.sendMsg("?????????????,??????????:"
                            + (int) ((AdvCustomConfig.GAMESHOP_LIMIT_TIME * 60 * 1000 - (System.currentTimeMillis() - lastUsage
                            .get(player
                                .getObjectId()))) / 1000) + " ?");
                        return;
                    }
                }
            }
        }
        lastRequestId++;
        IGRequest request = new IGRequest(lastRequestId, player.getObjectId(), itemObjId);
        request.accountId = player.getClientConnection().getAccount().getId();
        if (LoginServer.getInstance().sendPacket(new SM_PREMIUM_CONTROL(request, item.getItemPrice()))) {
            activeRequests.add(request);
        }
        if (AdvCustomConfig.GAMESHOP_LIMIT) {
            if (item.getCategory() == AdvCustomConfig.GAMESHOP_CATEGORY) {
                lastUsage.put(player.getObjectId(), System.currentTimeMillis());
            }
        }
        if (LoggingConfig.LOG_INGAMESHOP) {
            log.info("[INGAMESHOP] > Account name: " + player.getAcountName() + ", PlayerName: " + player.getName() + " is watching item:" + item
                .getItemId()
                + " cost " + item.getItemPrice() + " toll.");
        }
    }

    public void sendRequest(Player player, String receiver, String message, int itemObjId) {
        if (receiver.equalsIgnoreCase(player.getName())) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_INGAMESHOP_CANNOT_GIVE_TO_ME);
            return;
        }

        if (!InGameShopConfig.ALLOW_GIFTS) {
            player.sendMsg("Gifts are disabled.");
            return;
        }

        if (!GDB.get(PlayerDAO.class).isNameUsed(receiver)) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_INGAMESHOP_NO_USER_TO_GIFT);
            return;
        }

        PlayerCommonData recipientCommonData = GDB.get(PlayerDAO.class).loadPlayerCommonDataByName(receiver);
        if (recipientCommonData.getMailboxLetters() >= 100) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_MAIL_MSG_RECIPIENT_MAILBOX_FULL(recipientCommonData.getName()));
            return;
        }

        if (!InGameShopConfig.ENABLE_GIFT_OTHER_RACE && !player.isGM()) {
            if (player.getRace() != recipientCommonData.getRace()) {
                player.sendPck(new SM_MAIL_SERVICE(MailMessage.MAIL_IS_ONE_RACE_ONLY));
                return;
            }
        }

        IGItem item = getIGItem(itemObjId);
        lastRequestId++;
        IGRequest request = new IGRequest(lastRequestId, player.getObjectId(), receiver, message, itemObjId);
        request.accountId = player.getClientConnection().getAccount().getId();
        if (LoginServer.getInstance().sendPacket(new SM_PREMIUM_CONTROL(request, item.getItemPrice()))) {
            activeRequests.add(request);
        }
    }

    public void addToll(Player player, long cnt) {
        if (InGameShopConfig.ENABLE_IN_GAME_SHOP) {
            lastRequestId++;
            IGRequest request = new IGRequest(lastRequestId, player.getObjectId(), 0);
            request.accountId = player.getClientConnection().getAccount().getId();
            if (LoginServer.getInstance().sendPacket(new SM_PREMIUM_CONTROL(request, cnt * -1))) {
                activeRequests.add(request);
            }
        } else {
            player.sendMsg("You can't add toll if ingameshop is disabled!");
        }
    }


    public void querryToll(Player player, TollQuerry callback) {
        if (InGameShopConfig.ENABLE_IN_GAME_SHOP) {
            IGRequest request = new IGRequest(lastRequestId, player.getObjectId(), 0);
            request.accountId = player.getClientConnection().getAccount().getId();
            if (LoginServer.getInstance().sendPacket(new SM_PREMIUM_CONTROL(request, Long.MIN_VALUE))) {
                activeRequests.add(request);
                synchronized (callbacks) {
                    List<TollQuerry> cs = callbacks.get(player.getObjectId());
                    if (cs == null) {
                        cs = new ArrayList<>(1);
                        callbacks.put(player.getObjectId(), cs);
                    }

                    cs.add(callback);
                }
            }
        }
    }

    public void finishRequest(int requestId, int result, long toll) {
        for (IGRequest request : activeRequests) {
            if (request.requestId == requestId) {
                Player player = World.getInstance().findPlayer(request.playerId);
                if (player != null) {
                    if (result == 1) {
                        player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_INGAMESHOP_ERROR);
                    } else if (result == 2) {
                        IGItem item = getIGItem(request.itemObjId);
                        if (item == null) {
                            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_INGAMESHOP_ERROR);
                            log.error("player " + player.getName() + " requested " + request.itemObjId + " that was not exists in list.");
                            return;
                        }
                        player.sendPck(SM_SYSTEM_MESSAGE.STR_INGAMESHOP_NOT_ENOUGH_CASH("Toll"));
                        player.sendPck(new SM_TOLL_INFO(toll));
                        if (LoggingConfig.LOG_INGAMESHOP) {
                            log.info("[INGAMESHOP] > Account name: " + player.getAcountName() + ", PlayerName: " + player
                                .getName() + " has not bought item: "
                                + item.getItemId() + " count: " + item.getItemCount() + " Cause: NOT ENOUGH TOLLS");
                        }
                    } else if (result == 3) {
                        IGItem item = getIGItem(request.itemObjId);
                        if (item == null) {
                            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_INGAMESHOP_ERROR);
                            log.error("player " + player.getName() + " requested " + request.itemObjId + " that was not exists in list.");
                            return;
                        }

                        if (request.gift) {
                            SystemMailService.getInstance()
                                             .sendMail(player.getName(), request.receiver, "In Game Shop", request.message, item
                                                 .getItemId(),
                                                 item.getItemCount(), 0L, LetterType.BLACKCLOUD);
                            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_INGAMESHOP_GIFT_SUCCESS);
                            if (LoggingConfig.LOG_INGAMESHOP) 
                            	log.info("[INGAMESHOP] > Account name: "+player.getAcountName()+", PlayerName: "+player.getName() + " BUY ITEM: "+item.getItemId()+" COUNT: "+ item.getItemCount()+" FOR PlayerName: "+ request.receiver);
                            if (LoggingConfig.LOG_INGAMESHOP_SQL)
								GDB.get(InGameShopLogDAO.class).log("GIFT", new Timestamp(System.currentTimeMillis()), player.getName(), player.getAcountName(), request.receiver, item.getItemId(), item.getItemCount(), item.getItemPrice());
                        } else {
                            ItemService.addItem(player, item.getItemId(), item.getItemCount());
                            if (LoggingConfig.LOG_INGAMESHOP) 
                                log.info("[INGAMESHOP] > Account name: " + player.getAcountName() + ", PlayerName: " + player.getName() + " BUY ITEM: "+ item.getItemId() + " COUNT: " + item.getItemCount());
                            if (LoggingConfig.LOG_INGAMESHOP_SQL)
								GDB.get(InGameShopLogDAO.class).log("BUY", new Timestamp(System.currentTimeMillis()), player.getName(), player.getAcountName(), player.getName(), item.getItemId(), item.getItemCount(), item.getItemPrice());
                        }

                        item.increaseSales();
                        dao.increaseSales(item.getObjectId(), item.getSalesRanking());
                        player.sendPck(new SM_TOLL_INFO(toll));
                    } else if (result == 4) {
                        player.sendPck(new SM_TOLL_INFO(toll));
                    } else if (result == 5) {
                        player.sendPck(new SM_TOLL_INFO(toll));
                        synchronized (callbacks) {
                            List<TollQuerry> cs = callbacks.remove(player.getObjectId());
                            if (cs != null) {
                                for (TollQuerry c : cs) {
                                    c.onEvent(new TollQuerryResult(toll));
                                }
                            }
                        }
                    }
                }

                activeRequests.remove(request);
                break;
            }
        }
    }

    class DescFilter implements Comparator<Object> {

        DescFilter() {
        }

        @Override
        public int compare(Object o1, Object o2) {
            Integer i1 = (Integer) o1;
            Integer i2 = (Integer) o2;
            return -i1.compareTo(i2);
        }
    }

    public interface TollQuerry extends Callback<TollQuerryResult, Object> {}

    public static final class TollQuerryResult {
        public final long toll;

        public TollQuerryResult(long toll) {
            this.toll = toll;
        }
    }
}
