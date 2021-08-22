/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.taskmanager.tasks;

import com.google.common.base.Predicate;
import com.ne.commons.utils.chmv8.ForkJoinTask;
import com.ne.gs.ai2.AIState;
import com.ne.gs.ai2.NpcAI2;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.utils.ThreadPoolManager;
import javolution.util.FastList;
import javolution.util.FastMap;

import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.ai2.poll.AIQuestion;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.taskmanager.AbstractPeriodicTaskManager;
import com.ne.gs.taskmanager.FIFOSimpleExecutableQueue;
import com.ne.gs.world.zone.ZoneUpdateService;

import static com.ne.gs.taskmanager.parallel.ForEach.forEach;

/**
 * @author ATracer
 */
public class MoveTaskManager extends AbstractPeriodicTaskManager {

    private final FastMap<Integer, Creature> movingCreatures = new FastMap<Integer, Creature>().shared();

    public static final int UPDATE_PERIOD = 200;

    private final Predicate<Creature> CREATURE_MOVE_PREDICATE = new Predicate<Creature>() {

        @Override
        public boolean apply(Creature creature) {
            creature.getMoveController().moveToDestination();
            if (creature.getAi2().poll(AIQuestion.DESTINATION_REACHED)) {
                movingCreatures.remove(creature.getObjectId());
                creature.getAi2().onGeneralEvent(AIEventType.MOVE_ARRIVED);
                ZoneUpdateService.getInstance().add(creature);
            }
            else {

                creature.getAi2().onGeneralEvent(AIEventType.MOVE_VALIDATE);
            }
            return true;
        }

    };

    private MoveTaskManager() {
        super(UPDATE_PERIOD);
    }

    public void addCreature(Creature creature) {
        movingCreatures.put(creature.getObjectId(), creature);
    }

    public void removeCreature(Creature creature) {
        movingCreatures.remove(creature.getObjectId());
    }

    @Override
    public void run() {

        ForkJoinTask<Creature> task = forEach(new FastList<>(movingCreatures.values()), CREATURE_MOVE_PREDICATE);
        if (task != null)
            ThreadPoolManager.getInstance().getForkingPool().invoke(task);
    }

    public static MoveTaskManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static final class SingletonHolder {

        private static final MoveTaskManager INSTANCE = new MoveTaskManager();
    }

}
