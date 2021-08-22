/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */

package com.ne.gs.modules.pvpevent;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.services.abyss.AbyssPointsService;

/**
 * This class ...
 *
 * @author hex1r0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbyssPoints")
public class AbyssPoints extends Penalty {

    @XmlAttribute(name = "value", required = true)
    private int _value;

    @Override
    public void apply(Player player) {
        if (_value != 0) {
            AbyssPointsService.addAp(player, _value);
        }
    }
}
