/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.items;

import java.util.List;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.PersistentState;
import com.ne.gs.model.stats.calc.functions.StatFunction;
import com.ne.gs.model.templates.item.ItemTemplate;

/**
 * @author ATracer
 */
public class ManaStone extends ItemStone {

    private List<StatFunction> modifiers;

    public ManaStone(int itemObjId, int itemId, int slot, PersistentState persistentState) {
        super(itemObjId, itemId, slot, persistentState);

        ItemTemplate stoneTemplate = DataManager.ITEM_DATA.getItemTemplate(itemId);
        if (stoneTemplate != null && stoneTemplate.getModifiers() != null) {
            modifiers = stoneTemplate.getModifiers();
        }
    }

    /**
     * @return modifiers
     */
    public List<StatFunction> getModifiers() {
        return modifiers;
    }

    public StatFunction getFirstModifier() {
        return (modifiers != null && modifiers.size() > 0) ? modifiers.get(0) : null;
    }

}
