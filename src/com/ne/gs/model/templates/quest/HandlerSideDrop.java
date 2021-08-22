/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.quest;

/**
 * @author vlog
 */
public class HandlerSideDrop extends QuestDrop {

    private final int neededAmount;

    public HandlerSideDrop(int questId, int npcId, int itemId, int amount, int chance, boolean dropEachMember) {
        this.questId = questId;
        this.npcId = npcId;
        this.itemId = itemId;
        this.chance = chance;
        this.dropEachMember = dropEachMember ? 1 : 0;
        this.neededAmount = amount;
    }

    public int getNeededAmount() {
        return neededAmount;
    }
}
