/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model;

import java.util.List;
import java.util.Map;
import com.google.common.collect.ImmutableList;
import gnu.trove.map.hash.THashMap;

import com.ne.commons.func.Id;
import com.ne.gs.model.Auction.Lot;

/**
 * @author hex1r0
 */
public class Auction<T extends Lot<?>> {

    private final Map<LotId, T> _lots = new THashMap<>();

    public boolean addLot(T lot) {
        if (_lots.containsKey(lot.getId())) {
            return false;
        }

        _lots.put(lot.getId(), lot);
        return true;
    }

    public T removeLot(LotId id) {
        return _lots.remove(id);
    }

    public void removeAll() {
        _lots.clear();
    }

    public boolean containsLot(LotId id) {
        return _lots.containsKey(id);
    }

    public List<T> getAllLots() {
        return ImmutableList.copyOf(_lots.values());
    }

    public T getLot(LotId lotId) {
        return _lots.get(lotId);
    }

    public int getLotCount() {
        return _lots.size();
    }

    // ------------------------------------------------------------------------

    public static class Lot<E extends Bid> {

        private final LotId _id;
        private E _bid;
        private int _bidCount;

        public Lot(LotId id, E bid) {
            _id = id;
            _bid = bid;
        }

        public LotId getId() {
            return _id;
        }

        public E getBid() {
            return _bid;
        }

        public void setBid(E bid) {
            _bid = bid;
        }

        public long getCurrentPrice() {
            return _bid.getPrice();
        }

        public int getBidCount() {
            return _bidCount;
        }

        public void setBidCount(int bidCount) {
            _bidCount = bidCount;
        }

        public E placeBid(E bid) {
            E prev = _bid;
            _bid = bid;
            _bidCount++;

            return prev;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean equals(Object obj) {
            return obj instanceof Lot && _id.equals(((Lot) obj)._id);
        }

        @Override
        public int hashCode() {
            return _id.hashCode();
        }
    }

    public static class Bid {

        private final BidId _id;
        private final long _price;

        public Bid(BidId id, long price) {
            _id = id;
            _price = price;
        }

        public BidId getId() {
            return _id;
        }

        public long getPrice() {
            return _price;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Bid && _id.equals(((Bid) obj)._id);
        }

        @Override
        public int hashCode() {
            return _id.hashCode();
        }
    }

    public static class LotId<T> implements Id<T> {

        private final T _value;

        public LotId(T value) {
            _value = value;
        }

        @Override
        public T value() {
            return _value;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof LotId && _value.equals(((LotId) obj)._value);
        }

        @Override
        public int hashCode() {
            return _value.hashCode();
        }
    }

    public static class BidId implements Id<Integer> {

        public static final BidId EMPTY = new BidId(-1);

        public static BidId create(Integer value) {
            if (value.equals(-1)) {
                return EMPTY;
            } else {
                return new BidId(value);
            }
        }

        private final Integer _value;

        public BidId(Integer value) {
            _value = value;
        }

        public boolean isEmpty() {
            return equals(EMPTY);
        }

        @Override
        public Integer value() {
            return _value;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof BidId && _value.equals(((BidId) obj)._value);
        }

        @Override
        public int hashCode() {
            return _value.hashCode();
        }
    }

}
