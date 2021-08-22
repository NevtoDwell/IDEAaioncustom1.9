/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.configs.ingameshop.InGameShopProperty;
import com.ne.gs.model.ingameshop.InGameShopEn;
import com.ne.gs.model.templates.ingameshop.IGCategory;
import com.ne.gs.model.templates.ingameshop.IGSubCategory;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author xTz
 */
public class SM_IN_GAME_SHOP_CATEGORY_LIST extends AionServerPacket {

    private final int type;
    private final int categoryId;
    private final InGameShopProperty ing;

    public SM_IN_GAME_SHOP_CATEGORY_LIST(int type, int category) {
        this.type = type;
        categoryId = category;
        ing = InGameShopEn.getInstance().getIGSProperty();
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(type);
        switch (type) {
            case 0:
                writeH(ing.size());
                for (IGCategory category : ing.getCategories()) {
                    writeD(category.getId());
                    writeS(category.getName());
                }
                break;
            case 2:
                if (categoryId < ing.size()) {
                    IGCategory iGCategory = ing.getCategories().get(categoryId);
                    writeH(iGCategory.getSubCategories().size());
                    for (IGSubCategory subCategory : iGCategory.getSubCategories()) {
                        writeD(subCategory.getId());
                        writeS(subCategory.getName());
                    }
                }
                break;
        }
    }
}
