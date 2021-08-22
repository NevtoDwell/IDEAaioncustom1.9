/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.dao;

import java.util.List;
import javolution.util.FastMap;

import com.ne.commons.database.dao.DAO;
import com.ne.gs.model.ingameshop.IGItem;

/**
 * @author xTz, KID
 */
public abstract class InGameShopDAO implements DAO {

    public abstract boolean deleteIngameShopItem(int itemId, byte category, byte list, int param);

    public abstract FastMap<Byte, List<IGItem>> loadInGameShopItems();

    public abstract void saveIngameShopItem(int paramInt1, int paramInt2, long paramLong1, long paramLong2, byte paramByte1, byte paramByte2, int paramInt3,
                                            int paramInt4, byte paramByte3, byte paramByte4, String paramString1, String paramString2);

    public abstract boolean increaseSales(int object, int current);

    @Override
    public String getClassName() {
        return InGameShopDAO.class.getName();
    }
}
