/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers;

import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.ai2.follow.FollowStartService;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.summons.UnsummonType;
import com.ne.gs.model.templates.npcskill.NpcSkillTemplates;

public class SiegeWeaponController extends SummonController {

    private final NpcSkillTemplates skills;

    public SiegeWeaponController(int npcId) {
        skills = DataManager.NPC_SKILL_DATA.getNpcSkillList(npcId);
    }

    @Override
    public void release(UnsummonType unsummonType) {
        getMaster().getController().cancelTask(TaskId.SUMMON_FOLLOW);
        getOwner().getMoveController().abortMove();
        super.release(unsummonType);
    }

    @Override
    public void restMode() {
        getMaster().getController().cancelTask(TaskId.SUMMON_FOLLOW);
        super.restMode();
        getOwner().getAi2().onCreatureEvent(AIEventType.STOP_FOLLOW_ME, getMaster());
    }

    @Override
    public void setUnkMode() {
        super.setUnkMode();
        getMaster().getController().cancelTask(TaskId.SUMMON_FOLLOW);
    }

    @Override
    public final void guardMode() {
        super.guardMode();
        getMaster().getController().cancelTask(TaskId.SUMMON_FOLLOW);
        getOwner().setTarget(getMaster());
        getOwner().getAi2().onCreatureEvent(AIEventType.FOLLOW_ME, getMaster());
        getOwner().getMoveController().moveToTargetObject();
        getMaster().getController().addTask(TaskId.SUMMON_FOLLOW, FollowStartService.newFollowingToTargetCheckTask(getOwner(), getMaster()));
    }

    @Override
    public void attackMode(int targetObjId) {
        super.attackMode(targetObjId);
        Creature target = (Creature) getOwner().getKnownList().getObject(targetObjId);
        if (target == null) {
            return;
        }
        getOwner().setTarget(target);
        getOwner().getAi2().onCreatureEvent(AIEventType.FOLLOW_ME, target);
        getOwner().getMoveController().moveToTargetObject();
        getMaster().getController().addTask(TaskId.SUMMON_FOLLOW, FollowStartService.newFollowingToTargetCheckTask(getOwner(), target));
    }

    @Override
    public void onDie(Creature lastAttacker) {
        getMaster().getController().cancelTask(TaskId.SUMMON_FOLLOW);
        super.onDie(lastAttacker);
    }

    public NpcSkillTemplates getNpcSkillTemplates() {
        return skills;
    }
}
