/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.dataholders;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;
import gnu.trove.map.hash.TIntObjectHashMap;

import com.ne.gs.model.items.ItemMask;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.model.templates.restriction.ItemCleanupTemplate;

/**
 * @author Luno
 */
@XmlRootElement(name = "item_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemData {

    @XmlElement(name = "item_template")
    private List<ItemTemplate> its;

    @XmlTransient
    private TIntObjectHashMap<ItemTemplate> items;

    void afterUnmarshal(Unmarshaller u, Object parent) {
        items = new TIntObjectHashMap<>();
        for (ItemTemplate it : its) {
            items.put(it.getTemplateId(), it);
        }

        its = null;
    }

    public void cleanup() {
        for (ItemCleanupTemplate ict : DataManager.ITEM_CLEAN_UP.getList()) {
            ItemTemplate template = items.get(ict.getId());
            applyCleanup(template, ict.resultTrade(), ItemMask.TRADEABLE);
            applyCleanup(template, ict.resultSell(), ItemMask.SELLABLE);
            applyCleanup(template, ict.resultWH(), ItemMask.STORABLE_IN_WH);
            applyCleanup(template, ict.resultAccountWH(), ItemMask.STORABLE_IN_AWH);
            applyCleanup(template, ict.resultLegionWH(), ItemMask.STORABLE_IN_LWH);
        }
    }

    private void applyCleanup(ItemTemplate item, byte result, int mask) {
        if (result != -1) {
            switch (result) {
                case 1:
                    item.modifyMask(true, mask);
                    break;
                case 0:
                    item.modifyMask(false, mask);
                    break;
            }
        }
    }

    public ItemTemplate getItemTemplate(int itemId) {
        return items.get(itemId);
    }

    /**
     * @return items.size()
     */
    public int size() {
        return items.size();
    }
}
