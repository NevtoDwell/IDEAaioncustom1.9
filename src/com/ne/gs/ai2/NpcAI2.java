/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2;

import com.ne.gs.ai2.handler.ActivateEventHandler;
import com.ne.gs.ai2.handler.DiedEventHandler;
import com.ne.gs.ai2.handler.ShoutEventHandler;
import com.ne.gs.ai2.handler.SpawnEventHandler;
import com.ne.gs.ai2.poll.AIAnswer;
import com.ne.gs.ai2.poll.AIAnswers;
import com.ne.gs.ai2.poll.AIQuestion;
import com.ne.gs.ai2.poll.NpcAIPolls;
import com.ne.gs.configs.main.AIConfig;
import com.ne.gs.controllers.attack.AggroList;
import com.ne.gs.controllers.effect.EffectController;
import com.ne.gs.controllers.movement.NpcMoveController;
import com.ne.gs.model.Race;
import com.ne.gs.model.TribeClass;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.skill.NpcSkillList;
import com.ne.gs.model.stats.container.NpcLifeStats;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.world.knownlist.KnownList;

/**
 * @author ATracer
 */
@AIName("npc")
public class NpcAI2 extends AITemplate {

    @Override
    public Npc getOwner() {
        return (Npc) super.getOwner();
    }

    protected NpcTemplate getObjectTemplate() {
        return getOwner().getObjectTemplate();
    }

    protected SpawnTemplate getSpawnTemplate() {
        return getOwner().getSpawn();
    }

    protected NpcLifeStats getLifeStats() {
        return getOwner().getLifeStats();
    }

    protected Race getRace() {
        return getOwner().getRace();
    }

    protected TribeClass getTribe() {
        return getOwner().getTribe();
    }

    protected EffectController getEffectController() {
        return getOwner().getEffectController();
    }

    protected KnownList getKnownList() {
        return getOwner().getKnownList();
    }

    protected AggroList getAggroList() {
        return getOwner().getAggroList();
    }

    protected NpcSkillList getSkillList() {
        return getOwner().getSkillList();
    }

    protected VisibleObject getCreator() {
        return getOwner().getCreator();
    }

    /**
     * DEPRECATED as movements will be processed as commands only from ai
     */
    protected NpcMoveController getMoveController() {
        return getOwner().getMoveController();
    }

    protected int getNpcId() {
        return getOwner().getNpcId();
    }

    protected int getCreatorId() {
        return getOwner().getCreatorId();
    }

    protected boolean isInRange(VisibleObject object, int range) {
        return MathUtil.isIn3dRange(getOwner(), object, range);
    }

    @Override
    protected void handleActivate() {
        ActivateEventHandler.onActivate(this);
    }

    @Override
    protected void handleDeactivate() {
        ActivateEventHandler.onDeactivate(this);
    }

    @Override
    protected void handleSpawned() {
        SpawnEventHandler.onSpawn(this);
    }

    @Override
    protected void handleRespawned() {
        SpawnEventHandler.onRespawn(this);
    }

    @Override
    protected void handleDespawned() {
        if (poll(AIQuestion.CAN_SHOUT)) {
            ShoutEventHandler.onBeforeDespawn(this);
        }
        SpawnEventHandler.onDespawn(this);
    }

    @Override
    protected void handleDied() {
        DiedEventHandler.onSimpleDie(this);
    }

    @Override
    protected void handleMoveArrived() {
        if (!poll(AIQuestion.CAN_SHOUT) || getSpawnTemplate().getWalkerId() == null) {
            return;
        }
        ShoutEventHandler.onReachedWalkPoint(this);
    }

    @Override
    protected void handleTargetChanged(Creature creature) {
        super.handleMoveArrived();
        if (!poll(AIQuestion.CAN_SHOUT)) {
            return;
        }
        ShoutEventHandler.onSwitchedTarget(this, creature);
    }

    @Override
    protected AIAnswer pollInstance(AIQuestion question) {
        switch (question) {
            case SHOULD_DECAY:
                return NpcAIPolls.shouldDecay(this);
            case SHOULD_RESPAWN:
                return NpcAIPolls.shouldRespawn(this);
            case SHOULD_REWARD:
                return AIAnswers.POSITIVE;
            case CAN_SHOUT:
                return isMayShout() ? AIAnswers.POSITIVE : AIAnswers.NEGATIVE;
        }
        return null;
    }

    @Override
    public boolean isMayShout() {
        // temp fix, we shouldn't rely on it because of inheritance
        if (AIConfig.SHOUTS_ENABLE) {
            return getOwner().mayShout(0);
        }
        return false;
    }

    public boolean isMoveSupported() {
        return getOwner().getGameStats().getMovementSpeedFloat() > 0 && !isInSubState(AISubState.FREEZE);
    }

}
