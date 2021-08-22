/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.action;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.storage.Storage;
import com.ne.gs.skillengine.model.Skill;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemUseAction")
public class ItemUseAction extends Action {

    @XmlAttribute(required = true)
    protected int itemid;

    @XmlAttribute(required = true)
    protected int count;

    @Override
    public void act(Skill skill) {
        if (skill.getEffector() instanceof Player) {
            Player player = (Player) skill.getEffector();
            Storage inventory = player.getInventory();

            if (!inventory.decreaseByItemId(itemid, count)) {
                return;
            }
        }
    }

}
