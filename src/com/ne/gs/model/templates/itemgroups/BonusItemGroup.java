/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.itemgroups;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.templates.rewards.BonusType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BonusItemGroup")
@XmlSeeAlso({CraftItemGroup.class, CraftRecipeGroup.class, ManastoneGroup.class, FoodGroup.class, MedicineGroup.class, OreGroup.class, GatherGroup.class, EnchantGroup.class, BossGroup.class})
public abstract class BonusItemGroup {

    @XmlAttribute(name = "bonusType", required = true)
    protected BonusType bonusType;

    @XmlAttribute(name = "chance")
    protected Float chance;

    public BonusType getBonusType() {
        return bonusType;
    }

    public float getChance() {
        if (chance == null) {
            return 0.0f;
        }

        return chance.floatValue();
    }

    public abstract ItemRaceEntry[] getRewards();
}
