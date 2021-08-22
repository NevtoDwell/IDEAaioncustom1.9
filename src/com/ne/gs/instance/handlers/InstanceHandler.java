/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.instance.handlers;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Gatherable;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.instance.StageType;
import com.ne.gs.model.instance.instancereward.InstanceReward;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.world.WorldMapInstance;
import com.ne.gs.world.knownlist.Visitor;
import com.ne.gs.world.zone.ZoneInstance;
import mw.utils.threading.Task;

/**
 * @author ATracer
 */
public interface InstanceHandler {

    /**
     * Executed during instance creation.<br>
     * This method will run after spawns are loaded
     *
     * @param instance
     *     created
     */
    void onInstanceCreate(WorldMapInstance instance);

    /**
     * Executed during instance destroy.<br>
     * This method will run after all spawns unloaded.<br>
     * All class-shared objects should be cleaned in handler
     */
    void onInstanceDestroy();

    void onPlayerLogin(Player player);

    void onPlayerLogOut(Player player);

    void onEnterInstance(Player player);

    void onLeaveInstance(Player player);

    void onOpenDoor(int door);

    void onEnterZone(Player player, ZoneInstance zone);

    void onLeaveZone(Player player, ZoneInstance zone);

    void onPlayMovieEnd(Player player, int movieId);

    boolean onReviveEvent(Player player);

    void onExitInstance(Player player);

    void doReward(Player player);

    boolean onDie(Player player, Creature lastAttacker);

    void onStopTraining(Player player);

    void onDie(Npc npc);

    void onChangeStage(StageType type);

    StageType getStage();

    void onDropRegistered(Npc npc);

    void onGather(Player player, Gatherable paramGatherable);

    InstanceReward<?> getInstanceReward();

    boolean onPassFlyingRing(Player player, String flyingRing);

    void handleUseItemFinish(Player player, Npc npcId);

    boolean canUseSkill(Player player, Skill skill);

    void doOnAllPlayers(Visitor<Player> action);

    void destroy();
    
    boolean isDuelDisabled();
}
