/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item;

import javax.xml.bind.annotation.XmlEnum;

import com.ne.gs.model.SkillElement;

/**
 * @author ATracer
 */
@XmlEnum
public enum ItemAttackType {
    PHYSICAL(false, SkillElement.NONE),
    MAGICAL_EARTH(true, SkillElement.EARTH),
    MAGICAL_WATER(true, SkillElement.WATER),
    MAGICAL_WIND(true, SkillElement.WIND),
    MAGICAL_FIRE(true, SkillElement.FIRE);

    private final boolean magic;
    private final SkillElement elem;

    private ItemAttackType(boolean magic, SkillElement elem) {
        this.magic = magic;
        this.elem = elem;
    }

    public boolean isMagical() {
        return magic;
    }

    public SkillElement getMagicalElement() {
        return elem;
    }
}
