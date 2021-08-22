/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummonHouseObjectAction")
public class SummonHouseObjectAction extends AbstractItemAction {

    @XmlAttribute(name = "id")
    private int objectId;

    @Override
    public boolean canAct(Player player, Item parentItem, Item targetItem) {
        return false;
    }

    @Override
    public void act(Player player, Item parentItem, Item targetItem) {
    }

    public int getTemplateId() {
        return objectId;
    }
}
