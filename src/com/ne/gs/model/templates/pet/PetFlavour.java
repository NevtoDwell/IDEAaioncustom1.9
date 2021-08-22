/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.pet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.services.toypet.PetFeedCalculator;
import com.ne.gs.services.toypet.PetFeedProgress;
import com.ne.gs.services.toypet.PetHungryLevel;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PetFlavour", propOrder = {"food"})
public class PetFlavour {

    @XmlElement(required = true)
    protected List<PetRewards> food;

    @XmlAttribute(required = true)
    protected int id;

    @XmlAttribute(name = "full_count")
    protected int fullCount = 1;

    @XmlAttribute(name = "loved_limit")
    protected int lovedFoodLimit = 0;

    @XmlAttribute(name = "cd", required = true)
    protected int cooldown = 0;

    public List<PetRewards> getFood() {
        if (food == null) {
            food = new ArrayList<>();
        }
        return food;
    }

    public FoodType getFoodType(int itemId) {
        for (PetRewards rewards : getFood()) {
            if (DataManager.ITEM_GROUPS_DATA.isFood(itemId, rewards.getType())) {
                return rewards.getType();
            }
        }
        return null;
    }

    public PetFeedResult processFeedResult(PetFeedProgress progress, FoodType foodType, int itemLevel, int playerLevel, int rate) {
        PetRewards rewardGroup = null;
        for (PetRewards rewards : getFood()) {
            if (rewards.getType() == foodType) {
                rewardGroup = rewards;
                break;
            }
        }
        if (rewardGroup == null) {
            return null;
        }
        int maxFeedCount = 1;
        byte step = 1;
        if (rewardGroup.isLoved()) {
            progress.setIsLovedFeeded();
            if (lovedFoodLimit != 0) {
                step = (byte) Math.round(fullCount / lovedFoodLimit);
            } else {
                step = 10;
            }
        } else {
            step = 1;
        }
        maxFeedCount = fullCount;

        for(int i = 0; i < rate; i++)
            PetFeedCalculator.updatePetFeedProgress(progress, itemLevel, maxFeedCount, step);

        if (progress.getHungryLevel() != PetHungryLevel.FULL) {
            return null;
        }
        return PetFeedCalculator.getReward(maxFeedCount, rewardGroup, progress, playerLevel, itemLevel);
    }

    public boolean isLovedFood(FoodType foodType, int itemId) {
        PetRewards rewardGroup = null;
        for (PetRewards rewards : getFood()) {
            if (rewards.getType() == foodType) {
                rewardGroup = rewards;
                break;
            }
        }
        if (rewardGroup == null) {
            return false;
        }
        return rewardGroup.isLoved();
    }

    public int getId() {
        return id;
    }

    public int getFullCount() {
        return fullCount;
    }

    public int getLovedFoodLimit() {
        return lovedFoodLimit;
    }

    public int getCooldDown() {
        return cooldown;
    }
}
