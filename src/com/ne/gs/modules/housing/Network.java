/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.housing;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.annotations.Nullable;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

import static com.ne.gs.modules.housing.Housing.*;

/**
 * @author hex1r0
 */
public final class Network {

    private Network() {
    }

    static void sendHouseInfo(Player player, House house) {
        player.sendPck(new SpHouseInfo(house));
        if (house.equals(housing().getHome(player.getObjectId()))) {
            sendOwnerInfo(player, house);
        }

        for (HouseItem i : ItemList.of(house.getOwnerUid()).getInstalled()) {
            player.sendPck(new SpHouseItemInfo(ItemInfo.of(house, i)));
        }
    }

    static void sendOwnerInfo(Player player, House house) {
        player.sendPck(new SpHouseOwnerInfo(house));
    }

    //    static void broadcast(final PlayerCommonData pcd) {
    //        final Player player = pcd.getPlayer();
    //        if (player != null) {
    //            player.getKnownList().clear();
    //            player.getKnownList().doUpdate();
    //        }
    //    }

    static void broadcast(@Nullable Player player, @NotNull House.HouseTemplate tpl) {
        if (tpl.getType() == House.HouseType.FLAT) {
            House home;
            if (player != null) {
                home = housing().getHome(player.getObjectId());
                if (home != null) {
                    sendOwnerInfo(player, home);
                }
            }
            return;
        }

        if (player != null) {
            sendHouseInfo(player, housing().getHouseByTpl(tpl));
        }

        House.Trigger tr = housing().getTrigger(tpl);
        tr.getKnownList().clear();
        tr.getKnownList().doUpdate();
    }

    static void sendHouseInventory(Player player, House home) {
        player.sendPck(new SpHouseInventory(home, false));
        player.sendPck(new SpHouseInventory(home, true));
    }

    // ------------------------------------------------------------------------
    // -- server packets
    // ------------------------------------------------------------------------

    public static class SpHouseInfo extends AionServerPacket {

        private final int _doorId;
        private final int _houseTplUid;
        private final int _exteriorId;
        private final int _ownerUid;
        private final int _buildingType = 0; // FIXME
        private final byte UNK_1 = 0;
        private final int _housingFlags = 0; // FIXME
        private final int _accessId;
        private final int _signId;
        private final int[] _appearance; // should be Appearance.values().length
        private final int UNK_2 = 0;
        private final int UNK_3 = 0;
        private final int _flagId;
        private final String _ownerName;

        public SpHouseInfo(House house) {
            House.HouseTemplate tpl = house.getTemplate();
            _doorId = tpl.getDoorId();
            _houseTplUid = tpl.getHouseId();
            _exteriorId = house.getExteriorId();
            _ownerUid = house.getOwnerUid();
            // ...buildingType , UNK_1 , housingFlags
            _accessId = house.getAccess().id();
            _signId = house.getSignOpt().id();
            _appearance = house.getAppearance().clone();
            _flagId = house.getDoorFlag().id();
            if (house.getOwnerUid() > 0) {
                PlayerCommonData pcd = PlayerCommonData.get(house.getOwnerUid());
                _ownerName = pcd.getName();
            } else {
                _ownerName = "";
            }
        }

        @Override
        protected void writeImpl(AionConnection con) {
            writeInfo();
        }

        protected void writeInfo() {
            writeD(_doorId);
            writeD(_houseTplUid);
            writeD(_exteriorId);
            writeD(_ownerUid);
            writeD(_buildingType);

            writeC(UNK_1);
            writeC(_housingFlags);

            writeC(_accessId);
            writeC(_signId);

            for (int v : _appearance) {
                writeD(v);
            }

            writeD(UNK_2);
            writeD(UNK_3);

            writeC(_flagId);

            writeS(_ownerName, 182);
        }
    }

    public static final class SpHouseAcquire extends AionServerPacket {

        private final int _playerId;
        private final int _houseId;
        private final int _acquire;

        public SpHouseAcquire(int playerId, int houseId, boolean acquire) {
            _playerId = playerId;
            _houseId = houseId;
            _acquire = acquire ? 1 : 0;
        }

        @Override
        protected void writeImpl(AionConnection con) {
            writeD(_playerId);
            writeD(_houseId);
            writeD(_acquire);
        }
    }

    public static final class SpHouseOwnerInfo extends AionServerPacket {

        private final int _houseId;
        private final int _exteriorId;
        private final byte _housingFlags = 0; // FIXME

        public SpHouseOwnerInfo(House house) {
            _houseId = house.getHouseId();
            _exteriorId = house.getExteriorId();
        }

        @Override
        protected void writeImpl(AionConnection con) {
            writeD(_houseId);
            writeD(_exteriorId);
            writeC(_housingFlags); // housing flags ??
            writeC(1);
            writeC(0);
            writeD(0);
            writeD(0);
            writeD(0);
            writeH(0);
        }
    }

    public static final class SpHouseInfoUpdate extends SpHouseInfo {

        public SpHouseInfoUpdate(House house) {
            super(house);
        }

        @Override
        protected void writeImpl(AionConnection con) {
            writeH(1);
            writeH(0);
            writeH(1);

            writeInfo();
        }
    }

    public static class SpDeleteHouse extends AionServerPacket {

        private final int _houseId;

        public SpDeleteHouse(int houseId) {
            _houseId = houseId;
        }

        @Override
        protected void writeImpl(AionConnection con) {
            writeD(_houseId);
        }
    }

    public static final class SpEditorReply extends AionServerPacket {

        private final Blob _blob;

        public SpEditorReply(Blob blob) {
            _blob = blob;
        }

        @Override
        protected void writeImpl(AionConnection con) {
            _blob.write(getBuf());
        }
    }

    static final class SpUseHouseItem extends AionServerPacket {

        private final Blob _blob;

        public SpUseHouseItem(Blob blob) {
            _blob = blob;
        }

        @Override
        protected void writeImpl(AionConnection con) {
            _blob.write(getBuf());
        }

        static class Info extends Blob.CompositeBlob {

            public Info(byte itemTypeId, int itemUserUid, int itemUid) {
                super(itemTypeId, itemUserUid, (byte) 1, itemUid);
            }
        }

        static class ExtInfo extends Blob.CompositeBlob {

            public ExtInfo(byte itemTypeId, int itemUserUid, int itemOwnerUid, int itemUid,
                           int usages, byte checkType) {
                super(itemTypeId, itemUserUid, itemOwnerUid, itemUid, usages, checkType);
            }
        }
    }

    static final class UsableInfo extends Blob.CompositeBlob {

        UsableInfo(int useCount, byte checkType) {
            super(useCount, checkType);
        }
    }

    static final class NpcInfo extends Blob.CompositeBlob {

        NpcInfo(int npcId) {
            super(npcId);
        }
    }

    static final class ItemInfo extends Blob.CompositeBlob {

        ItemInfo(int doorId, int playerUid, int itemUid, int itemId,
                 float x, float y, float z, short h, int cooldown,
                 int expire, byte typeId, Blob extInfo) {
            // NOTE: itemUid is specified twice
            super(doorId, playerUid, itemUid, itemUid, itemId, x, y, z, h, cooldown, expire, typeId, extInfo);
        }

        static ItemInfo of(House house, HouseItem item) {
            House.HouseTemplate htpl = house.getTemplate();
            HouseItemTemplate tpl = item.getTemplate();

            Blob info = null;
            if (tpl.type == HouseItem.Type.USABLE) {
                info = new UsableInfo(item.getUsages(), (byte) 1); // FIXME checkType
            } else if (item instanceof HouseItem.HouseNpc) {
                info = new NpcInfo(((HouseItem.HouseNpc) item).getNpcUid());
            }

            return new ItemInfo(htpl.getMapId() + htpl.getDoorId(), house.getOwnerUid(), item.getObjectId(),
                item.getItemId(), item.getX(), item.getY(), item.getZ(), item.getH(),
                item.readyIn(), item.expiresIn(), (byte) tpl.type.getId(), info);
        }
    }

    static abstract class Blob {

        private ByteBuffer _buf;

        public final void write(ByteBuffer buf) {
            _buf = buf;
            build();
        }

        public final ByteBuffer getBuf() {
            return _buf;
        }

        protected abstract void build();

        protected final void writeD(int value) {
            _buf.putInt(value);
        }

        protected final void writeH(int value) {
            _buf.putShort((short) value);
        }

        protected final void writeC(int value) {
            _buf.put((byte) value);
        }

        protected final void writeDF(double value) {
            _buf.putDouble(value);
        }

        protected final void writeF(float value) {
            _buf.putFloat(value);
        }

        protected final void writeQ(long value) {
            _buf.putLong(value);
        }

        protected final void writeS(String text) {
            if (text == null) {
                _buf.putChar('\000');
            } else {
                int len = text.length();
                for (int i = 0; i < len; i++) {
                    _buf.putChar(text.charAt(i));
                }
                _buf.putChar('\000');
            }
        }

        protected final void writeB(byte[] data) {
            _buf.put(data);
        }

        protected final void skip(int bytes) {
            for (int i = 0; i < bytes; i++) {
                _buf.put((byte) 0);
            }
        }

        public static CompositeBlob of(Object first, Object... next) {
            return new CompositeBlob(first, next);
        }

        public static class CompositeBlob extends Blob {

            private final List<Object> _data;

            public CompositeBlob(@NotNull Object first, @Nullable Object... next) {
                List<Object> data = new ArrayList<>();
                data.add(first);

                if (next != null) {
                    for (Object o : next) {
                        if (o instanceof Iterable) {
                            Iterables.addAll(data, (Iterable<?>) o);
                        } else {
                            data.add(o);
                        }
                    }
                }

                _data = data;
            }

            @Override
            protected final void build() {
                for (Object o : _data) {
                    if (o == null) {
                        continue;
                    }

                    Class<?> type = o.getClass();
                    if (type == Integer.TYPE || type == Integer.class) {
                        writeD((Integer) o);
                    } else if (type == Short.TYPE || type == Short.class) {
                        writeH((Short) o);
                    } else if (type == Byte.TYPE || type == Byte.class) {
                        writeC((Byte) o);
                    } else if (type == Long.TYPE || type == Long.class) {
                        writeQ((Long) o);
                    } else if (type == Float.TYPE || type == Float.class) {
                        writeF((Float) o);
                    } else if (type == Double.TYPE || type == Double.class) {
                        writeDF((Double) o);
                    } else if (type == String.class) {
                        writeS((String) o);
                    } else if (o instanceof Blob) {
                        ((Blob) o).write(getBuf());
                    }
                }
            }
        }
    }

    static class EditorAction extends Blob.CompositeBlob {

        EditorAction(CpCustomizeHouse.Action action) {
            super((byte) action.getId());
        }
    }

    //    static class RefreshItem extends Blob {
    //        private final int storeId;
    //        private final int itemUid;
    //
    //        RefreshItem(final int storeId, final int itemUid) {
    //            this.storeId = storeId;
    //            this.itemUid = itemUid;
    //        }
    //
    //        @Override
    //        protected void build() {
    //            writeC(4);
    //            writeC(1); // FIXME
    //            writeD(itemUid);
    //        }
    //    }

    public static final class SpHouseItemInfo extends AionServerPacket {

        private final Blob _blob;

        public SpHouseItemInfo(Blob blob) {
            _blob = blob;
        }

        @Override
        protected void writeImpl(AionConnection con) {
            _blob.write(getBuf());
        }
    }

    public static class SpHouseInventory extends AionServerPacket {

        private final Blob _data;

        public SpHouseInventory(House house, boolean appearance) {
            List<Blob> blobs = createBlobs(house, appearance);
            _data = Blob.of((byte) (appearance ? 2 : 1), (short) blobs.size(), blobs);
        }

        @Override
        protected void writeImpl(AionConnection con) {
            _data.write(getBuf());
        }

        private static final Blob DEFAULT_WALL = Blob.of(0, Housing.DEFAULT_WALL.getItemId());
        private static final Blob DEFAULT_FLOOR = Blob.of(0, Housing.DEFAULT_FLOOR.getItemId());

        private List<Blob> createBlobs(House house, boolean appearance) {
            List<Blob> blobs = new ArrayList<>();
            if (appearance) {
                blobs.add(DEFAULT_WALL);
                blobs.add(DEFAULT_FLOOR);
                for (HouseItem item : ItemList.of(house.getOwnerUid()).getAppearance()) {
                    blobs.add(Blob.of(item.getObjectId(), item.getItemId()));
                }
            } else {
                for (HouseItem item : ItemList.of(house.getOwnerUid()).getInstallable()) {
                    HouseItemTemplate tpl = item.getTemplate();

                    blobs.add(new Info(item.getObjectId(), tpl.id, item.readyIn(), item.expiresIn(), (byte) tpl.type.getId(),
                        tpl.type == HouseItem.Type.USABLE ? new UsableInfo(item.getUsages(), (byte) 1) : null));
                }
            }

            return blobs;
        }

        private static class Info extends Blob.CompositeBlob {

            public Info(int itemUid, int tplId, int cooldown, int expire, byte typeId,
                        Blob etc) {
                super(itemUid, tplId, cooldown, expire, typeId, etc);
            }
        }
    }

    public static class SpDeleteHouseItem extends AionServerPacket {

        private final int _itemUid;

        public SpDeleteHouseItem(int itemUid) {
            _itemUid = itemUid;
        }

        @Override
        protected void writeImpl(AionConnection con) {
            writeD(_itemUid);
        }
    }

    // ------------------------------------------------------------------------
    // -- client packets
    // ------------------------------------------------------------------------

    public static final class CpKickVisitors extends AionClientPacket {

        private int _mode;

        @Override
        protected void readImpl() {
            _mode = readC();
            readH();
        }

        @Override
        protected void runImpl() {
            Player player = getConnection().getActivePlayer();

            Housing.housing().tell(new KickVisitors(player, Mode.valueOf(_mode)));
        }

        static enum Mode {
            ALL_BUT_FRIENDS(1),
            ALL(2);

            private final int _id;

            private Mode(int id) {
                _id = id;
            }

            public int getId() {
                return _id;
            }

            public static Mode valueOf(int id) {
                for (Mode m : Mode.values()) {
                    if (m.getId() == id) {
                        return m;
                    }
                }

                return ALL;
            }
        }
    }

    public static class CpChangeHouseSettings extends AionClientPacket {

        private int _access;
        private int _signOpt;
        private String _signText;

        @Override
        protected void readImpl() {
            _access = readC();
            _signOpt = readC();
            _signText = readS();
        }

        @Override
        protected void runImpl() {
            Player p = getConnection().getActivePlayer();
            if (p != null) {
                housing().tell(new Housing.ChangeHouseSettings(p, _access, _signOpt, _signText));
            }
        }
    }

    public static final class CpCustomizeHouse extends AionClientPacket {

        private Action _action;
        private int _id;
        private float _x;
        private float _y;
        private float _z;
        private short _h;

        @Override
        protected void readImpl() {
            int actionId = readC();
            _action = Action.valueOf(actionId);

            switch (_action) {
                case REGISTER_ITEM:
                case REMOVE:
                case RECYCLE_ITEM:
                case CHANGE_EXTERIOR:
                    _id = readD();
                    break;

                case INSTALL_ITEM:
                case MOVE_ITEM:
                    _id = readD();
                    _x = readF();
                    _y = readF();
                    _z = readF();
                    _h = (short) readH();
                    break;
            }
        }

        @Override
        protected void runImpl() {
            Player p = getConnection().getActivePlayer();
            if (p != null) {
                housing().tell(new Housing.CustomizeHouse(p, _action, _id, _x, _y, _z, _h));
            }
        }

        static enum Action {
            OPEN_EDITOR(1),
            // enter decoration mode
            CLOSE_EDITOR(2),
            // leave decoration mode
            REGISTER_ITEM(3),
            // register item in house inventory
            REMOVE(4),
            INSTALL_ITEM(5),
            // install item
            MOVE_ITEM(6),
            // move item around / rotate
            RECYCLE_ITEM(7),
            // recycle item to house inventory
            OPEN_EXTERIOR_MENU(14),
            CLOSE_EXTERIOR_MENU(15),
            CHANGE_EXTERIOR(16);    // change house exterior

            private final int _id;

            private Action(int id) {
                _id = id;
            }

            int getId() {
                return _id;
            }

            static Action valueOf(int id) {
                for (Action m : Action.values()) {
                    if (m.getId() == id) {
                        return m;
                    }
                }

                return CLOSE_EDITOR;
            }
        }
    }

    public static class CpCustomizeAppearance extends AionClientPacket {

        private int _itemUid;
        private int _itemId;
        private int _lineNr;

        @Override
        protected void readImpl() {
            _itemUid = readD();
            _itemId = readD();
            _lineNr = readH();
        }

        @Override
        protected void runImpl() {
            Player player = getConnection().getActivePlayer();
            if (player == null) {
                return;
            }

            housing().tell(new Housing.CustomizeAppearance(player, _itemUid, _itemId, _lineNr));
        }
    }

    public static final class CpUseHouseDoor extends AionClientPacket {

        private int _encodedDoorId;
        private boolean _leave;

        @Override
        protected void readImpl() {
            _encodedDoorId = readD();
            _leave = readC() != 0;
        }

        @Override
        protected void runImpl() {
            Player player = getConnection().getActivePlayer();
            if (player == null) {
                return;
            }

            int worldId = _encodedDoorId / 10000 * 10000;
            int doorId = _encodedDoorId - worldId;

            if (worldId != player.getWorldId()) {
                return; // hack
            }

            housing().tell(new Housing.UseDoor(player, doorId, _leave));
        }
    }

    public static final class CpUseHouseItem extends AionClientPacket {

        private int _itemUid;

        @Override
        protected void readImpl() {
            _itemUid = readD();
        }

        @Override
        protected void runImpl() {
            Player player = getConnection().getActivePlayer();
            if (player == null) {
                return;
            }

            housing().tell(new Housing.UseInstalledItem(player, _itemUid));
        }
    }

    public static final class CpVisitHouse extends AionClientPacket {

        public static final int TO_HOME = 1;
        public static final int VISIT_FRIEND = 2;
        public static final int THREE = 3;

        private int _actionId;
        /*private int _requesterUid;*/
        private int _targetUid;

        @Override
        protected void readImpl() {
            _actionId = readC();
            /*_requesterUid =*/
            readD();
            _targetUid = readD();
        }

        @Override
        protected void runImpl() {
            Player requester = getConnection().getActivePlayer();

            if (requester == null || !requester.isOnline()) {
                return;
            }

            Housing.housing().tell(new VisitHouse(requester, _targetUid, _actionId));
        }
    }


    // ------------------------------------------------------------------------
    // -- auction server packets
    // ------------------------------------------------------------------------

    public static final class SpAuctionLotInfo extends AionServerPacket {

        private final boolean _isFirst;
        private final boolean _isLast;

        private final BidInfo _playerBid;
        private final BidInfo _playerSell;

        private final List<LotInfo> _lotInfos;

        public SpAuctionLotInfo(boolean isFirstPacket,
                                boolean isLastPacket,
                                List<HouseAuction.HouseLot> lots,
                                HouseAuction.HouseLot playerBidLot,
                                HouseAuction.HouseLot playerPutToSaleLot) {
            _isFirst = isFirstPacket;
            _isLast = isLastPacket;

            if (playerBidLot != null) {
                _playerBid = new BidInfo(playerBidLot.getSequenceNumber(), playerBidLot.getCurrentPrice());
            } else {
                _playerBid = BidInfo.EMPTY;
            }

            if (playerPutToSaleLot != null) {
                _playerSell = new BidInfo(playerPutToSaleLot.getSequenceNumber(), playerPutToSaleLot.getCurrentPrice());
            } else {
                _playerSell = BidInfo.EMPTY;
            }

            _lotInfos = ImmutableList.copyOf(Lists.transform(lots, new Function<HouseAuction.HouseLot, LotInfo>() {
                @Override
                public LotInfo apply(HouseAuction.HouseLot lot) {
                    House.HouseTemplate tpl = lot.getHouseTpl();
                    return new LotInfo(
                        lot.getSequenceNumber(),
                        tpl.getRegionTemplate().getUid(),
                        tpl.getHouseId(),
                        tpl.getExteriorTemplates().get(0).getId(),
                        tpl.getType().id(),
                        lot.getCurrentPrice(),
                        lot.getBidCount(),
                        auction().leftToProcessSeconds()
                    );
                }
            }));
        }

        @Override
        protected void writeImpl(AionConnection con) {
            writeC(_isFirst ? 1 : 0);
            writeC(_isLast ? 1 : 0);

            writeD(_playerBid.sequenceNumber);
            writeQ(_playerBid.currentPrice);

            writeD(_playerSell.sequenceNumber);
            writeQ(_playerSell.currentPrice);

            writeH(_lotInfos.size());
            for (LotInfo i : _lotInfos) {
                writeD(i.sequenceNumber);
                writeD(i.regionId);
                writeD(i.houseId);
                writeD(i.viewId);
                writeD(i.houseTypeId);
                writeQ(i.currentPrice);
                writeQ(i.unk); // unk
                writeD(i.bidCount);
                writeD(i.remainingSeconds);
            }
        }

        private static class LotInfo {

            final int sequenceNumber;
            final int regionId;
            final int houseId;
            final int viewId;
            final int houseTypeId;
            final long currentPrice;
            final long unk = 100000L;
            final int bidCount;
            final int remainingSeconds;

            private LotInfo(int sequenceNumber,
                            int regionId,
                            int houseId,
                            int viewId,
                            int houseTypeId,
                            long currentPrice,
                            int bidCount,
                            int remainingSeconds) {
                this.sequenceNumber = sequenceNumber;
                this.regionId = regionId;
                this.houseId = houseId;
                this.viewId = viewId;
                this.houseTypeId = houseTypeId;
                this.currentPrice = currentPrice;
                this.bidCount = bidCount;
                this.remainingSeconds = remainingSeconds;
            }
        }

        private static class BidInfo {

            static final BidInfo EMPTY = new BidInfo(0, 0);

            final int sequenceNumber;
            final long currentPrice;

            private BidInfo(int sequenceNumber, long currentPrice) {
                this.sequenceNumber = sequenceNumber;
                this.currentPrice = currentPrice;
            }
        }
    }


    // ------------------------------------------------------------------------
    // -- auction client packets
    // ------------------------------------------------------------------------

    public static final class CpPlaceBid extends AionClientPacket {

        private int _sequenceNumber;
        private long _price;

        @Override
        protected void readImpl() {
            _sequenceNumber = readD();
            _price = readQ();
        }

        @Override
        protected void runImpl() {
            Player player = getConnection().getActivePlayer();
            if (player == null) {
                return;
            }

            housing().tell(new HouseAuction.PlaceBid(player, _sequenceNumber, _price));
        }
    }

    public static class CpGetLots extends AionClientPacket {

        @Override
        protected void readImpl() {
        }

        @Override
        protected void runImpl() {
            Player p = getConnection().getActivePlayer();
            if (p != null) {
                housing().tell(new HouseAuction.SendLots(p));
            }
        }
    }

    public static class CpPutForSale extends AionClientPacket {

        private long _price;
        //private long _unk1;

        @Override
        protected void readImpl() {
            _price = readQ();
            /*_unk1 = */
            readQ();
        }

        @Override
        protected void runImpl() {
            Player p = getConnection().getActivePlayer();
            if (p != null) {
                housing().tell(new HouseAuction.PutForSaleByPlayer(p, _price));
            }
        }
    }
}
