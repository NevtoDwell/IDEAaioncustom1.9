/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.dataholders;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import gnu.trove.map.hash.TIntObjectHashMap;

import com.ne.commons.utils.Rnd;
import com.ne.gs.model.templates.item.bonuses.RandomBonus;
import com.ne.gs.model.templates.stats.ModifiersTemplate;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"randomBonuses"})
@XmlRootElement(name = "random_bonuses")
public class ItemRandomBonusData {

    @XmlElement(name = "random_bonus", required = true)
    protected List<RandomBonus> randomBonuses;

    @XmlTransient
    private final TIntObjectHashMap<RandomBonus> randomBonusData = new TIntObjectHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (RandomBonus bonus : randomBonuses) {
            randomBonusData.put(bonus.getId(), bonus);
        }
        randomBonuses = null;
    }

    public ModifiersTemplate getRandomModifiers(int rndOptionSet) {
        RandomBonus bonus = randomBonusData.get(rndOptionSet);
        if (bonus == null) {
            return null;
        }
        List<ModifiersTemplate> modifiersGroup = bonus.getModifiers();

        int chance = Rnd.get(10000);
        int current = 0;
        ModifiersTemplate template = null;
        for (ModifiersTemplate modifiers : modifiersGroup) {
            current = (int) (current + modifiers.getChance() * 100);
            if (current >= chance) {
                template = modifiers;
                break;
            }
        }
        return template;
    }

    public int size() {
        return randomBonusData.size();
    }
}
