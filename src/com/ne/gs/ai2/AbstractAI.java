/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.google.common.base.Preconditions;

import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.utils.EventNotifier;
import com.ne.gs.ai2.event.AIEventLog;
import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.ai2.eventcallback.OnHandleAIGeneralEvent;
import com.ne.gs.ai2.handler.FollowEventHandler;
import com.ne.gs.ai2.handler.FreezeEventHandler;
import com.ne.gs.ai2.manager.SimpleAttackManager;
import com.ne.gs.ai2.manager.WalkManager;
import com.ne.gs.ai2.poll.AIAnswer;
import com.ne.gs.ai2.poll.AIAnswers;
import com.ne.gs.ai2.poll.AIQuestion;
import com.ne.gs.ai2.scenario.AI2Scenario;
import com.ne.gs.ai2.scenario.AI2Scenarios;
import com.ne.gs.configs.main.AIConfig;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.npcshout.ShoutEventType;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.world.WorldPosition;

/**
 * @author ATracer
 */
public abstract class AbstractAI implements AI2 {

    private Creature owner;
    private AIState currentState;
    private AISubState currentSubState;

    private final Lock thinkLock = new ReentrantLock();

    private boolean logging = false;

    protected int skillId;
    protected int skillLevel;

    private volatile AIEventLog eventLog;

    private AI2Scenario scenario;

    AbstractAI() {
        currentState = AIState.CREATED;
        currentSubState = AISubState.NONE;
        clearScenario();
    }

    public AI2Scenario getScenario() {
        return scenario;
    }

    public void setScenario(AI2Scenario scenario) {
        this.scenario = scenario;
    }

    public void clearScenario() {
        scenario = AI2Scenarios.NO_SCENARIO;
    }

    public AIEventLog getEventLog() {
        return eventLog;
    }

    @Override
    public AIState getState() {
        return currentState;
    }

    public final boolean isInState(AIState state) {
        return currentState == state;
    }

    @Override
    public AISubState getSubState() {
        return currentSubState;
    }

    public final boolean isInSubState(AISubState subState) {
        return currentSubState == subState;
    }

    @Override
    public String getName() {
        if (getClass().isAnnotationPresent(AIName.class)) {
            AIName annotation = getClass().getAnnotation(AIName.class);
            return annotation.value();
        }
        return "noname";
    }

    public int getSkillId() {
        return skillId;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    protected boolean canHandleEvent(AIEventType eventType) {
        switch (currentState) {
            case DESPAWNED:
                return StateEvents.DESPAWN_EVENTS.hasEvent(eventType);
            case DIED:
                return StateEvents.DEAD_EVENTS.hasEvent(eventType);
            case CREATED:
                return StateEvents.CREATED_EVENTS.hasEvent(eventType);
            default:
                break;
        }
        switch (eventType) {
            case DIALOG_START:
            case DIALOG_FINISH:
                return isNonFightingState();
            case CREATURE_NEEDS_SUPPORT:
                return getName().equals("trap") || currentState != AIState.FIGHT && isNonFightingState();
            default:
                break;
        }
        return true;
    }

    public boolean isNonFightingState() {
        return currentState == AIState.WALKING || currentState == AIState.IDLE;
    }

    public synchronized boolean setStateIfNot(AIState newState) {
        if (currentState == newState) {
            if (isLogging()) {
                AI2Logger.info(this, "Can't change state to " + newState + " from " + currentState);
            }
            return false;
        }
        if (isLogging()) {
            AI2Logger.info(this, "Setting AI state to " + newState);
            if (currentState == AIState.DIED && newState == AIState.FIGHT) {
                StackTraceElement[] stack = new Throwable().getStackTrace();
                for (StackTraceElement elem : stack) {
                    AI2Logger.info(this, elem.toString());
                }
            }
        }
        currentState = newState;
        return true;
    }

    public synchronized boolean setSubStateIfNot(AISubState newSubState) {
        if (currentSubState == newSubState) {
            if (isLogging()) {
                AI2Logger.info(this, "Can't change substate to " + newSubState + " from " + currentSubState);
            }
            return false;
        }
        if (isLogging()) {
            AI2Logger.info(this, "Setting AI substate to " + newSubState);
        }
        currentSubState = newSubState;
        return true;
    }

    @Override
    public void onGeneralEvent(AIEventType event) {
        if (canHandleEvent(event)) {
            if (isLogging()) {
                AI2Logger.info(this, "General event" + event);
            }
            handleGeneralEvent(event);
        }
    }

    @Override
    public void onCreatureEvent(AIEventType event, Creature creature) {
        Preconditions.checkNotNull(creature, "Creature must not be null");
        if (canHandleEvent(event)) {
            if (isLogging()) {
                AI2Logger.info(this, "Creature event " + event + ": " + creature.getObjectTemplate().getTemplateId());
            }
            handleCreatureEvent(event, creature);
        }
    }

    @Override
    public void onCustomEvent(int eventId, Object... args) {
        if (isLogging()) {
            AI2Logger.info(this, "Custom event - id = " + eventId);
        }
        handleCustomEvent(eventId, args);
    }

    /**
     * Will be hidden for all AI's below NpcAI2
     *
     * @return
     */
    public Creature getOwner() {
        return owner;
    }

    public int getObjectId() {
        return owner.getObjectId();
    }

    public WorldPosition getPosition() {
        return owner.getPosition();
    }

    public VisibleObject getTarget() {
        return owner.getTarget();
    }

    public boolean isAlreadyDead() {
        return owner.getLifeStats().isAlreadyDead();
    }

    void setOwner(Creature owner) {
        this.owner = owner;
    }

    public final boolean tryLockThink() {
        return thinkLock.tryLock();
    }

    public final void unlockThink() {
        thinkLock.unlock();
    }

    @Override
    public final boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    protected abstract void handleActivate();

    protected abstract void handleDeactivate();

    protected abstract void handleSpawned();

    protected abstract void handleRespawned();

    protected abstract void handleDespawned();

    protected abstract void handleDied();

    protected abstract void handleMoveValidate();

    protected abstract void handleMoveArrived();

    protected abstract void handleAttackComplete();

    protected abstract void handleFinishAttack();

    protected abstract void handleTargetReached();

    protected abstract void handleTargetTooFar();

    protected abstract void handleTargetGiveup();

    protected abstract void handleNotAtHome();

    protected abstract void handleBackHome();

    protected abstract void handleDropRegistered();

    protected abstract void handleAttack(Creature creature);

    protected abstract boolean handleCreatureNeedsSupport(Creature paramCreature);

    protected abstract boolean handleGuardAgainstAttacker(Creature paramCreature);

    protected abstract void handleCreatureSee(Creature paramCreature);

    protected abstract void handleCreatureNotSee(Creature paramCreature);

    protected abstract void handleCreatureMoved(Creature creature);

    protected abstract void handleCreatureAggro(Creature creature);

    protected abstract void handleTargetChanged(Creature creature);

    protected abstract void handleFollowMe(Creature creature);

    protected abstract void handleStopFollowMe(Creature creature);

    protected abstract void handleDialogStart(Player player);

    protected abstract void handleDialogFinish(Player player);

    protected abstract void handleCustomEvent(int eventId, Object... args);

    public abstract boolean onPatternShout(ShoutEventType paramShoutEventType, String paramString, int paramInt);

    protected void handleGeneralEvent(AIEventType event) {
        if (isLogging()) {
            AI2Logger.info(this, "Handle general event " + event);
        }
        logEvent(event);
        switch (event) {
            case MOVE_VALIDATE:
                handleMoveValidate();
                break;
            case MOVE_ARRIVED:
                handleMoveArrived();
                break;
            case SPAWNED:
                handleSpawned();
                break;
            case RESPAWNED:
                handleRespawned();
                break;
            case DESPAWNED:
                handleDespawned();
                break;
            case DIED:
                handleDied();
                break;
            case ATTACK_COMPLETE:
                handleAttackComplete();
                break;
            case ATTACK_FINISH:
                handleFinishAttack();
                break;
            case TARGET_REACHED:
                handleTargetReached();
                break;
            case TARGET_TOOFAR:
                handleTargetTooFar();
                break;
            case TARGET_GIVEUP:
                handleTargetGiveup();
                break;
            case NOT_AT_HOME:
                handleNotAtHome();
                break;
            case BACK_HOME:
                handleBackHome();
                break;
            case ACTIVATE:
                handleActivate();
                break;
            case DEACTIVATE:
                handleDeactivate();
                break;
            case FREEZE:
                FreezeEventHandler.onFreeze(this);
                break;
            case UNFREEZE:
                FreezeEventHandler.onUnfreeze(this);
                break;
            case DROP_REGISTERED:
                handleDropRegistered();
                break;
            default:
                break;
        }

        EventNotifier.GLOBAL.fire(OnHandleAIGeneralEvent.class, Tuple2.of(this, event));
    }

    /**
     * @param event
     */
    protected void logEvent(AIEventType event) {
        if (AIConfig.EVENT_DEBUG) {
            if (eventLog == null) {
                synchronized (this) {
                    if (eventLog == null) {
                        eventLog = new AIEventLog(10);
                    }
                }
            }
            eventLog.addFirst(event);
        }
    }

    void handleCreatureEvent(AIEventType event, Creature creature) {
        switch (event) {
            case ATTACK:
                if (DataManager.TRIBE_RELATIONS_DATA.isFriendlyRelation(getOwner().getTribe(), creature.getTribe())) {
                    return;
                }
                handleAttack(creature);
                logEvent(event);
                break;
            case CREATURE_NEEDS_SUPPORT:
                if (!handleCreatureNeedsSupport(creature) && creature.getTarget() instanceof Creature) {
                    if (!handleCreatureNeedsSupport((Creature) creature.getTarget()) && !handleGuardAgainstAttacker(creature)) {
                        handleGuardAgainstAttacker((Creature) creature.getTarget());
                    }
                }
                logEvent(event);
                break;
            case CREATURE_SEE:
                handleCreatureSee(creature);
                break;
            case CREATURE_NOT_SEE:
                handleCreatureNotSee(creature);
                break;
            case CREATURE_MOVED:
                handleCreatureMoved(creature);
                break;
            case CREATURE_AGGRO:
                handleCreatureAggro(creature);
                logEvent(event);
                break;
            case TARGET_CHANGED:
                handleTargetChanged(creature);
                break;
            case FOLLOW_ME:
                handleFollowMe(creature);
                logEvent(event);
                break;
            case STOP_FOLLOW_ME:
                handleStopFollowMe(creature);
                logEvent(event);
                break;
            case DIALOG_START:
                handleDialogStart((Player) creature);
                logEvent(event);
                break;
            case DIALOG_FINISH:
                handleDialogFinish((Player) creature);
                logEvent(event);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean poll(AIQuestion question) {
        AIAnswer instanceAnswer = pollInstance(question);
        if (instanceAnswer != null) {
            return instanceAnswer.isPositive();
        }
        switch (question) {
            case DESTINATION_REACHED:
                return isDestinationReached();
            case CAN_SPAWN_ON_DAYTIME_CHANGE:
                return isCanSpawnOnDaytimeChange();
            case CAN_SHOUT:
                return isMayShout();
            default:
                return false;
        }
    }

    /**
     * Poll concrete AI instance for the answer.
     *
     * @param question
     *
     * @return null if there is no specific answer
     */
    protected AIAnswer pollInstance(AIQuestion question) {
        return null;
    }

    @Override
    public AIAnswer ask(AIQuestion question) {
        return AIAnswers.NEGATIVE;
    }

    // TODO move to NPC ai
    protected boolean isDestinationReached() {
        AIState state = currentState;
        switch (state) {
            case FEAR:
                return MathUtil.isNearCoordinates(getOwner(), owner.getMoveController().getTargetX2(), owner.getMoveController().getTargetY2(), owner
                    .getMoveController().getTargetZ2(), 1);
            case FIGHT:
                return SimpleAttackManager.isTargetInAttackRange((Npc) owner);
            case RETURNING:
                SpawnTemplate spawn = getOwner().getSpawn();
                return MathUtil.isNearCoordinates(getOwner(), spawn.getX(), spawn.getY(), spawn.getZ(), 1);
            case FOLLOWING:
                return FollowEventHandler.isInRange(this, getOwner().getTarget());
            case WALKING:
                return currentSubState == AISubState.TALK || WalkManager.isArrivedAtPoint((NpcAI2) this);
            default:
                break;
        }
        return true;
    }

    protected boolean isCanSpawnOnDaytimeChange() {
        return currentState == AIState.DESPAWNED || currentState == AIState.CREATED;
    }

    public abstract boolean isMayShout();

    public abstract AttackIntention chooseAttackIntention();

    @Override
    public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
        return false;
    }

    @Override
    public long getRemainigTime() {
        return 0;
    }

    /**
     * Spawn object in the same world and instance as AI's owner
     */
    protected VisibleObject spawn(int npcId, float x, float y, float z, int heading) {
        return spawn(owner.getWorldId(), npcId, x, y, z, heading, 0, getPosition().getInstanceId());
    }

    /**
     * Spawn object with staticId in the same world and instance as AI's owner
     */
    protected VisibleObject spawn(int npcId, float x, float y, float z, int heading, int staticId) {
        return spawn(owner.getWorldId(), npcId, x, y, z, heading, staticId, getPosition().getInstanceId());
    }

    protected VisibleObject spawn(int worldId, int npcId, float x, float y, float z, int heading, int staticId,
                                  int instanceId) {
        SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(worldId, npcId, x, y, z, heading);
        template.setStaticId(staticId);
        return SpawnEngine.spawnObject(template, instanceId);
    }

    @Override
    public int modifyDamage(int damage) {
        return damage;
    }

    @Override
    public int modifyOwnerDamage(int damage) {
        return damage;
    }

    @Override
    public int modifyReflectedDamage(int reflectedDamage) {
        return reflectedDamage;
    }

    @Override
    public int modifyHealValue(int value) {
        return value;
    }
}
