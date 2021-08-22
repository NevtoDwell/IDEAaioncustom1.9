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

import com.ne.gs.model.templates.item.DecomposableItemInfo;
import com.ne.gs.model.templates.item.ExtractedItemsCollection;

/**
 * @author antness
 */
@XmlRootElement(name = "decomposable_items")
@XmlAccessorType(XmlAccessType.FIELD)
public class DecomposableItemsData {

    @XmlElement(name = "decomposable")
    private List<DecomposableItemInfo> decomposableItemsTemplates;
    private final TIntObjectHashMap<List<ExtractedItemsCollection>> decomposableItemsInfo = new TIntObjectHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        decomposableItemsInfo.clear();
        for (DecomposableItemInfo template : decomposableItemsTemplates) {
            decomposableItemsInfo.put(template.getItemId(), template.getItemsCollections());
        }

        decomposableItemsTemplates = null;
    }

    public int size() {
        return decomposableItemsInfo.size();
    }

    public List<ExtractedItemsCollection> getInfoByItemId(int itemId) {
        return decomposableItemsInfo.get(itemId);
    }

    public TIntObjectHashMap<List<ExtractedItemsCollection>> getDecomposableItemsInfo() {
        return decomposableItemsInfo;
    }
}
