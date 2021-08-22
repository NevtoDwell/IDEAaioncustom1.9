/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.items;

import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.ObserverType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.PersistentState;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;

/**
 * @author ATracer
 */
public class ChargeInfo extends ActionObserver {

    public static final int LEVEL2 = 1000000;
    public static final int LEVEL1 = 500000;

    private int chargePoints;
    private final int attackBurn;
    private final int defendBurn;
    private final Item item;
    private Player player;

    /**
     * @param chargePoints
     */
    public ChargeInfo(int chargePoints, Item item) {
        super(ObserverType.ATTACK_DEFEND);
        this.chargePoints = chargePoints;
        this.item = item;
        if (item.getImprovement() != null) {
            attackBurn = item.getImprovement().getBurnAttack();
            defendBurn = item.getImprovement().getBurnDefend();
        } else {
            attackBurn = 0;
            defendBurn = 0;
        }
    }

    public int getChargePoints() {
        return chargePoints;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int updateChargePoints(int addPoints) {
        int newChargePoints = chargePoints + addPoints;
        if (newChargePoints > LEVEL2) {
            newChargePoints = LEVEL2;
        } else if (newChargePoints < 0) {
            newChargePoints = 0;
        }
        if (item.isEquipped() && player != null) {
            player.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);
        }
        item.setPersistentState(PersistentState.UPDATE_REQUIRED);
        chargePoints = newChargePoints;
        return newChargePoints;
    }

    @Override
    public void attacked(Creature creature) {
        updateChargePoints(-defendBurn);
        Player player = this.player;
        if (player != null) {
            player.sendPck(new SM_INVENTORY_UPDATE_ITEM(player, item));
        }
    }

    @Override
    public void attack(Creature creature) {
        updateChargePoints(-attackBurn);
        Player player = this.player;
        if (player != null) {
            player.sendPck(new SM_INVENTORY_UPDATE_ITEM(player, item));
        }
    }

}
