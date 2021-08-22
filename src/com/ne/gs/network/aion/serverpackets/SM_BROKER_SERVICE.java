/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.sql.Timestamp;
import java.util.Calendar;

import com.ne.gs.model.gameobjects.BrokerItem;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.iteminfo.ItemInfoBlob;
import com.ne.gs.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * @author IlBuono, kosyachok
 */
public class SM_BROKER_SERVICE extends AionServerPacket {

    private enum BrokerPacketType {
        SEARCHED_ITEMS(0),
        REGISTERED_ITEMS(1),
        REGISTER_ITEM(3),
        SHOW_SETTLED_ICON(5),
        SETTLED_ITEMS(5),
        REMOVE_SETTLED_ICON(6);

        private final int id;

        private BrokerPacketType(int id) {
            this.id = id;
        }

        private int getId() {
            return id;
        }
    }

    private final BrokerPacketType type;
    private BrokerItem[] brokerItems;
    private int itemsCount;
    private int startPage;
    private int message;
    private long settled_kinah;

    public SM_BROKER_SERVICE(BrokerItem brokerItem, int message) {
        type = BrokerPacketType.REGISTER_ITEM;
        brokerItems = new BrokerItem[]{brokerItem};
        this.message = message;
    }

    public SM_BROKER_SERVICE(int message) {
        type = BrokerPacketType.REGISTER_ITEM;
        this.message = message;
    }

    public SM_BROKER_SERVICE(BrokerItem[] brokerItems) {
        type = BrokerPacketType.REGISTERED_ITEMS;
        this.brokerItems = brokerItems;
    }

    public SM_BROKER_SERVICE(BrokerItem[] brokerItems, long settled_kinah) {
        type = BrokerPacketType.SETTLED_ITEMS;
        this.brokerItems = brokerItems;
        this.settled_kinah = settled_kinah;
    }

    public SM_BROKER_SERVICE(BrokerItem[] brokerItems, int itemsCount, int startPage) {
        type = BrokerPacketType.SEARCHED_ITEMS;
        this.brokerItems = brokerItems;
        this.itemsCount = itemsCount;
        this.startPage = startPage;
    }

    public SM_BROKER_SERVICE(boolean showSettledIcon, long settled_kinah) {
        type = showSettledIcon ? BrokerPacketType.SHOW_SETTLED_ICON : BrokerPacketType.REMOVE_SETTLED_ICON;
        this.settled_kinah = settled_kinah;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        switch (type) {
            case SEARCHED_ITEMS:
                writeSearchedItems();
                break;
            case REGISTERED_ITEMS:
                writeRegisteredItems();
                break;
            case REGISTER_ITEM:
                writeRegisterItem();
                break;
            case SHOW_SETTLED_ICON:
                writeShowSettledIcon();
                break;
            case REMOVE_SETTLED_ICON:
                writeRemoveSettledIcon();
                break;
            case SETTLED_ITEMS:
                writeShowSettledItems();
                break;
        }

    }

    private void writeSearchedItems() {
        writeC(type.getId());
        writeD(itemsCount);
        writeC(0);
        writeH(startPage);
        writeH(brokerItems.length);
        for (BrokerItem item : brokerItems) {
            writeItemInfo(item);
        }
    }

    private void writeRegisteredItems() {
        writeC(type.getId());
        writeD(0x00);
        writeH(brokerItems.length); // you can register a max of 15 items, so 0x0F
        for (BrokerItem brokerItem : brokerItems) {
            writeRegisteredItemInfo(brokerItem);
        }
    }

    private void writeRegisterItem() {
        writeC(type.getId());
        writeH(message);
        if (message == 0) {
            BrokerItem itemForRegistration = brokerItems[0];
            writeRegisteredItemInfo(itemForRegistration);
        }
    }

    private void writeShowSettledIcon() {
        writeC(type.getId());
        writeQ(settled_kinah);
        writeD(0x00);
        writeH(0x00);
        writeH(0x01);
        writeC(0x00);
    }

    private void writeRemoveSettledIcon() {
        writeH(type.getId());
    }

    private final static byte[] NULL_BLOCK = new byte[48];

    private void writeShowSettledItems() {
        writeC(type.getId());
        writeQ(settled_kinah);

        writeH(brokerItems.length);
        writeD(0x00);
        writeC(0x00);

        writeH(brokerItems.length);
        for (BrokerItem settledItem : brokerItems) {
            writeD(settledItem.getItemId());
            if (settledItem.isSold()) {
                writeQ(settledItem.getPrice());
            } else {
                writeQ(0);
            }
            writeQ(settledItem.getItemCount());
            writeQ(settledItem.getItemCount());
            writeD((int) settledItem.getSettleTime().getTime() / 60000);

            // TODO! thats really odd - looks like getItem() may return null...
            Item item = settledItem.getItem();
            if (item != null) {
                ItemInfoBlob.newBlobEntry(ItemBlobType.MANA_SOCKETS, null, item).writeThisBlob(getBuf());
            } else {
                writeB(NULL_BLOCK);
            }

            writeS(settledItem.getItemCreator()); // TODO! i'm not sure
        }
    }

    private void writeRegisteredItemInfo(BrokerItem brokerItem) {
        Item item = brokerItem.getItem();

        writeD(brokerItem.getItemUniqueId());
        writeD(brokerItem.getItemId());
        writeQ(brokerItem.getPrice());
        writeQ(item.getItemCount());
        writeQ(item.getItemCount());
        Timestamp currentTime = new Timestamp(Calendar.getInstance().getTimeInMillis());
        int daysLeft = (int) ((brokerItem.getExpireTime().getTime() - currentTime.getTime()) / 86400000);
        writeC(daysLeft);

        ItemInfoBlob.newBlobEntry(ItemBlobType.MANA_SOCKETS, null, item).writeThisBlob(getBuf());

        writeS(brokerItem.getItemCreator()); // TODO! i'm not sure
    }

    private void writeItemInfo(BrokerItem brokerItem) {
        Item item = brokerItem.getItem();

        writeD(item.getObjectId());
        writeD(item.getItemTemplate().getTemplateId());
        writeQ(brokerItem.getPrice());
        writeQ(item.getItemCount());

        ItemInfoBlob.newBlobEntry(ItemBlobType.MANA_SOCKETS, null, item).writeThisBlob(getBuf());

        writeS(brokerItem.getSeller());
        writeS(brokerItem.getItemCreator()); // creator
    }
}
