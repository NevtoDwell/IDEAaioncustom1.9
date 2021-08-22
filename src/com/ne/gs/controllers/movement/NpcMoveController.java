/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.movement;
import java.util.List;
import com.ne.gs.ai2.AI2Logger;
import com.ne.gs.ai2.AIState;
import com.ne.gs.ai2.AISubState;
import com.ne.gs.ai2.NpcAI2;
import com.ne.gs.ai2.handler.TargetEventHandler;
import com.ne.gs.ai2.manager.WalkManager;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.geometry.Point3D;
import com.ne.gs.model.stats.calc.Stat2;
import com.ne.gs.model.templates.walker.RouteStep;
import com.ne.gs.model.templates.zone.Point2D;
import com.ne.gs.network.aion.serverpackets.SM_MOVE;
import com.ne.gs.spawnengine.WalkerGroup;
import com.ne.gs.taskmanager.tasks.MoveTaskManager;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.collections.LastUsedCache;
import com.ne.gs.world.World;
import mw.Global;
import mw.engines.geo.GeoEngine;
import mw.engines.geo.GeoHelper;
import mw.engines.geo.math.Vector3f;
import mw.processors.movement.motor.FollowMotor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author ATracer
 */
public class NpcMoveController extends CreatureMoveController<Npc> {
    private static final Logger log = LoggerFactory.getLogger(NpcMoveController.class);
    public static final float MOVE_CHECK_OFFSET = 0.1f;
    private static final float MOVE_OFFSET = 0.05f;
    private Destination destination = Destination.TARGET_OBJECT;
    private float pointX;
    private float pointY;
    private float pointZ;
    private LastUsedCache<Byte, Point3D> lastSteps = null;
    private byte stepSequenceNr = 0;
    private float offset = 0.1f;
    // walk related
    List<RouteStep> currentRoute;
    int currentPoint;
    int walkPause;
    private float cachedTargetZ;

    private FollowMotor _followMotor;

    public NpcMoveController(Npc owner) {
        super(owner);
    }
    private static enum Destination {
        TARGET_OBJECT,
        POINT;
    }
    /**
     * Move to current target
     */
    public void moveToTargetObject() {
        if (started.compareAndSet(false, true)) {
            if (owner.getAi2().isLogging()) {
                AI2Logger.moveinfo(owner, "MC: moveToTarget started");
            }
            destination = Destination.TARGET_OBJECT;
            updateLastMove();
            MoveTaskManager.getInstance().addCreature(owner);
        }
    }
    public void moveToPoint(float x, float y, float z) {
        if (started.compareAndSet(false, true)) {
            if (owner.getAi2().isLogging()) {
                AI2Logger.moveinfo(owner, "MC: moveToPoint started");
            }
            destination = Destination.POINT;
            pointX = x;
            pointY = y;
            pointZ = z;
            updateLastMove();
            MoveTaskManager.getInstance().addCreature(owner);
        }
    }
    public void moveToNextPoint() {
        if (started.compareAndSet(false, true)) {
            if (owner.getAi2().isLogging()) {
                AI2Logger.moveinfo(owner, "MC: moveToNextPoint started");
            }
            destination = Destination.POINT;
            updateLastMove();
            MoveTaskManager.getInstance().addCreature(owner);
        }
    }
    /**
     * @return if destination reached
     */
    @Override
    public void moveToDestination() {
        if (owner.getAi2().isLogging()) {
            AI2Logger.moveinfo(owner, "moveToDestination destination: " + destination);
        }
        if (owner.getLifeStats() != null && owner.getLifeStats().isAlreadyDead()) {
            abortMove();
            return;
        }
        if (!owner.canPerformMove()) {
            if (owner.getAi2().isLogging()) {
                AI2Logger.moveinfo(owner, "moveToDestination can't perform move");
            }
            if (started.compareAndSet(true, false)) {
                cancelFollow();
                setAndSendStopMove(owner);
            }
            updateLastMove();
            return;
        }
        else if (started.compareAndSet(false, true)) {
            movementMask = MovementMask.NPC_STARTMOVE;
            PacketSendUtility.broadcastPacket(owner, new SM_MOVE(owner));
        }
        if (!started.get()) {
            if (owner.getAi2().isLogging()) {
                AI2Logger.moveinfo(owner, "moveToDestination not started");
            }
        }
        switch (destination) {
            case TARGET_OBJECT:
                Npc npc = (Npc) owner;
                VisibleObject target = owner.getTarget();// todo no target
                if (target == null) {
                    cancelFollow();
                    return;
                }
                if (!(target instanceof Creature)) {
                    cancelFollow();
                    return;
                }

                /*if (MathUtil.getDistance(target, pointX, pointY, pointZ) > MOVE_CHECK_OFFSET) {
                    Creature creature = (Creature) target;
                    offset = npc.getController().getAttackDistanceToTarget();
                    pointX = target.getX();
                    pointY = target.getY();
                    pointZ = getTargetZ(npc, creature);
                }
                moveToLocation(pointX, pointY, pointZ, offset);
                */
                applyFollow(target);
                break;
            case POINT:
                cancelFollow();
                offset = 0.1f;
                moveToLocation(pointX, pointY, pointZ, offset);
                break;
        }
        updateLastMove();
    }
    /**
     * @param npc
     * @param creature
     * @return
     */
    private float getTargetZ(Npc npc, Creature creature) {
        float targetZ = creature.getZ();
        if (creature.isInFlyingState() && !npc.isInFlyingState() ) {
            if (npc.getGameStats().checkGeoNeedUpdate()) {
                cachedTargetZ = GeoHelper.getZ(creature);
            }
            targetZ = cachedTargetZ;
        }
        return targetZ;
    }
    /**
     * @param targetX
     * @param targetY
     * @param targetZ
     * @param offset
     * @return
     */
    protected void moveToLocation(float targetX, float targetY, float targetZ, float offset) {
        boolean directionChanged = false;
        float ownerX = owner.getX();
        float ownerY = owner.getY();
        float ownerZ = owner.getZ();
        directionChanged = targetX != targetDestX || targetY != targetDestY || targetZ != targetDestZ;
        if (directionChanged) {
            heading = (byte) (Math.toDegrees(Math.atan2(targetY - ownerY, targetX - ownerX)) / 3);
        }
        if (owner.getAi2().isLogging()) {
            AI2Logger.moveinfo(owner, "OLD targetDestX: " + targetDestX + " targetDestY: " + targetDestY + " targetDestZ " + targetDestZ);
        }
        // to prevent broken walkers in case of activating/deactivating zones
        if (targetX == 0 && targetY == 0) {
            targetX = owner.getSpawn().getX();
            targetY = owner.getSpawn().getY();
            targetZ = owner.getSpawn().getZ();
        }
        targetDestX = targetX;
        targetDestY = targetY;
        targetDestZ = targetZ;
        if (owner.getAi2().isLogging()) {
            AI2Logger.moveinfo(owner, "ownerX=" + ownerX + " ownerY=" + ownerY + " ownerZ=" + ownerZ);
            AI2Logger.moveinfo(owner, "targetDestX: " + targetDestX + " targetDestY: " + targetDestY + " targetDestZ " + targetDestZ);
        }
        float currentSpeed = owner.getGameStats().getMovementSpeedFloat();
        float futureDistPassed = currentSpeed * (System.currentTimeMillis() - lastMoveUpdate) / 1000f;
        float dist = (float) MathUtil.getDistance(ownerX, ownerY, ownerZ, targetX, targetY, targetZ);
        if (owner.getAi2().isLogging()) {
            AI2Logger.moveinfo(owner, "futureDist: " + futureDistPassed + " dist: " + dist);
        }
        if (dist == 0) {
            if (owner.getAi2().getState() == AIState.RETURNING) {
                if (owner.getAi2().isLogging()) {
                    AI2Logger.moveinfo(owner, "State RETURNING: abort move");
                }
                TargetEventHandler.onTargetReached((NpcAI2) owner.getAi2());
            }
            return;
        }
        if (futureDistPassed > dist) {
            futureDistPassed = dist;
        }
        float distFraction = futureDistPassed / dist;
        float newX = (targetDestX - ownerX) * distFraction + ownerX;
        float newY = (targetDestY - ownerY) * distFraction + ownerY;
        float newZ = (targetDestZ - ownerZ) * distFraction + ownerZ;

        if ((ownerX == newX) && (ownerY == newY) && owner.getSpawn().getRandomWalk() > 0) {
            return;
        }

        if (owner.getAi2().getSubState() != AISubState.WALK_PATH &&
                owner.getAi2().getState() != AIState.RETURNING && owner.getGameStats().getLastGeoZUpdate() < System.currentTimeMillis()) {
            // fix Z if npc doesn't move to spawn point
            if (owner.getSpawn().getX() != targetDestX || owner.getSpawn().getY() != targetDestY || owner.getSpawn().getZ() != targetDestZ) {
                Vector3f norm = new Vector3f(newX, newY, ownerZ);
                if(GeoEngine.setGroundZ(owner, norm) != null)
                {
                    float geoZ = norm.z;
                    if (Math.abs(newZ - geoZ) > 1)
                        directionChanged = true;

                    newZ = norm.z;
                }
            }
            owner.getGameStats().setLastGeoZUpdate(System.currentTimeMillis() + 1000);
        }
        if (owner.getAi2().isLogging()) {
            AI2Logger.moveinfo(owner, "newX=" + newX + " newY=" + newY + " newZ=" + newZ + " mask=" + movementMask);
        }
        World.getInstance().updatePosition(owner, newX, newY, newZ, heading, false);
        byte newMask = getMoveMask(directionChanged);
        if (movementMask != newMask) {
            if (owner.getAi2().isLogging()) {
                AI2Logger.moveinfo(owner, "oldMask=" + movementMask + " newMask=" + newMask);
            }
            movementMask = newMask;
            PacketSendUtility.broadcastPacket(owner, new SM_MOVE(owner));
        }
    }
    private byte getMoveMask(boolean directionChanged) {
        if (directionChanged)
            return MovementMask.NPC_STARTMOVE;
        else if (owner.getAi2().getState() == AIState.RETURNING)
            return MovementMask.NPC_RUN_FAST;
        else if (owner.getAi2().getState() == AIState.FOLLOWING)
            return MovementMask.NPC_WALK_SLOW;
        byte mask = MovementMask.IMMEDIATE;
        final Stat2 stat = owner.getGameStats().getMovementSpeed();
        if (owner.isInState(CreatureState.WEAPON_EQUIPPED)) {
            mask = stat.getBonus() < 0 ? MovementMask.NPC_RUN_FAST : MovementMask.NPC_RUN_SLOW;
        }
        else if (owner.isInState(CreatureState.WALKING) || owner.isInState(CreatureState.ACTIVE)) {
            mask = stat.getBonus() < 0 ? MovementMask.NPC_WALK_FAST : MovementMask.NPC_WALK_SLOW;
        }
        if (owner.isFlying())
            mask |= MovementMask.GLIDE;
        return mask;
    }
    @Override
    public void abortMove() {
        if (!started.get())
            return;
        resetMove();
        setAndSendStopMove(owner);
    }
    /**
     * Initialize values to default ones
     */
    public void resetMove() {
        if (owner.getAi2().isLogging()) {
            AI2Logger.moveinfo(owner, "MC perform stop");
        }
        cancelFollow();
        started.set(false);
        targetDestX = 0;
        targetDestY = 0;
        targetDestZ = 0;
        pointX = 0;
        pointY = 0;
        pointZ = 0;
    }
    /**
     * Walker
     *
     * @param currentRoute
     */
    public void setCurrentRoute(List<RouteStep> currentRoute) {
        if (currentRoute == null) {
            AI2Logger.info(owner.getAi2(), String.format("MC: setCurrentRoute is setting route to null (NPC id: {})!!!", owner.getNpcId()));
        }
        else {
            this.currentRoute = currentRoute;
        }
        this.currentPoint = 0;
    }
    public void setRouteStep(RouteStep step, RouteStep prevStep) {
        Point2D dest = null;
        if (owner.getWalkerGroup() != null) {
            dest = WalkerGroup.getLinePoint(new Point2D(prevStep.getX(), prevStep.getY()), new Point2D(step.getX(), step.getY()),
                    owner.getWalkerGroupShift());
            this.pointZ = GeoEngine.getZ(owner.getWorldId(), owner.getInstanceId(), step.getX(), step.getY());
            if(this.pointZ == 0.0)
            	this.pointZ = prevStep.getZ();
            owner.getWalkerGroup().setStep(owner, step.getRouteStep());
        }
        else {
            this.pointZ = step.getZ();
        }
        this.currentPoint = step.getRouteStep() - 1;
        this.pointX = dest == null ? step.getX() : dest.getX();
        this.pointY = dest == null ? step.getY() : dest.getY();
        this.destination = Destination.POINT;
        this.walkPause = step.getRestTime();
    }
    public int getCurrentPoint() {
        return currentPoint;
    }
    public boolean isReachedPoint() {
        return MathUtil.getDistance(owner.getX(), owner.getY(), owner.getZ(), pointX, pointY, pointZ) < MOVE_OFFSET;
    }
    public void chooseNextStep() {
        int oldPoint = currentPoint;
        if (currentRoute == null) {
            WalkManager.stopWalking((NpcAI2) owner.getAi2());
            //log.warn("Bad Walker Id: " + owner.getNpcId() + " - point: " + oldPoint);
            return;
        }
        if (currentPoint < (currentRoute.size() - 1)) {
            currentPoint++;
        }
        else {
            currentPoint = 0;
        }
        setRouteStep(currentRoute.get(currentPoint), currentRoute.get(oldPoint));
    }
    public int getWalkPause() {
        return walkPause;
    }
    public boolean isChangingDirection() {
        return currentPoint == 0;
    }
    @Override
    public final float getTargetX2() {
        return started.get() ? targetDestX : owner.getX();
    }
    @Override
    public final float getTargetY2() {
        return started.get() ? targetDestY : owner.getY();
    }
    @Override
    public final float getTargetZ2() {
        return started.get() ? targetDestZ : owner.getZ();
    }
    /**
     * @return
     */
    public boolean isFollowingTarget() {
        return destination == Destination.TARGET_OBJECT;
    }
    public void storeStep() {
        if (owner.getAi2().getState() == AIState.RETURNING)
            return;
        if (lastSteps == null)
            lastSteps = new LastUsedCache<Byte, Point3D>(10);
        Point3D currentStep = new Point3D(owner.getX(), owner.getY(), owner.getZ());
        if (owner.getAi2().isLogging()) {
            AI2Logger.moveinfo(owner, "store back step: X=" + owner.getX() + " Y=" + owner.getY() + " Z=" + owner.getZ());
        }
        if (stepSequenceNr == 0 || MathUtil.getDistance(lastSteps.get(stepSequenceNr), currentStep) >= 10)
            lastSteps.put(++stepSequenceNr, currentStep);
    }
    public Point3D getPreviousStep(){
        if (lastSteps == null)
            lastSteps = new LastUsedCache<Byte, Point3D>(10);
        Point3D result = stepSequenceNr == 0 ? null : lastSteps.get(stepSequenceNr--);
        if (result == null) {
            result = new Point3D(
                    owner.getSpawn().getX(),
                    owner.getSpawn().getY(),
                    owner.getSpawn().getZ());
        }
        return result;
    }
    public Point3D recallPreviousStep() {
        if (lastSteps == null)
            lastSteps = new LastUsedCache<Byte, Point3D>(10);
        Point3D result = stepSequenceNr == 0 ? null : lastSteps.get(stepSequenceNr--);
        if (result == null) {
            if (owner.getAi2().isLogging())
                AI2Logger.moveinfo(owner, "recall back step: spawn point");
            targetDestX = owner.getSpawn().getX();
            targetDestY = owner.getSpawn().getY();
            targetDestZ = owner.getSpawn().getZ();
            result = new Point3D(targetDestX, targetDestY, targetDestZ);
        }
        else {
            if (owner.getAi2().isLogging())
                AI2Logger.moveinfo(owner, "recall back step: X=" + result.getX() + " Y=" + result.getY() + " Z=" + result.getZ());
            targetDestX = result.getX();
            targetDestY = result.getY();
            targetDestZ = result.getZ();
        }
        return result;
    }
    public void clearBackSteps() {
        stepSequenceNr = 0;
        lastSteps = null;
        movementMask = MovementMask.IMMEDIATE;
    }

    private void applyFollow(VisibleObject target){

        if(_followMotor != null && _followMotor._target == target)
            return;

        if(_followMotor != null) {
            _followMotor.stop();
        }

        _followMotor = new FollowMotor(Global.MovementProcessor, owner, target);
        _followMotor.start();
    }

    private void cancelFollow(){

        if(_followMotor != null){
            _followMotor.stop();
            _followMotor = null;
        }
    }
}