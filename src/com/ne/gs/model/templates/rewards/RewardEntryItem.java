/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.rewards;

/**
 * @author KID
 */
public class RewardEntryItem {

    public RewardEntryItem(int unique, int item_id, long count) {
        this.unique = unique;
        this.id = item_id;
        this.count = count;
    }

    public int id, unique;
    public long count;
}
