/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.quest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Rewards", propOrder = {"selectableRewardItem", "rewardItem"})
public class Rewards {

    @XmlElement(name = "selectable_reward_item")
    protected List<QuestItems> selectableRewardItem;
    @XmlElement(name = "reward_item")
    protected List<QuestItems> rewardItem;
    @XmlAttribute
    protected int gold;
    @XmlAttribute
    protected int exp;
    @XmlAttribute(name = "reward_abyss_point")
    protected int rewardAbyssPoint;
    @XmlAttribute
    protected int title;
    @XmlAttribute(name = "extend_inventory")
    protected int extendInventory;
    @XmlAttribute(name = "stigma")
    protected boolean stigma;

    /**
     * Gets the value of the selectableRewardItem property.
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the selectableRewardItem property.
     * <p/>
     * For example, to add a new item, do as follows:
     * <p/>
     * <pre>
     * getSelectableRewardItem().add(newItem);
     * </pre>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link QuestItems }
     */
    public List<QuestItems> getSelectableRewardItem() {
        if (selectableRewardItem == null) {
            selectableRewardItem = new ArrayList<>(0);
        }
        return this.selectableRewardItem;
    }

    /**
     * Gets the value of the rewardItem property.
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the rewardItem property.
     * <p/>
     * For example, to add a new item, do as follows:
     * <p/>
     * <pre>
     * getRewardItem().add(newItem);
     * </pre>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link QuestItems }
     */
    public List<QuestItems> getRewardItem() {
        if (rewardItem == null) {
            rewardItem = new ArrayList<>(0);
        }
        return this.rewardItem;
    }

    /**
     * Gets the value of the gold property.
     *
     * @return possible object is {@link Integer }
     */
    public int getGold() {
        return gold;
    }

    /**
     * Gets the value of the exp property.
     *
     * @return possible object is {@link Integer }
     */
    public int getExp() {
        return exp;
    }

    /**
     * Gets the value of the rewardAbyssPoint property.
     *
     * @return possible object is {@link Integer }
     */
    public int getRewardAbyssPoint() {
        return rewardAbyssPoint;
    }

    /**
     * Gets the value of the title property.
     *
     * @return possible object is {@link Integer }
     */
    public int getTitle() {
        return title;
    }

    /**
     * @return the extendInventory
     */
    public int getExtendInventory() {
        return extendInventory;
    }

    public boolean isStigma() {
        return stigma;
    }
}
