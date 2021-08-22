/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.housing;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

import com.ne.commons.DateUtil;
import com.ne.commons.Sys;
import com.ne.commons.annotations.NotNull;
import com.ne.commons.annotations.Nullable;
import com.ne.commons.database.SQL;
import com.ne.commons.database.TableRow;
import com.ne.commons.network.util.ThreadPoolManager;
import com.ne.commons.utils.collections.Partitioner;
import com.ne.gs.configs.main.HousingConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.Auction;
import com.ne.gs.model.gameobjects.LetterType;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.model.templates.mail.MailPart;
import com.ne.gs.model.templates.mail.MailTemplate;
import com.ne.gs.network.aion.serverpackets.SM_RECEIVE_BIDS;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.questEngine.model.QuestStatus;
import com.ne.gs.services.mail.SystemMailService;

import static com.ne.gs.modules.housing.HouseAuction.AuctionTable.*;
import static com.ne.gs.modules.housing.Housing.*;

/**
 * @author hex1r0
 */
public class HouseAuction extends Auction<HouseAuction.HouseLot> {

    public static void schedule(@NotNull State state, long delay) {
        Housing.housing().tell(new ScheduleAuction(state, delay));
    }

    static void saveLot(HouseLot lot) {
        SQL.insertOrUpdate(AuctionTable.class,
            TableRow.of(AuctionTable.class)
                .set(PLAYER_ID, lot.getBid().getId().value())
                .set(HOUSE_ID, lot.getHouseTpl().getHouseId())
                .set(PRICE, lot.getCurrentPrice())
                .set(BID_COUNT, lot.getBidCount())
                .set(DATE, Sys.timestamp()))
            .submit();
    }

    static void deleteLot(HouseLot lot) {
        SQL.delete(AuctionTable.class)
            .where(HOUSE_ID, lot.getHouseTpl().getHouseId())
            .submit();
    }

    @Override
    public HouseLot removeLot(LotId id) {
        HouseLot lot = super.removeLot(id);
        if (lot != null) {
            updateSequenceNumbers();
        }
        return lot;
    }

    @Override
    public boolean addLot(HouseLot lot) {
        boolean res = super.addLot(lot);
        if (res) {
            updateSequenceNumbers();
        }
        return res;
    }

    List<HouseLot> getOrderedLots() {
        Ordering<HouseLot> bySeqNum = new Ordering<HouseLot>() {
            @Override
            public int compare(HouseLot left, HouseLot right) {
                return Ints.compare(left.getSequenceNumber(), right.getSequenceNumber());
            }
        };

        return bySeqNum.immutableSortedCopy(getAllLots());
    }

    void updateSequenceNumbers() {
        List<HouseLot> orderedLots = getOrderedLots();
        for (int i = 0; i < orderedLots.size(); i++) {
            orderedLots.get(i).setSequenceNumber(i + 1);
        }
    }

    private State _state = State.STOPPED;
    private ScheduledFuture<?> _futureAction;

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    /**
     * Cancels previous future and sets new or null
     *
     * @param futureAction
     */
    void setFutureAction(@Nullable ScheduledFuture<?> futureAction) {
        if (_futureAction != null) {
            _futureAction.cancel(false);
        }

        _futureAction = futureAction;
    }

    public int leftToProcessSeconds() {
        return (int) (processMillis() - Sys.millis()) / 1000;
    }

    long processMillis() {
        return DateUtil.cronAfterMillis(HousingConfig.AUCTION_TIME);
    }

    void sendMail(final House.HouseTemplate houseTpl,
                  final PlayerCommonData pcd,
                  final BidResult result,
                  final int time,
                  long returnKinah) {
        MailTemplate template =
            DataManager.SYSTEM_MAIL_TEMPLATES.getMailTemplate("$$HS_AUCTION_MAIL", "", pcd.getRace());

        MailPart formatter = new MailPart() {
            @Override
            public String getParamValue(String name) {
                if ("address".equals(name)) {
                    return Integer.toString(houseTpl.getHouseId());
                }

                if ("datetime".equals(name)) {
                    return Integer.toString(time);
                }

                if ("resultid".equals(name)) {
                    return Integer.toString(result.getId());
                }

                if ("raceid".equals(name)) {
                    return Integer.toString(pcd.getRace().getRaceId());
                }

                return "";
            }
        };
        String title = template.getFormattedTitle(formatter);
        String message = template.getFormattedMessage(formatter);

        SystemMailService.getInstance().sendMail("$$HS_AUCTION_MAIL",
            pcd.getName(), title, message, 0, 0L, returnKinah, LetterType.NORMAL);
    }

    public enum State {
        START,
        PROCESS,
        STOPPED
    }

    public static class Info {

        private final EnumMap<Arg, String> _map = new EnumMap<>(Arg.class);

        public String get(Arg arg) {
            return _map.get(arg);
        }

        public void set(Arg arg, String value) {
            _map.put(arg, value);
        }

        public Set<Map.Entry<Arg, String>> entrySet() {
            return _map.entrySet();
        }

        public static enum Arg {
            TIME,
            STATE,
            PROCESS_TIME,
            LOT_COUNT,
            BID_COUNT
        }
    }

    @com.ne.commons.database.SqlTable(name = "house_auction")
    public static enum AuctionTable implements com.ne.commons.database.TableColumn {
        HOUSE_ID("house_id", Integer.class),
        PLAYER_ID("player_id", Integer.class),
        PRICE("price", Long.class),
        BID_COUNT("bid_count", Integer.class),
        DATE("date", Timestamp.class);

        private final String _name;
        private final Class<?> _type;

        private AuctionTable(String name, Class<?> type) {
            _name = name;
            _type = type;
        }

        @Override
        public String getName() {
            return _name;
        }

        @Override
        public Class<?> getType() {
            return _type;
        }
    }

    enum BidResult { // TODO find out what each means
        FAILED_BID(0),
        CANCELED_BID(1),
        FAILED_SALE(2),
        SUCCESS_SALE(3),
        WIN_BID(4),
        GRACE_START(5),
        GRACE_FAIL(6),
        GRACE_SUCCESS(7);

        private final int _id;

        private BidResult(int id) {
            _id = id;
        }

        public int getId() {
            return _id;
        }

        public static BidResult valueOf(int id) {
            for (BidResult result : values()) {
                if (result.getId() == id) {
                    return result;
                }
            }

            return null;
        }
    }

    static class HouseBid extends Auction.Bid {

        private HouseBid(BidId id, long price) {
            super(id, price);
        }

        public static HouseBid create(long price) {
            return create(-1, price);
        }

        public static HouseBid create(Integer objectId, long price) {
            return new HouseBid(BidId.create(objectId), price);
        }
    }

    static class HouseLot extends Auction.Lot<HouseBid> {

        private final House.HouseTemplate _houseTpl;
        private int _sequenceNumber;

        public HouseLot(House.HouseTemplate houseTpl, HouseBid bid) {
            super(HouseLotId.of(houseTpl), bid);
            _houseTpl = houseTpl;
        }

        House.HouseTemplate getHouseTpl() {
            return _houseTpl;
        }

        public int getSequenceNumber() {
            return _sequenceNumber;
        }

        public void setSequenceNumber(int sequenceNumber) {
            _sequenceNumber = sequenceNumber;
        }

        public static HouseLot create(House.HouseTemplate houseTpl, long price) {
            return new HouseLot(houseTpl, HouseBid.create(price));
        }
    }

    static class HouseLotId extends com.ne.gs.model.Auction.LotId<House.HouseTemplate> {

        private HouseLotId(House.HouseTemplate value) {
            super(value);
        }

        public static HouseLotId of(House.HouseTemplate tpl) {
            return new HouseLotId(tpl);
        }
    }

    // ------------------------------------------------------------------------
    // -- actions
    // ------------------------------------------------------------------------

    public static final class ScheduleAuction implements Runnable {

        private final State _state;
        private final long _delay;

        ScheduleAuction(@NotNull State state, long delay) {
            _state = state;
            _delay = delay;
        }

        @Override
        public void run() {
            Runnable task = null;
            switch (_state) {
                case START:
                    task = new Trigger(new StartupAuction());
                    break;
                case PROCESS:
                    task = new Trigger(new ProcessAuction());
                    break;
                case STOPPED:
                    task = new Trigger(new ShutdownAuction());
                    break;
            }

            ScheduledFuture future = ThreadPoolManager.getInstance().schedule(task, _delay);

            auction().setFutureAction(future);

            log().info(String.format("Auction: Scheduled %s to %s",
                _state, DateUtil.date(Sys.millis() + _delay)));
        }

        /**
         * Used for scheduling purposes
         */
        private static class Trigger implements Runnable {

            private final Runnable _task;

            Trigger(Runnable task) {
                _task = task;
            }

            @Override
            public void run() {
                housing().tell(_task);
            }
        }
    }

    private static final class StartupAuction implements Runnable {

        @Override
        public void run() {
            log().info("Auction: Starting...");
            auction().setState(State.START);

            int eCount = 0;
            int aCount = 0;

            // [1] restore lots from database
            for (TableRow<AuctionTable> row : SQL.select(AuctionTable.class).submit()) {
                Integer playerId = row.get(PLAYER_ID);
                Integer houseId = row.get(HOUSE_ID);
                Long bid = row.get(PRICE);
                Integer bidCount = row.get(BID_COUNT);
                House.HouseTemplate tpl = HOUSE_TEMPLATES.get(houseId);

                if (tpl == null) {
                    log().warn(String.format("Auction: Invalid houseId=%d in database", houseId));

                    // cleanup invalid records
                    PlayerCommonData pcd = PlayerCommonData.get(playerId);
                    if (pcd != null) {
                        auction()
                            .sendMail(HOUSE_TEMPLATES.get(6001), pcd, BidResult.FAILED_BID, Sys.seconds(), bid);
                        log().info(String.format("Auction: Cleanup: Bid %d returned to %s", bid, pcd.getName()));
                    }

                    SQL.delete(AuctionTable.class).where(HOUSE_ID, houseId).submit();
                    continue;
                }

                //                // skip houses that are already owned
                //                if (!housing().getHouseByTpl(tpl).isFree()) {
                //                    log().warn(String.format("Auction: Lot %s already owned.", tpl));
                //                    SQL.delete(AuctionTable.class).where(HOUSE_ID, houseId).submit();
                //                    continue;
                //                }

                HouseLot lot = auction().getLot(HouseLotId.of(tpl));
                if (lot == null) {
                    lot = HouseLot.create(tpl, tpl.getPrice());
                    auction().addLot(lot);
                }

                lot.setBid(HouseBid.create(playerId, bid));
                lot.setBidCount(bidCount);

                housing().getHouseByTpl(tpl).setState(House.State.SELLING);

                switch (findHouseRace(tpl)) {
                    case ELYOS:
                        eCount++;
                        break;
                    case ASMODIANS:
                        aCount++;
                        break;
                }
            }

            // [2] add lots until > HousingConfig.AUCTION_LOT_COUNT for each Race
            Collection<House> houses = housing().getAllHouses();
            for (House house : houses) {
                if (HousingConfig.AUCTION_LOT_COUNT != 0) {
                    if (eCount > HousingConfig.AUCTION_LOT_COUNT && aCount > HousingConfig.AUCTION_LOT_COUNT) {
                        break;
                    }
                }

                if (auction().containsLot(HouseLotId.of(house.getTemplate()))) {
                    continue;
                }

                if (house.isFree()) {
                    House.HouseTemplate tpl = house.getTemplate();

                    switch (findHouseRace(tpl)) {
                        case ELYOS:
                            if (HousingConfig.AUCTION_LOT_COUNT != 0 && eCount >= HousingConfig.AUCTION_LOT_COUNT) {
                                continue;
                            } else {
                                eCount++;
                            }
                            break;
                        case ASMODIANS:
                            if (HousingConfig.AUCTION_LOT_COUNT != 0 && aCount >= HousingConfig.AUCTION_LOT_COUNT) {
                                continue;
                            } else {
                                aCount++;
                            }
                            break;
                    }

                    auction().addLot(HouseLot.create(tpl, tpl.getPrice()));
                    house.setState(House.State.SELLING);
                }
            }

            // [3] save in database
            for (HouseLot lot : auction().getAllLots()) {
                saveLot(lot);
            }

            // [4] schedule processing
            schedule(State.PROCESS, auction().processMillis() - Sys.millis());

            log().info(String.format("Auction: Started with %d lots", auction().getLotCount()));
        }
    }

    private static final class ProcessAuction implements Runnable {
        @Override
        public void run() {
            log().info("Auction: Processing...");

            if (auction().getState() != State.START) {
                log().info("Auction: aborted");
                return;
            }

            auction().setState(State.PROCESS);

            for (HouseLot lot : auction().getAllLots()) {
                try {
                    BidId bidId = lot.getBid().getId();
                    if (bidId.isEmpty()) {
                        continue;
                    }

                    PlayerCommonData pcd = PlayerCommonData.get(bidId.value());
                    if (pcd == null) {
                        continue;
                    }

                    House.HouseTemplate tpl = lot.getHouseTpl();
                    House house = housing().getHouseByTpl(tpl);
                    house.setState(House.State.WAITING); // state wiil be changed by GiveHouse if necessary
                    housing().invoke(new GiveHouse(pcd, tpl)); // invoke explicitly

                    Player player = pcd.getPlayer();
                    if (player != null) {
                        player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_BID_WIN(tpl.getHouseId()));
                    }

                    auction().sendMail(tpl, pcd, BidResult.WIN_BID, Sys.seconds(), 0);
                } catch (Exception e) {
                    log().error("Auction: Unable to process lot " + lot.getHouseTpl(), e);
                }
            }

            // remove all lots from auction & database
            auction().removeAll();
            SQL.truncate(AuctionTable.class);

            // start auction
            schedule(State.START, 0);  // TODO implement cooltime

            log().info("Auction: Processed");
        }
    }

    private static final class ShutdownAuction implements Runnable {

        @Override
        public void run() {
            log().info("Auction: Shutting down...");
            auction().setState(State.STOPPED);

            auction().setFutureAction(null);
            auction().removeAll();

            log().info("Auction: Shutted down");
        }
    }

    public static final class Truncate implements Runnable {

        @Override
        public void run() {
            log().info("Auction: Truncating");
            housing().invoke(new ShutdownAuction());
            SQL.truncate(AuctionTable.class);
            log().info("Auction: Truncated");
        }
    }

    public Info getInfo() {
        Collection<HouseLot> lots = getAllLots();

        int totalBidCount = 0;
        for (HouseLot lot : lots) {
            totalBidCount += lot.getBidCount();
        }

        Info i = new Info();
        i.set(Info.Arg.TIME, Long.toString(Sys.millis()));
        i.set(Info.Arg.STATE, getState().toString());
        i.set(Info.Arg.PROCESS_TIME, Long.toString(processMillis()));
        i.set(Info.Arg.BID_COUNT, Integer.toString(totalBidCount));
        i.set(Info.Arg.LOT_COUNT, Integer.toString(getLotCount()));

        return i;
    }

    static final class SendLots implements Runnable {

        private final Player _player;

        SendLots(@NotNull Player player) {
            _player = player;
        }

        @Override
        public void run() {
            // check if player completed quest for flat (required to view auction)
            switch (_player.getRace()) {
                case ELYOS:
                    if (_player.getQuestStateList().getQuestStatus(18802) != QuestStatus.COMPLETE) {
                        return;
                    }
                    break;
                case ASMODIANS:
                    if (_player.getQuestStateList().getQuestStatus(28802) != QuestStatus.COMPLETE) {
                        return;
                    }
                    break;
            }

            List<HouseLot> lots = new ArrayList<>(auction().getLotCount());

            HouseLot playerBidLot = null, playerPutToSaleLot = null;
            for (HouseLot lot : auction().getOrderedLots()) {
                if (findHouseRace(lot.getHouseTpl()) == _player.getRace()) {
                    lots.add(lot);
                }

                Integer bidderId = lot.getBid().getId().value();
                if (playerBidLot == null && bidderId.equals(_player.getObjectId())) {
                    playerBidLot = lot;
                }
            }

            for (House house : housing().getPlayerHouses(_player.getObjectId())) {
                if (house.getState() == House.State.SELLING) {
                    playerPutToSaleLot = auction().getLot(HouseLotId.of(house.getTemplate()));
                    break;
                }
            }

            final HouseLot f1 = playerBidLot, f2 = playerPutToSaleLot;
            Partitioner.of(lots, 181).foreach(new Partitioner.Func2<HouseLot>() {
                @Override
                public boolean apply(List<HouseLot> list, boolean first, boolean last) {
                    _player.sendPck(new Network.SpAuctionLotInfo(first, last, list, f1, f2));
                    return true;
                }
            });
        }
    }

    public static class PlaceBid implements Runnable {

        private final Player _player;
        private final int _sequenceNumber;
        private final long _price;

        public PlaceBid(@NotNull Player player, int sequenceNumber, long price) {
            _player = player;
            _sequenceNumber = sequenceNumber;
            _price = price;
        }

        @Override
        public void run() {
            // [1] check status
            if (!HousingConfig.AUCTION_STATUS || auction().getState() != State.START) {
                _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_BID_TIMEOUT);
                return;
            }

            // [2] find lot
            HouseLot lot = null;
            for (HouseLot l : auction().getAllLots()) {
                if (l.getSequenceNumber() == _sequenceNumber) {
                    lot = l;
                    break;
                }
            }

            // [3] check lot
            if (lot == null) {
                log().warn(String.format("Auction: Player %s requested unexisting lot!", _player.getName()));
                return;
            }

            House.HouseTemplate tpl = lot.getHouseTpl();
            House house = housing().getHouseByTpl(tpl);
            Integer playerId = _player.getObjectId();

            // [4] check level
            if (tpl.getMinLevel() > _player.getLevel()) {
                _player.sendPck(SM_SYSTEM_MESSAGE
                    .STR_MSG_HOUSING_CANT_BID_LOW_LEVEL(tpl.getMinLevel()));
                return;
            }

            // [5] check if house is not owned by this player
            if (house.getOwnerUid().equals(playerId)) {
                _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_BID_MY_HOUSE);
                return;
            }

            // [6] check if player tries to bid for his lot
            if (lot.getBid().getId().value().equals(playerId)) {
                _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_BID_SUCC_BID_HOUSE);
                return;
            }

            // [7] check price
            if (_price < 0 //
                || _price < lot.getCurrentPrice() //
                || _price - lot.getCurrentPrice() >= lot
                .getCurrentPrice() * HousingConfig.HOUSE_AUCTION_BID_LIMIT / 100) {
                _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_BID_EXCESS_ACCOUNT);
                return;
            }

            // [8] try take kinah
            if (!_player.getInventory().tryDecreaseKinah(_price)) {
                _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_BID_EXCESS_ACCOUNT);
                return;
            }

            // [9] save
            HouseBid prev = lot.placeBid(HouseBid.create(playerId, _price));

            saveLot(lot);

            log().info(String.format("Auction: Player %s placed %d for %s",
                _player.getName(), _price, house.getName()));

            _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_BID_SUCCESS(house.getHouseId()));
            _player.sendPck(new SM_RECEIVE_BIDS(0));

            // [10] return kinah to loser
            if (!prev.getId().isEmpty()) {
                PlayerCommonData loser = PlayerCommonData.get(prev.getId().value());
                if (loser.isOnline()) {
                    Player l = loser.getPlayer();
                    l.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_BID_CANCEL);
                    l.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_PRICE_CHANGE(_price));
                }

                auction().sendMail(tpl, loser, BidResult.FAILED_BID, Sys.seconds(), prev.getPrice());
                log().info(String.format("Auction: Bid %d for %s returned to %s", prev.getPrice(), house
                    .getName(), loser.getName()));
            }
        }
    }

    public static class RemoveFromSale implements Runnable {

        protected final House.HouseTemplate _tpl;

        public RemoveFromSale(House.HouseTemplate tpl) {
            _tpl = tpl;
        }

        @Override
        public void run() {
            HouseLotId lotId = HouseLotId.of(_tpl);
            HouseLot lot = auction().getLot(lotId);
            if (lot == null) {
                return;
            }

            auction().removeLot(lotId);
            deleteLot(lot);
            log().info(String.format("Auction: Lot %s removed", _tpl.getName()));

            HouseBid bid = lot.getBid();
            if (!bid.getId().isEmpty()) {
                PlayerCommonData loser = PlayerCommonData.get(bid.getId().value());
                if (loser.isOnline()) {
                    Player l = loser.getPlayer();
                    l.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_BID_CANCEL);
                    l.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_PRICE_CHANGE(0));
                }

                auction().sendMail(_tpl, loser, BidResult.FAILED_BID, Sys.seconds(), bid.getPrice());
                log().info(String.format("Auction: Bid %d for %s returned to %s",
                    bid.getPrice(), _tpl.getName(), loser.getName()));
            }
        }
    }

    public static class PutForSale implements Runnable {

        protected final House.HouseTemplate _tpl;
        protected final long _price;

        public PutForSale(House.HouseTemplate tpl, long price) {
            _tpl = tpl;
            _price = price;
        }

        @Override
        public void run() {
            process();

            log().info(String.format("Auction: Put for sale %s for %d", _tpl.getName(), _price));
        }

        protected void process() {
            // delete from database
            //housing().delete(house.getOwnerUid(), _tpl);

            HouseLot lot = HouseLot.create(_tpl, _price);
            auction().addLot(lot);
            saveLot(lot);

            housing().getHouseByTpl(_tpl).setState(House.State.SELLING);
        }
    }

    public static class PutForSaleByPlayer extends PutForSale {

        private final Player _player;

        public PutForSaleByPlayer(@NotNull Player player, long price) {
            super(null, price);
            _player = player;
        }

        @Override
        public void run() {
            if (!HousingConfig.AUCTION_STATUS || auction().getState() != State.START) {
                _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_AUCTION_TIMEOUT);
                return;
            }

            House home = housing().getHome(_player.getObjectId());

            if (home == null) {
                return;
            }

            if (auction().containsLot(HouseLotId.of(home.getTemplate()))) {
                return;
            }

            long fee = (long) (_price * 0.3F);

            if (!_player.getInventory().tryDecreaseKinah(fee)) {
                _player.sendPck(SM_SYSTEM_MESSAGE.STR_NOT_ENOUGH_MONEY);
                return;
            }

            process();

            _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_AUCTION_MY_HOUSE(home.getHouseId()));

            log().info(String.format("Auction: Player %s put for sale %s for %d",
                _player.getName(), home.getName(), _price));
        }
    }
}
