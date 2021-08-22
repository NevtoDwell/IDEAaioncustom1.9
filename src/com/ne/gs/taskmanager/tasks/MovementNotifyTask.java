/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.taskmanager.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ne.gs.ai2.AI2Logger;
import com.ne.gs.ai2.AIState;
import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.world.WorldMapTemplate;
import com.ne.gs.taskmanager.AbstractFIFOPeriodicTaskManager;
import com.ne.gs.world.knownlist.VisitorWithOwner;

/**
 * @author ATracer
 */
public class MovementNotifyTask extends AbstractFIFOPeriodicTaskManager<Creature> {

    private static final Map<Integer, int[]> moveBroadcastCounts = new HashMap<>();

    private final MoveNotifier MOVE_NOTIFIER = new MoveNotifier();

    public static MovementNotifyTask getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public MovementNotifyTask() {
        super(500);
    }

    @Override
    protected void callTask(Creature creature) {
        if (creature.getLifeStats().isAlreadyDead()) {
            return;
        }

        int limit = creature.getWorldId() == 400010000 ? 200 : 2147483647;
        int iterations = creature.getKnownList().doOnAllNpcsWithOwner(MOVE_NOTIFIER, limit);

        if (!(creature instanceof Player)) {
            int[] maxCounts = moveBroadcastCounts.get(creature.getWorldId());
            synchronized (maxCounts) {
                if (iterations > maxCounts[0]) {
                    maxCounts[0] = iterations;
                    maxCounts[1] = creature.getObjectTemplate().getTemplateId();
                }
            }
        }
    }

    public String[] dumpBroadcastStats() {
        List<String> lines = new ArrayList<>();
        lines.add("------- Movement broadcast counts -------");
        for (Entry<Integer, int[]> entry : moveBroadcastCounts.entrySet()) {
            lines.add("WorldId=" + entry.getKey() + ": " + entry.getValue()[0] + " (NpcId " + entry.getValue()[1] + ")");
        }
        lines.add("-----------------------------------------");
        return lines.toArray(new String[0]);
    }

    @Override
    protected String getCalledMethodName() {
        return "notifyOnMove()";
    }

    static {
        Iterator<WorldMapTemplate> iter = DataManager.WORLD_MAPS_DATA.iterator();
        while (iter.hasNext()) {
            moveBroadcastCounts.put(iter.next().getMapId(), new int[2]);
        }
    }

    private class MoveNotifier implements VisitorWithOwner<Npc, VisibleObject> {

        private MoveNotifier() {
        }

        @Override
        public void visit(Npc object, VisibleObject owner) {
            if ((object.getAi2().getState() == AIState.DIED) || (object.getLifeStats().isAlreadyDead())) {
                if (object.getAi2().isLogging()) {
                    AI2Logger.moveinfo(object, "WARN: NPC died but still in knownlist");
                }
                return;
            }
            object.getAi2().onCreatureEvent(AIEventType.CREATURE_MOVED, (Creature) owner);
        }
    }

    private static final class SingletonHolder {

        private static final MovementNotifyTask INSTANCE = new MovementNotifyTask();
    }
}
