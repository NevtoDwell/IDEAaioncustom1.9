/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAttribute;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;

public class DecorateAction extends AbstractItemAction {

    @XmlAttribute(name = "id")
    private Integer partId;

    @Override
    public boolean canAct(Player player, Item parentItem, Item targetItem) {
        return false;
    }

    @Override
    public void act(Player player, Item parentItem, Item targetItem) {
    }

    public int getTemplateId() {
        if (partId == null) {
            return 0;
        }
        return partId;
    }
}
