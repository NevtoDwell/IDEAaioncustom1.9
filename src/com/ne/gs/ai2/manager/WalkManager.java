/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2.manager;

import com.ne.commons.utils.Rnd;
import com.ne.gs.ai2.AIState;
import com.ne.gs.ai2.AISubState;
import com.ne.gs.ai2.NpcAI2;
import com.ne.gs.configs.main.AIConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.templates.walker.RouteStep;
import com.ne.gs.model.templates.walker.WalkerTemplate;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.ThreadPoolManager;
import mw.Global;
import mw.engines.geo.GeoEngine;
import mw.engines.geo.collision.CollidableType;
import mw.engines.geo.math.Vector3f;
import mw.processors.movement.PathfindHelper;

import java.util.List;

/**
 * @author ATracer
 */
public final class WalkManager {
    private static final int WALK_RANDOM_RANGE = 5;

    private static final int MINIMAL_WALK_RANDOM_RANGE = 5;

    private static final int MAXIMAL_WALK_RANDOM_RANGE = 20;

    /**
     * @param npcAI
     */
    public static boolean startWalking(NpcAI2 npcAI) {
        npcAI.setStateIfNot(AIState.WALKING);
        Npc owner = npcAI.getOwner();
        WalkerTemplate template = DataManager.WALKER_DATA.getWalkerTemplate(owner.getSpawn().getWalkerId());
        if (template != null && npcAI.setSubStateIfNot(AISubState.WALK_PATH)) {
            startRouteWalking(npcAI, owner, template);
        } else {
            return startRandomWalking(npcAI, owner);
        }
        return true;
    }

    /**
     * @param npcAI
     * @param owner
     */
    private static boolean startRandomWalking(NpcAI2 npcAI, Npc owner) {
        if (!AIConfig.ACTIVE_NPC_MOVEMENT) {
            return false;
        }
        int randomWalkNr = owner.getSpawn().getRandomWalk();
        if (randomWalkNr == 0) {
            return false;
        }
        if (npcAI.setSubStateIfNot(AISubState.WALK_RANDOM)) {
            EmoteManager.emoteStartWalking(npcAI.getOwner());
            chooseNextRandomPoint(npcAI);
            return true;
        }
        return false;
    }

    /**
     * @param npcAI
     * @param owner
     * @param template
     */
    protected static void startRouteWalking(NpcAI2 npcAI, Npc owner, WalkerTemplate template) {
        if (!AIConfig.ACTIVE_NPC_MOVEMENT) {
            return;
        }
        List<RouteStep> route = template.getRouteSteps();
        int currentPoint = owner.getMoveController().getCurrentPoint();
        RouteStep nextStep = findNextRoutStep(owner, route);
        owner.getMoveController().setCurrentRoute(route);
        owner.getMoveController().setRouteStep(nextStep, route.get(currentPoint));
        EmoteManager.emoteStartWalking(npcAI.getOwner());
        npcAI.getOwner().getMoveController().moveToNextPoint();
    }

    /**
     * @param owner
     * @param route
     * @return
     */
    protected static RouteStep findNextRoutStep(Npc owner, List<RouteStep> route) {
        int currentPoint = owner.getMoveController().getCurrentPoint();
        RouteStep nextStep = null;
        if (currentPoint != 0) {
            nextStep = findNextRouteStepAfterPause(owner, route, currentPoint);
        } else {
            nextStep = findClosestRouteStep(owner, route, nextStep);
        }
        return nextStep;
    }

    /**
     * @param owner
     * @param route
     * @param nextStep
     * @return
     */
    protected static RouteStep findClosestRouteStep(Npc owner, List<RouteStep> route, RouteStep nextStep) {
        double closestDist = 0;
        float x = owner.getX();
        float y = owner.getY();
        float z = owner.getZ();
        if (owner.getWalkerGroup() != null) {
            // always choose the 1st step, not the last which is close enough
            if (owner.getWalkerGroup().getGroupStep() < 2) {
                nextStep = route.get(0);
            } else {
                nextStep = route.get(owner.getWalkerGroup().getGroupStep() - 1);
            }
        } else {
            for (RouteStep step : route) {
                double stepDist = MathUtil.getDistance(x, y, z, step.getX(), step.getY(), step.getZ());
                if (closestDist == 0 || stepDist < closestDist) {
                    closestDist = stepDist;
                    nextStep = step;
                }
            }
        }
        return nextStep;
    }

    /**
     * @param owner
     * @param route
     * @param currentPoint
     * @return
     */
    protected static RouteStep findNextRouteStepAfterPause(Npc owner, List<RouteStep> route, int currentPoint) {
        RouteStep nextStep = route.get(currentPoint);
        double stepDist = MathUtil.getDistance(owner.getX(), owner.getY(), owner.getZ(), nextStep.getX(), nextStep.getY(), nextStep.getZ());
        if (stepDist < 1) {
            nextStep = nextStep.getNextStep();
        }
        return nextStep;
    }

    /**
     * Is this npc will walk. Currently all monsters will walk and those npc wich has walk routes
     *
     * @param npcAI
     * @return
     */
    public static boolean isWalking(NpcAI2 npcAI) {
        return npcAI.isMoveSupported() && (hasWalkRoutes(npcAI) || npcAI.getOwner().isAttackableNpc());
    }

    /**
     * @param npcAI
     * @return
     */
    public static boolean hasWalkRoutes(NpcAI2 npcAI) {
        return npcAI.getOwner().hasWalkRoutes();
    }

    /**
     * @param npcAI
     */
    public static void targetReached(NpcAI2 npcAI) {
        if (npcAI.isInState(AIState.WALKING)) {
            switch (npcAI.getSubState()) {
                case WALK_PATH:
                    npcAI.getOwner().updateKnownlist();
                    if (npcAI.getOwner().getWalkerGroup() != null) {
                        npcAI.getOwner().getWalkerGroup().targetReached(npcAI);
                    } else {
                        chooseNextRouteStep(npcAI);
                    }
                    break;
                case WALK_WAIT_GROUP:
                    npcAI.setSubStateIfNot(AISubState.WALK_PATH);
                    chooseNextRouteStep(npcAI);
                    break;
                case WALK_RANDOM:
                    chooseNextRandomPoint(npcAI);
                    break;
                case TALK:
                    npcAI.getOwner().getMoveController().abortMove();
                    break;
            }
        }
    }

    /**
     * @param npcAI
     */
    protected static void chooseNextRouteStep(final NpcAI2 npcAI) {
        int walkPause = npcAI.getOwner().getMoveController().getWalkPause();
        if (walkPause == 0) {
            npcAI.getOwner().getMoveController().resetMove();
            npcAI.getOwner().getMoveController().chooseNextStep();
            npcAI.getOwner().getMoveController().moveToNextPoint();
        } else {
            npcAI.getOwner().getMoveController().abortMove();
            npcAI.getOwner().getMoveController().chooseNextStep();

            //TODO temporary post movement to movement processor to decrease main pool tasks counter
            Global.MovementProcessor.schedule(() -> {
                if (npcAI.isInState(AIState.WALKING)) {
                    npcAI.getOwner().getMoveController().moveToNextPoint();
                }
            }, walkPause);
        }
    }

    /**
     * @param npcAI
     */
    private static void chooseNextRandomPoint(final NpcAI2 npcAI) {
        final Npc owner = npcAI.getOwner();
        owner.getMoveController().abortMove();
        int randomWalkNr = owner.getSpawn().getRandomWalk();
        final int walkRange = Math.max(randomWalkNr, WALK_RANDOM_RANGE);
        final float distToSpawn = (float) owner.getDistanceToSpawnLocation();

        int delay = Rnd.get(AIConfig.MINIMIMUM_DELAY, AIConfig.MAXIMUM_DELAY) * 1000;

        //TODO temporary post movement to movement processor to decrease main pool tasks counter
        Global.MovementProcessor.schedule(() -> {

            if (npcAI.isInState(AIState.WALKING)) {

                if (distToSpawn > walkRange) {
                    owner.getMoveController().moveToPoint(owner.getSpawn().getX(), owner.getSpawn().getY(), owner.getSpawn().getZ());
                } else {

                    int maxRange = MAXIMAL_WALK_RANDOM_RANGE;
                    if (walkRange > MINIMAL_WALK_RANDOM_RANGE && walkRange < MAXIMAL_WALK_RANDOM_RANGE)
                        maxRange = walkRange;

                    Vector3f nextPoint = PathfindHelper.getRandomPoint(
                            owner,
                            MINIMAL_WALK_RANDOM_RANGE,
                            maxRange);

                    if(nextPoint == null && distToSpawn > 0.01f)
                        nextPoint = new Vector3f(owner.getSpawn().getX(), owner.getSpawn().getY(), owner.getSpawn().getZ());

                    if (nextPoint != null) {
                        owner.getMoveController().moveToPoint(nextPoint.x, nextPoint.y, nextPoint.z);
                    }  else {
                        //no available point was found and creature already at spawn location
                        //so fuck to random walk
                    }
                }
            }

        }, delay);
    }

    /**
     * @param npcAI
     */
    public static void stopWalking(NpcAI2 npcAI) {
        npcAI.getOwner().getMoveController().abortMove();
        npcAI.setStateIfNot(AIState.IDLE);
        npcAI.setSubStateIfNot(AISubState.NONE);
        EmoteManager.emoteStopWalking(npcAI.getOwner());
    }

    /**
     * @return
     */
    public static boolean isArrivedAtPoint(NpcAI2 npcAI) {
        return npcAI.getOwner().getMoveController().isReachedPoint();
    }
}