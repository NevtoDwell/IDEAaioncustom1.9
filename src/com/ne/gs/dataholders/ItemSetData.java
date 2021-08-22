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
import java.util.List;
import gnu.trove.map.hash.TIntObjectHashMap;

import com.ne.gs.model.templates.itemset.ItemPart;
import com.ne.gs.model.templates.itemset.ItemSetTemplate;

/**
 * @author ATracer
 */
@XmlRootElement(name = "item_sets")
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemSetData {

    @XmlElement(name = "itemset")
    protected List<ItemSetTemplate> itemsetList;

    private TIntObjectHashMap<ItemSetTemplate> sets;

    // key: item id, value: associated item set template
    // This should provide faster search of the item template set by item id
    private TIntObjectHashMap<ItemSetTemplate> setItems;

    void afterUnmarshal(Unmarshaller u, Object parent) {
        sets = new TIntObjectHashMap<>();
        setItems = new TIntObjectHashMap<>();

        for (ItemSetTemplate set : itemsetList) {
            sets.put(set.getId(), set);

            // Add reference to the ItemSetTemplate from
            for (ItemPart part : set.getItempart()) {
                setItems.put(part.getItemid(), set);
            }
        }
        itemsetList = null;
    }

    /**
     * @param itemSetId
     *
     * @return
     */
    public ItemSetTemplate getItemSetTemplate(int itemSetId) {
        return sets.get(itemSetId);
    }

    /**
     * @param itemId
     *
     * @return
     */
    public ItemSetTemplate getItemSetTemplateByItemId(int itemId) {
        return setItems.get(itemId);
    }

    /**
     * @return itemSets.size()
     */
    public int size() {
        return sets.size();
    }
}
