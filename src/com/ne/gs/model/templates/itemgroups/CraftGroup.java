/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.itemgroups;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.lang3.Range;

import com.ne.gs.model.templates.rewards.CraftReward;

/**
 * @author Rolandas
 */
public abstract class CraftGroup extends BonusItemGroup {

    private TIntObjectHashMap<Map<Range<Integer>, List<CraftReward>>> dataHolder;

    public ItemRaceEntry[] getRewards(Integer skillId) {
        if (!dataHolder.containsKey(skillId)) {
            return new ItemRaceEntry[0];
        }
        List<CraftReward> result = new ArrayList<>();
        for (List<CraftReward> items : dataHolder.get(skillId).values()) {
            result.addAll(items);
        }
        return result.toArray(new ItemRaceEntry[0]);
    }

    public ItemRaceEntry[] getRewards(Integer skillId, Integer skillPoints) {
        if (!dataHolder.containsKey(skillId)) {
            return new ItemRaceEntry[0];
        }
        List<CraftReward> result = new ArrayList<>();
        for (Entry<Range<Integer>, List<CraftReward>> entry : dataHolder.get(skillId).entrySet()) {
            if (entry.getKey().contains(skillPoints)) {
                result.addAll(entry.getValue());
            }
        }
        return result.toArray(new ItemRaceEntry[0]);
    }

    /**
     * @return the dataHolder
     */
    public TIntObjectHashMap<Map<Range<Integer>, List<CraftReward>>> getDataHolder() {
        return dataHolder;
    }

    /**
     * @param dataHolder
     *     the dataHolder to set
     */
    public void setDataHolder(TIntObjectHashMap<Map<Range<Integer>, List<CraftReward>>> dataHolder) {
        this.dataHolder = dataHolder;
    }
}
