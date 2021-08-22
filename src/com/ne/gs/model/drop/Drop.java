/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.drop;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.Collection;
import java.util.Set;

import com.ne.commons.utils.Rnd;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ItemCategory;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.services.custom.KeyDropTuningService;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "drop")
public class Drop implements DropCalculator {

    @XmlAttribute(name = "item_id", required = true)
    protected int itemId;
    @XmlAttribute(name = "min_amount")
    protected int minAmount = 1;
    @XmlAttribute(name = "max_amount")
    protected int maxAmount = 1;
    @XmlAttribute(required = true)
    protected float chance;
    @XmlAttribute(name = "no_reduce")
    protected boolean noReduce = false;
    @XmlAttribute(name = "eachmember")
    protected boolean eachMember = false;
    private ItemTemplate template;

    public Drop() {
    }

    public Drop(int itemId, int minAmount, int maxAmount, float chance, boolean noReduce) {
        this.itemId = itemId;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.chance = chance;
        this.noReduce = noReduce;
        template = DataManager.ITEM_DATA.getItemTemplate(itemId);
    }

    public ItemTemplate getItemTemplate() {
        return template == null ? DataManager.ITEM_DATA.getItemTemplate(itemId) : template;
    }

    public int getItemId() {
        return itemId;
    }

    /**
     * Gets the value of the minAmount property.
     */
    public int getMinAmount() {
        return minAmount;
    }

    /**
     * Gets the value of the maxAmount property.
     */
    public int getMaxAmount() {
        return maxAmount;
    }

    /**
     * Gets the value of the chance property.
     */
    public float getChance() {
        return chance;
    }

    public boolean isNoReduction() {
        return noReduce;
    }

    public boolean isEachMember() {
        return eachMember;
    }

    @Override
    public int dropCalculator(Set<DropItem> result, int index, float dropModifier, Race race, Collection<Player> groupMembers) {
        float percent = chance;
        //exception for quest drops
        ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(itemId);
        if(itemTemplate.getCategory() == ItemCategory.QUEST) {
        	percent = 100f;
        }
        else if(itemTemplate.getCategory() == ItemCategory.KEY) {
        	percent = KeyDropTuningService.getInstance().getModifiedChance(itemId, percent);
        }
        
        if (chance < 100f) {
            if (!noReduce) {
                percent *= dropModifier;
            }
        }
        if (Rnd.chance(percent)) {
            if (eachMember && groupMembers != null && !groupMembers.isEmpty()) {
                for (Player player : groupMembers) {
                    DropItem dropitem = new DropItem(this);
                    dropitem.calculateCount();
                    dropitem.setIndex(index++);
                    dropitem.setPlayerObjId(player.getObjectId());
                    dropitem.setWinningPlayer(player);
                    dropitem.isDistributeItem(true);
                    result.add(dropitem);
                }
            } else {
                DropItem dropitem = new DropItem(this);
                dropitem.calculateCount();
                dropitem.setIndex(index++);
                result.add(dropitem);
            }
        }
        return index;
    }

    @Override
    public String toString() {
        return "Drop [itemId=" + itemId + ", minAmount=" + minAmount + ", maxAmount=" + maxAmount + ", chance=" + chance + ", noReduce=" + noReduce
            + ", eachMember=" + eachMember + "]";
    }
}
