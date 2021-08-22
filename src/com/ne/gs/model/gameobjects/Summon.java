/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects;

import java.util.concurrent.Future;

import com.ne.gs.ai2.AI2Engine;
import com.ne.gs.controllers.CreatureController;
import com.ne.gs.controllers.SummonController;
import com.ne.gs.controllers.attack.AggroList;
import com.ne.gs.controllers.attack.PlayerAggroList;
import com.ne.gs.controllers.movement.SiegeWeaponMoveController;
import com.ne.gs.controllers.movement.SummonMoveController;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.Race;
import com.ne.gs.model.TribeClass;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.container.SummonGameStats;
import com.ne.gs.model.stats.container.SummonLifeStats;
import com.ne.gs.model.summons.SummonMode;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.model.templates.stats.SummonStatsTemplate;
import com.ne.gs.world.WorldPosition;

/**
 * @author ATracer
 */
public class Summon extends Creature {

    private Player master;
    private SummonMode mode = SummonMode.GUARD;
    private final byte level;
    private int liveTime = 0;
    private Future<?> releaseTask;

    /**
     * @param objId
     * @param controller
     * @param spawnTemplate
     * @param objectTemplate
     * @param level
     */
    public Summon(int objId, CreatureController<? extends Creature> controller, SpawnTemplate spawnTemplate,
                  NpcTemplate objectTemplate, byte level, int time) {
        super(objId, controller, spawnTemplate, objectTemplate, new WorldPosition());
        controller.setOwner(this);
        String ai = objectTemplate.getAi();
        AI2Engine.getInstance().setupAI(ai, this);
        moveController = (ai.equals("siege_weapon") ? new SiegeWeaponMoveController(this) : new SummonMoveController(this));
        this.level = level;
        liveTime = time;
        SummonStatsTemplate statsTemplate = DataManager.SUMMON_STATS_DATA.getSummonTemplate(objectTemplate.getTemplateId(), level);
        setGameStats(new SummonGameStats(this, statsTemplate));
        setLifeStats(new SummonLifeStats(this));
    }

    @Override
    protected AggroList createAggroList() {
        return new PlayerAggroList(this);
    }

    @Override
    public SummonGameStats getGameStats() {
        return (SummonGameStats) super.getGameStats();
    }

    @Override
    public Player getMaster() {
        return master;
    }

    /**
     * @param master
     *     the master to set
     */
    public void setMaster(Player master) {
        this.master = master;
    }

    @Override
    public String getName() {
        return objectTemplate.getName();
    }

    /**
     * @return the level
     */
    @Override
    public byte getLevel() {
        return level;
    }

    @Override
    public NpcTemplate getObjectTemplate() {
        return (NpcTemplate) super.getObjectTemplate();
    }

    public int getNpcId() {
        return getObjectTemplate().getTemplateId();
    }

    public int getNameId() {
        return getObjectTemplate().getNameId();
    }

    /**
     * @return NpcObjectType.SUMMON
     */
    @Override
    public NpcObjectType getNpcObjectType() {
        return NpcObjectType.SUMMON;
    }

    @Override
    public SummonController getController() {
        return (SummonController) super.getController();
    }

    /**
     * @return the mode
     */
    public SummonMode getMode() {
        return mode;
    }

    /**
     * @param mode
     *     the mode to set
     */
    public void setMode(SummonMode mode) {
        this.mode = mode;
    }

    @Override
    public boolean isEnemy(Creature creature) {
        return master != null && master.isEnemy(creature);
    }

    @Override
    public boolean isEnemyFrom(Npc npc) {
        return master != null && master.isEnemyFrom(npc);
    }

    @Override
    public boolean isEnemyFrom(Player player) {
        return master != null && master.isEnemyFrom(player);
    }

    @Override
    public TribeClass getTribe() {
        if (master == null) {
            return ((NpcTemplate) objectTemplate).getTribe();
        }
        return master.getTribe();
    }

    @Override
    public final boolean isAggroFrom(Npc npc) {
        return getMaster() != null && getMaster().isAggroFrom(npc);

    }

    @Override
    public SummonMoveController getMoveController() {
        return (SummonMoveController) super.getMoveController();
    }

    @Override
    public Creature getActingCreature() {
        return getMaster() == null ? this : getMaster();
    }

    @Override
    public Race getRace() {
        return getMaster() != null ? getMaster().getRace() : Race.NONE;
    }

    /**
     * @return liveTime in sec.
     */
    public int getLiveTime() {
        return liveTime;
    }

    /**
     * @param liveTime
     *     in sec.
     */
    public void setLiveTime(int liveTime) {
        this.liveTime = liveTime;
    }

    public void setReleaseTask(Future<?> task) {
        releaseTask = task;
    }

    public void cancelReleaseTask() {
        if (releaseTask != null && !releaseTask.isDone()) {
            releaseTask.cancel(true);
        }
    }
}
