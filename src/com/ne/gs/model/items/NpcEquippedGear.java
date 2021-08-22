/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.items;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.ne.gs.dataholders.loadingutils.adapters.NpcEquipmentList;
import com.ne.gs.dataholders.loadingutils.adapters.NpcEquippedGearAdapter;
import com.ne.gs.model.templates.item.ItemTemplate;

/**
 * @author Luno
 */
@XmlJavaTypeAdapter(NpcEquippedGearAdapter.class)
public class NpcEquippedGear implements Iterable<Entry<ItemSlot, ItemTemplate>> {

    private Map<ItemSlot, ItemTemplate> items;
    private short mask;

    private NpcEquipmentList v;

    public NpcEquippedGear(NpcEquipmentList v) {
        this.v = v;
    }

    /**
     * @return short
     */
    public short getItemsMask() {
        if (items == null) {
            init();
        }
        return mask;
    }

    @Override
    public Iterator<Entry<ItemSlot, ItemTemplate>> iterator() {
        if (items == null) {
            init();
        }
        return items.entrySet().iterator();
    }

    /**
     * Here NPC equipment mask is initialized. All NPC slot masks should be lower than 65536
     */
    public void init() {
        synchronized (this) {
            if (items == null) {
                items = new TreeMap<>();
                for (ItemTemplate item : v.items) {
                    ItemSlot[] itemSlots = ItemSlot.getSlotsFor(item.getItemSlot());
                    for (ItemSlot itemSlot : itemSlots) {
                        if (items.get(itemSlot) == null) {
                            items.put(itemSlot, item);
                            mask |= itemSlot.id();
                            break;
                        }
                    }
                }
            }
            v = null;
        }
    }

    /**
     * @param itemSlot
     *
     * @return
     */
    public ItemTemplate getItem(ItemSlot itemSlot) {
        return items != null ? items.get(itemSlot) : null;
    }

}
