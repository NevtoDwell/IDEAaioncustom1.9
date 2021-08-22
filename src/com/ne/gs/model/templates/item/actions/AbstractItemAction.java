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
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractItemAction")
public abstract class AbstractItemAction {
	
	private boolean isPetDopingAction;

    /**
     * Check if an item can be used.
     *
     * @param player
     * @param parentItem
     * @param targetItem
     *
     * @return
     */
    public abstract boolean canAct(Player player, Item parentItem, Item targetItem);

    /**
     * @param player
     * @param parentItem
     * @param targetItem
     */
    public abstract void act(Player player, Item parentItem, Item targetItem);

	public boolean isPetDopingAction() {
		return isPetDopingAction;
	}

	public void setPetDopingAction(boolean isPetDopingAction) {
		this.isPetDopingAction = isPetDopingAction;
	}

}
