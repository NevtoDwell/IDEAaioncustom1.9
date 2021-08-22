/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.instance.playerreward;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.instance.InstanceBuff;

/**
 * @author xTz
 */
public class PvPArenaPlayerReward extends InstancePlayerReward {

    private int position;
    private int timeBonus;
    private final float timeBonusModifier;
    private int abyssPoints;
    private int crucibleInsignia;
    private int courageInsignia;
    private long logoutTime;
    private boolean isRewarded = false;
    private final InstanceBuff boostMorale = new InstanceBuff(8);

    public PvPArenaPlayerReward(Player player, int timeBonus) {
        super(player);
        super.addPoints(13000);
        this.timeBonus = timeBonus;
        timeBonusModifier = (this.timeBonus / 660000f);
        boostMorale.applyEffect(player, 20000);
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getTimeBonus() {
        return timeBonus > 0 ? timeBonus : 0;
    }

    public void updateLogOutTime() {
        logoutTime = System.currentTimeMillis();
    }

    public void updateBonusTime() {
        int offlineTime = (int) (System.currentTimeMillis() - logoutTime);
        timeBonus = (int) (timeBonus - offlineTime * timeBonusModifier);
    }

    public boolean isRewarded() {
        return isRewarded;
    }

    public void setRewarded() {
        isRewarded = true;
    }

    public int getAbyssPoints() {
        return abyssPoints;
    }

    public void setAbyssPoints(int abyssPoints) {
        this.abyssPoints = abyssPoints;
    }

    public int getCrucibleInsignia() {
        return crucibleInsignia;
    }

    public void setCrucibleInsignia(int crucibleInsignia) {
        this.crucibleInsignia = crucibleInsignia;
    }

    public int getCourageInsignia() {
        return courageInsignia;
    }

    public void setCourageInsignia(int courageInsignia) {
        this.courageInsignia = courageInsignia;
    }

    public boolean hasBoostMorale() {
        return boostMorale.hasInstanceBuff();
    }

    public void applyBoostMoraleEffect() {
        boostMorale.applyEffect(player, 20000);
    }

    public void endBoostMoraleEffect() {
        boostMorale.endEffect(player);
    }
}
