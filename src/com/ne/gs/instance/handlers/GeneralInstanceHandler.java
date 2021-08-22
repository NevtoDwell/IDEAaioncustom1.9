/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.instance.handlers;

import com.ne.commons.Sys;
import com.ne.commons.utils.concurrent.RunnableWrapper;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Gatherable;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.instance.StageType;
import com.ne.gs.model.instance.instancereward.InstanceReward;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.services.NpcShoutsService;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.WorldMapInstance;
import com.ne.gs.world.knownlist.Visitor;
import com.ne.gs.world.zone.ZoneInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;


/**
 * @author ATracer
 */
public class GeneralInstanceHandler implements InstanceHandler {

    private static final Logger _log = LoggerFactory.getLogger(GeneralInstanceHandler.class);

    protected final long creationTime;
    protected WorldMapInstance instance;
    protected int instanceId;
    protected Integer mapId;

    private boolean _destroyed = false;
    private List<Future<?>> _tasks = new ArrayList<>();

    public GeneralInstanceHandler() {
        creationTime = Sys.millis();
    }

    @Override
    public void onInstanceCreate(WorldMapInstance instance) {
        this.instance = instance;
        instanceId = instance.getInstanceId();
        mapId = instance.getMapId();
    }

    @Override
    public void onInstanceDestroy() {
    }

    @Override
    public void onPlayerLogin(Player player) {
    }

    @Override
    public void onPlayerLogOut(Player player) {
    }

    @Override
    public void onEnterInstance(Player player) {
    }

    @Override
    public void onLeaveInstance(Player player) {
    }

    @Override
    public void onOpenDoor(int door) {
    }

    @Override
    public void onEnterZone(Player player, ZoneInstance zone) {
    }

    @Override
    public void onLeaveZone(Player player, ZoneInstance zone) {
    }

    @Override
    public void onPlayMovieEnd(Player player, int movieId) {
    }

    @Override
    public boolean onReviveEvent(Player player) {
        return false;
    }

    @Override
    public boolean isDuelDisabled() {
        return false;
    }
    
    protected VisibleObject spawn(int npcId, float x, float y, float z, int heading) {
        SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(mapId, npcId, x, y, z, heading);
        return SpawnEngine.spawnObject(template, instanceId);
    }

    protected VisibleObject spawn(int npcId, float x, float y, float z, int heading, int staticId) {
        SpawnTemplate template = SpawnEngine.addNewSingleTimeSpawn(mapId, npcId, x, y, z, heading);
        template.setStaticId(staticId);
        return SpawnEngine.spawnObject(template, instanceId);
    }

    protected Npc getNpc(int npcId) {
        return instance.getNpc(npcId);
    }

    protected void sendMsg(int msg, int Obj, boolean isShout, int color) {
        sendMsg(msg, Obj, isShout, color, 0);
    }

    protected void sendMsg(int msg, int Obj, boolean isShout, int color, int time) {
        NpcShoutsService.getInstance().sendMsg(instance, msg, Obj, isShout, color, time);
    }

    protected void sendMsg(int msg) {
        sendMsg(msg, 0, false, 25);
    }

    @Override
    public void onExitInstance(Player player) {
    }

    @Override
    public void doReward(Player player) {
    }

    @Override
    public boolean onDie(Player player, Creature lastAttacker) {
        return false;
    }

    @Override
    public void onStopTraining(Player player) {
    }

    @Override
    public void onDie(Npc npc) {
    }

    @Override
    public void onChangeStage(StageType type) {
    }

    @Override
    public StageType getStage() {
        return StageType.DEFAULT;
    }

    @Override
    public void onDropRegistered(Npc npc) {
    }

    @Override
    public void onGather(Player player, Gatherable gatherable) {
    }

    @Override
    public InstanceReward<?> getInstanceReward() {
        return null;
    }

    @Override
    public boolean onPassFlyingRing(Player player, String flyingRing) {
        return false;
    }

    @Override
    public void handleUseItemFinish(Player player, Npc npc) {
    }

    @Override
    public boolean canUseSkill(Player player, Skill skill) {
        return true;
    }

    public void doOnAllPlayers(Visitor<Player> action) {
        onRunning(i -> i.doOnAllPlayers(action));
    }

    @Override
    public final void destroy() {
        try {
            onInstanceDestroy();

            for (Future<?> task : _tasks) {
                if (task != null && !task.isCancelled()) {
                    task.cancel(false);
                }
            }

            _tasks.clear();
        } catch (Throwable t) {
            _log.error("Exception while destroying instance", t);
        } finally {
            _destroyed = true;
        }
    }

    protected /*final*/ ScheduledFuture<?> schedule(Runnable task, long delay) {
        RunnableWrapper wrapper = new RunnableWrapper(task);
        ScheduledFuture<?> future = ThreadPoolManager.getInstance().schedule(wrapper, delay);
        wrapper.setFuture(future);

        _tasks.add(future);

        return future;
    }

    protected final ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long delay, long period) {
        RunnableWrapper wrapper = new RunnableWrapper(task);
        ScheduledFuture<?> future = ThreadPoolManager.getInstance().scheduleAtFixedRate(wrapper, delay, period);
        wrapper.setFuture(future);

        _tasks.add(future);

        return future;
    }
    private void onRunning(Consumer<WorldMapInstance> func) {
        if (!_destroyed)
            func.accept(instance);
    }

    private final class RunnableWrapper implements Runnable {
        private final Runnable _task;
        private ScheduledFuture<?> _future;

        private RunnableWrapper(Runnable task) {
            _task = task;
        }

        public void setFuture(ScheduledFuture<?> future) {
            _future = future;
        }

        @Override
        public void run() {
            if (_destroyed) {

                if (_future != null && !_future.isCancelled()) {
                    _future.cancel(false);
                }

                _log.debug("Task {} was canceled because tried to run after instance was destroyed", _task);
            } else {
                _task.run();
            }
        }
    }

}
