/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers.models.xmlQuest.conditions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.questEngine.model.QuestEnv;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PcInventoryCondition")
public class PcInventoryCondition extends QuestCondition {

    @XmlAttribute(name = "item_id", required = true)
    protected int itemId;
    @XmlAttribute(required = true)
    protected long count;

    /**
     * Gets the value of the itemId property.
     */
    public int getItemId() {
        return itemId;
    }

    /**
     * Gets the value of the count property.
     */
    public long getCount() {
        return count;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.ne.gs.questEngine.handlers.template.xmlQuest.condition.QuestCondition#doCheck(com.ne.gs
     * .questEngine.model.QuestEnv)
     */
    @Override
    public boolean doCheck(QuestEnv env) {
        Player player = env.getPlayer();
        long itemCount = player.getInventory().getItemCountByItemId(itemId);
        switch (getOp()) {
            case EQUAL:
                return itemCount == count;
            case GREATER:
                return itemCount > count;
            case GREATER_EQUAL:
                return itemCount >= count;
            case LESSER:
                return itemCount < count;
            case LESSER_EQUAL:
                return itemCount <= count;
            case NOT_EQUAL:
                return itemCount != count;
            default:
                return false;
        }
    }
}
