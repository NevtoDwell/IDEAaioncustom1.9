/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.broker;

import java.util.ArrayList;
import java.util.List;

import com.ne.gs.model.gameobjects.BrokerItem;

/**
 * @author ATracer
 */
public class BrokerPlayerCache {

    private BrokerItem[] brokerListCache = new BrokerItem[0];
    private int brokerMaskCache;
    private int brokerSoftTypeCache;
    private int brokerStartPageCache;
    private List<Integer> itemList = new ArrayList<>();

    /**
     * @return the brokerListCache
     */
    public BrokerItem[] getBrokerListCache() {
        return brokerListCache;
    }

    /**
     * @param brokerListCache
     *     the brokerListCache to set
     */
    public void setBrokerListCache(BrokerItem[] brokerListCache) {
        this.brokerListCache = brokerListCache;
    }

    /**
     * @return the brokerMaskCache
     */
    public int getBrokerMaskCache() {
        return brokerMaskCache;
    }

    /**
     * @param brokerMaskCache
     *     the brokerMaskCache to set
     */
    public void setBrokerMaskCache(int brokerMaskCache) {
        this.brokerMaskCache = brokerMaskCache;
    }

    /**
     * @return the brokerSoftTypeCache
     */
    public int getBrokerSortTypeCache() {
        return brokerSoftTypeCache;
    }

    /**
     * @param brokerSoftTypeCache
     *     the brokerSoftTypeCache to set
     */
    public void setBrokerSortTypeCache(int brokerSoftTypeCache) {
        this.brokerSoftTypeCache = brokerSoftTypeCache;
    }

    /**
     * @return the brokerStartPageCache
     */
    public int getBrokerStartPageCache() {
        return brokerStartPageCache;
    }

    /**
     */
    public List<Integer> getSearchItemList() {
        if (itemList == null) {
            return null;
        }
        return itemList;
    }

    /**
     * @param brokerStartPageCache
     *     the brokerStartPageCache to set
     */
    public void setBrokerStartPageCache(int brokerStartPageCache) {
        this.brokerStartPageCache = brokerStartPageCache;
    }

    /**
     */
    public void setSearchItemsList(List<Integer> itemList) {
        this.itemList = itemList;
    }
}
