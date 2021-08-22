/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers;

import com.ne.gs.controllers.attack.AttackStatus;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.summons.SummonMode;
import com.ne.gs.model.summons.UnsummonType;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.network.aion.serverpackets.SM_SUMMON_UPDATE;
import com.ne.gs.restrictions.RestrictionsManager;
import com.ne.gs.services.summons.SummonsService;
import com.ne.gs.skillengine.SkillEngine;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.taskmanager.tasks.PlayerMoveTaskManager;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author ATracer
 * @author RotO (Attack-speed hack protection) modified by Sippolo
 */
public class SummonController extends CreatureController<Summon> {

    private long lastAttackMilis = 0;
    private final boolean isAttacked = false;
    private int releaseAfterSkill = -1;

    @Override
    public void notSee(VisibleObject object, boolean isOutOfRange) {
        super.notSee(object, isOutOfRange);
        if (getOwner().getMaster() == null) {
            return;
        }

        if (object.getObjectId() == getOwner().getMaster().getObjectId()) {
            SummonsService.release(getOwner(), UnsummonType.DISTANCE, isAttacked);
        }
    }

    /**
     * Release summon
     */
    public void release(UnsummonType unsummonType) {
        SummonsService.release(getOwner(), unsummonType, isAttacked);
    }

    @Override
    public Summon getOwner() {
        return (Summon) super.getOwner();
    }

    /**
     * Change to rest mode
     */
    public void restMode() {
        SummonsService.restMode(getOwner());
    }

    public void setUnkMode() {
        SummonsService.setUnkMode(getOwner());
    }

    /**
     * Change to guard mode
     */
    public void guardMode() {
        SummonsService.guardMode(getOwner());
    }

    /**
     * Change to attackMode
     */
    public void attackMode(int targetObjId) {
        VisibleObject obj = getOwner().getKnownList().getObject(targetObjId);
        if (obj != null && obj instanceof Creature) {
            SummonsService.attackMode(getOwner());
        }
    }

    @Override
    public void attackTarget(Creature target, int time) {

        Player master = getOwner().getMaster();

        if (!RestrictionsManager.canAttack(master, target)) {
            return;
        }

        int attackSpeed = getOwner().getGameStats().getAttackSpeed().getCurrent();
        long milis = System.currentTimeMillis();
        if (milis - lastAttackMilis < attackSpeed) {
            /**
             * Hack!
             */
            return;
        }
        lastAttackMilis = milis;

        super.attackTarget(target, time);
    }

    @Override
    public void onAttack(Creature creature, int skillId, TYPE type, int damage, boolean notifyAttack, LOG log,  AttackStatus attackStatus) {
        if (getOwner().getLifeStats().isAlreadyDead()) {
            return;
        }

        // temp
        if (getOwner().getMode() == SummonMode.RELEASE) {
            return;
        }

        super.onAttack(creature, skillId, type, damage, notifyAttack, log, attackStatus);
        getOwner().getLifeStats().reduceHp(damage, creature);
        PacketSendUtility.broadcastPacket(getOwner(), new SM_ATTACK_STATUS(getOwner(), TYPE.REGULAR, 0, damage, log));
        getOwner().getMaster().sendPck(new SM_SUMMON_UPDATE(getOwner()));
    }

    @Override
    public void onDie(final Creature lastAttacker) {
        if (lastAttacker == null) {
            throw new NullPointerException("lastAttacker");
        }

        super.onDie(lastAttacker);
        SummonsService.release(getOwner(), UnsummonType.UNSPECIFIED, isAttacked);
        Summon owner = getOwner();
        final Player master = getOwner().getMaster();
        PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.DIE, 0, lastAttacker.equals(owner) ? 0 : lastAttacker.getObjectId()));

        if (!master.equals(lastAttacker) && !owner.equals(lastAttacker) && !master.getLifeStats().isAlreadyDead()
            && !lastAttacker.getLifeStats().isAlreadyDead()) {
            ThreadPoolManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    lastAttacker.getAggroList().addHate(master, 1);
                }
            }, 1000);
        }
    }

    public void useSkill(int skillId, Creature target) {
        Creature creature = getOwner();
        boolean petHasSkill = DataManager.PET_SKILL_DATA.petHasSkill(getOwner().getObjectTemplate().getTemplateId(), skillId);
        if (!petHasSkill) {
            // hackers!)
            return;
        }
        Skill skill = SkillEngine.getInstance().getSkill(creature, skillId, 1, target);
        if (skill != null) {
            // If skill succeeds, handle automatic release if expected
            if (skill.useSkill() && skillId == releaseAfterSkill) {
                ThreadPoolManager.getInstance().schedule(new Runnable() {

                    @Override
                    public void run() {
                        SummonsService.release(getOwner(), UnsummonType.UNSPECIFIED, isAttacked);
                    }
                }, 1000);
            }
            setReleaseAfterSkill(-1);
        }
    }

    /**
     * Handle automatic release if Ultra Skill demands it
     *
     * @param skillId
     *     is the skill commanded by summoner, after which pet is automatically dismissed
     */
    public void setReleaseAfterSkill(int skillId) {
        releaseAfterSkill = skillId;
    }

    @Override
    public void onStartMove() {
        super.onStartMove();
        getOwner().getMoveController().setInMove(true);
        getOwner().getObserveController().notifyMoveObservers();
        PlayerMoveTaskManager.addPlayer(getOwner());
    }

    @Override
    public void onStopMove() {
        super.onStopMove();
        PlayerMoveTaskManager.removePlayer(getOwner());
        getOwner().getObserveController().notifyMoveObservers();
        getOwner().getMoveController().setInMove(false);
    }

    @Override
    public void onMove() {
        getOwner().getObserveController().notifyMoveObservers();
        super.onMove();
    }

    protected Player getMaster() {
        return getOwner().getMaster();
    }
}
