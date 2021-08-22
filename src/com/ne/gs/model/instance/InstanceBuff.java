/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.instance.instancereward.InstanceReward;
import com.ne.gs.model.instance.instancereward.PvPArenaReward;
import com.ne.gs.model.stats.calc.StatOwner;
import com.ne.gs.model.stats.calc.functions.IStatFunction;
import com.ne.gs.model.stats.calc.functions.StatAddFunction;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.model.templates.instance_bonusatrr.InstanceBonusAttr;
import com.ne.gs.model.templates.instance_bonusatrr.InstancePenaltyAttr;
import com.ne.gs.skillengine.change.Func;
import com.ne.gs.utils.ThreadPoolManager;

public class InstanceBuff implements StatOwner {

    private Future<?> task;
    private final List<IStatFunction> functions = new ArrayList<>();
    private final InstanceBonusAttr instanceBonusAttr;

    public InstanceBuff(int buffId) {
        instanceBonusAttr = DataManager.INSTANCE_BUFF_DATA.getInstanceBonusattr(buffId);
    }

    public void applyEffect(Player player, int time) {
        if (hasInstanceBuff() || instanceBonusAttr == null) {
            return;
        }
        if (time != 0) {
            task = ThreadPoolManager.getInstance().schedule(new InstanceBuffTask(player), time);
        }
        for (InstancePenaltyAttr instancePenaltyAttr : instanceBonusAttr.getPenaltyAttr()) {
            StatEnum stat = instancePenaltyAttr.getStat();
            int statToModified = player.getGameStats().getStat(stat, 0).getBase();
            int value = instancePenaltyAttr.getValue();
            int valueModified = instancePenaltyAttr.getFunc().equals(Func.PERCENT) ? statToModified * value / 100 : value;
            functions.add(new StatAddFunction(stat, valueModified, true));
        }
        player.getGameStats().addEffect(this, functions);
    }

    public void endEffect(Player player) {
        functions.clear();
        if (hasInstanceBuff()) {
            task.cancel(true);
        }
        player.getGameStats().endEffect(this);
        sendPacket(player);
    }

    private void sendPacket(Player player) {
        if (player.isOnline()) {
            InstanceReward<?> instanceReward = player.getPosition().getWorldMapInstance().getInstanceHandler().getInstanceReward();
            if (instanceReward != null && (instanceReward instanceof PvPArenaReward)) {
                ((PvPArenaReward) instanceReward).sendPacket();
            }
        }
    }

    public boolean hasInstanceBuff() {
        return task != null && !task.isDone();
    }

    private class InstanceBuffTask implements Runnable {

        private final Player player;

        public InstanceBuffTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            endEffect(player);
        }
    }
}
