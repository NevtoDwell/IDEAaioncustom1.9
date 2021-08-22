/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReferenceArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.Rnd;
import com.ne.gs.ai2.AI2;
import com.ne.gs.ai2.AISubState;
import com.ne.gs.ai2.NpcAI2;
import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.ai2.handler.ShoutEventHandler;
import com.ne.gs.ai2.poll.AIQuestion;
import com.ne.gs.controllers.attack.AttackResult;
import com.ne.gs.controllers.attack.AttackStatus;
import com.ne.gs.controllers.attack.AttackUtil;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.model.templates.item.ItemAttackType;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.network.aion.serverpackets.SM_MOVE;
import com.ne.gs.network.aion.serverpackets.SM_SKILL_CANCEL;
import com.ne.gs.skillengine.SkillEngine;
import com.ne.gs.skillengine.model.HealType;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.taskmanager.tasks.MovementNotifyTask;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.Visitor;
import com.ne.gs.world.zone.ZoneInstance;
import com.ne.gs.world.zone.ZoneUpdateService;

/**
 * This class is for controlling Creatures [npc's, players etc]
 *
 * @author -Nemesiss-, ATracer(2009-09-29), Sarynth, hex1r0
 * @modified by Wakizashi
 */
public abstract class CreatureController<T extends Creature> extends VisibleObjectController<Creature> {

    private static final Logger _log = LoggerFactory.getLogger(CreatureController.class);

    private final AtomicReferenceArray<Future<?>> _tasks = new AtomicReferenceArray<>(TaskId.values().length);

    private float healingSkillBoost = 1.0f;

    @Override
    public void notSee(VisibleObject object, boolean isOutOfRange) {
        super.notSee(object, isOutOfRange);
        if (object == getOwner().getTarget()) {
            getOwner().setTarget(null);
        }
    }

    /**
     * Perform tasks on Creature starting to move
     */
    public void onStartMove() {
        getOwner().getObserveController().notifyMoveObservers();
        notifyAIOnMove();
    }

    /**
     * Perform tasks on Creature move in progress
     */
    public void onMove() {
        notifyAIOnMove();
        updateZone();
    }

    /**
     * Perform tasks on Creature stop move
     */
    public void onStopMove() {
        notifyAIOnMove();
    }

    /**
     * Notify everyone in knownlist about move event
     */
    protected void notifyAIOnMove() {
        MovementNotifyTask.getInstance().add(getOwner());
    }

    /**
     * Refresh completely zone irrespective of the current zone
     */
    public void refreshZoneImpl() {
        getOwner().revalidateZones();
    }

    /**
     * Zone update mask management
     *
     */
    public final void updateZone() {
        ZoneUpdateService.getInstance().add(getOwner());
    }

    /**
     * Will be called by ZoneManager when creature enters specific zone
     *
     * @param zoneInstance
     */
    public void onEnterZone(ZoneInstance zoneInstance) {
    }

    /**
     * Will be called by ZoneManager when player leaves specific zone
     *
     * @param zoneInstance
     */
    public void onLeaveZone(ZoneInstance zoneInstance) {
    }

    /**
     * Perform tasks on Creature death
     *
     * @param lastAttacker
     */
    public void onDie(Creature lastAttacker) {
        getOwner().getMoveController().abortMove();
        getOwner().setCasting(null);
        getOwner().getEffectController().removeAllEffects();
        // exception for player
        if (getOwner() instanceof Player && ((Player) getOwner()).getIsFlyingBeforeDeath()) {
            getOwner().unsetState(CreatureState.ACTIVE);
            getOwner().setState(CreatureState.FLOATING_CORPSE);
        } else {
            getOwner().setState(CreatureState.DEAD);
        }
        getOwner().getObserveController().notifyDeathObservers(lastAttacker);
    }

    /**
     * Perform tasks when Creature was attacked //TODO may be pass only Skill
     * object - but need to add properties in it
     * @param creature
     * @param skillId
     * @param type
     * @param damage
     * @param notifyAttack
     * @param log
     * @param attackStatus
     */
    public void onAttack(Creature creature, int skillId, TYPE type, int damage, boolean notifyAttack, LOG log, AttackStatus attackStatus) {

        // Do NOT notify attacked observers if the damage is 0 and shield is up (means the attack has been absorbed)
        if (damage == 0 && getOwner().getEffectController().isUnderShield()) {
            notifyAttack = false;
        }
            //by enemy should fix killing players after duel
            if (creature != getOwner() && !getOwner().isEnemy(creature)) {
            return;
            }
        
        if (notifyAttack) {
            getOwner().getObserveController().notifyAttackedObservers(creature);    
        }
        if (damage > getOwner().getLifeStats().getCurrentHp()) {
            damage = getOwner().getLifeStats().getCurrentHp() + 1;
        }
        getOwner().getAggroList().addDamage(creature, damage);
        getOwner().getLifeStats().reduceHp(damage, creature);
        if(damage != 0){
            if(this.getOwner() instanceof Player){
                if(this.getOwner().isCasting()){
                    Player pl = (Player)this.getOwner();
                    float conc = pl.getGameStats().getStat(StatEnum.CONCENTRATION, 0).getCurrent();
                    conc += 400.0f + pl.getLevel();
                   if(Rnd.get(615)>conc){
                       cancelCurrentSkill();
                   }
                }
            }
        }
        
        if (getOwner() instanceof Npc) {
            AI2 ai = getOwner().getAi2();
            if (ai.poll(AIQuestion.CAN_SHOUT)) {
                if (creature instanceof Player) {
                    ShoutEventHandler.onHelp((NpcAI2) ai, creature);
                } else {
                    ShoutEventHandler.onEnemyAttack((NpcAI2) ai, creature);
                }
            }
        } else if (getOwner() instanceof Player && creature instanceof Npc) {
            AI2 ai = creature.getAi2();
            if (ai.poll(AIQuestion.CAN_SHOUT)) {
                ShoutEventHandler.onAttack((NpcAI2) ai, getOwner());
            }
        }
        getOwner().incrementAttackedCount();

        // notify all NPC's around that creature is attacking me
        getOwner().getKnownList().doOnAllNpcs(new Visitor<Npc>() {

            @Override
            public void visit(Npc object) {
                object.getAi2().onCreatureEvent(AIEventType.CREATURE_NEEDS_SUPPORT, getOwner());
            }
        });
    }

    /**
     * Perform tasks when Creature was attacked
     */
    public final void onAttack(Creature creature, int skillId, final int damage, boolean notifyAttack, AttackStatus attackStatus) {
        this.onAttack(creature, skillId, TYPE.REGULAR, damage, notifyAttack, LOG.REGULAR, attackStatus);
    }

    public final void onAttack(Creature creature, final int damage, boolean notifyAttack) {
        this.onAttack(creature, 0, TYPE.REGULAR, damage, notifyAttack, LOG.REGULAR, null);
    }

    public final void onAttack(Creature creature, final int damage, boolean notifyAttack, AttackStatus attackStatus) {
        this.onAttack(creature, 0, TYPE.REGULAR, damage, notifyAttack, LOG.REGULAR, attackStatus);
    }

    /**
     * @param hopType
     * @param value
     */
    public void onRestore(HealType hopType, int value) {
        switch (hopType) {
            case HP:
                getOwner().getLifeStats().increaseHp(TYPE.HP, value);
                break;
            case MP:
                getOwner().getLifeStats().increaseMp(TYPE.MP, value);
                break;
            case FP:
                getOwner().getLifeStats().increaseFp(TYPE.FP, value);
                break;
            default:
                break;
        }
    }

    /**
     * Perform reward operation
     */
    public void doReward() {
    }

    /**
     * This method should be overriden in more specific controllers
     *
     * @param player
     */
    public void onDialogRequest(Player player) {
    }

    /**
     * @param target
     * @param time
     */
    public void attackTarget(Creature target, int time) {
        /**
         * Check all prerequisites
         */
        if (target == null || !getOwner().canAttack() || getOwner().getLifeStats().isAlreadyDead() || !getOwner().isSpawned()) {
            return;
        }

        /**
         * Calculate and apply damage
         */
        int attackType = 0;
        List<AttackResult> attackResult;
        if (getOwner().getAttackType() == ItemAttackType.PHYSICAL) {
            attackResult = AttackUtil.calculatePhysicalAttackResult(getOwner(), target);
        } else {
            attackResult = AttackUtil.calculateMagicalAttackResult(getOwner(), target, getOwner().getAttackType().getMagicalElement());
            attackType = 1;
        }

        int damage = 0;
        for (AttackResult result : attackResult) {
            damage += result.getDamage();
        }
        AttackStatus firstAttackStatus = AttackStatus.getBaseStatus(attackResult.get(0).getAttackStatus());
        PacketSendUtility.broadcastPacketAndReceive(getOwner(), new SM_ATTACK(getOwner(), target, getOwner().getGameStats().getAttackCounter(), time,
                attackType, attackResult));

        getOwner().getGameStats().increaseAttackCounter();
        getOwner().getObserveController().notifyAttackObservers(target);

        Creature creature = getOwner();
        if (time == 0) {
            target.getController().onAttack(getOwner(), damage, true, firstAttackStatus);
        } else {
            ThreadPoolManager.getInstance().schedule(new DelayedOnAttack(target, creature, damage, firstAttackStatus), time);
        }
    }

    /**
     * Stops movements
     */
    public void stopMoving() {
        Creature owner = getOwner();
        World.getInstance().updatePosition(owner, owner.getX(), owner.getY(), owner.getZ(), owner.getHeading());
        PacketSendUtility.broadcastPacket(owner, new SM_MOVE(owner));
    }

    /**
     * Handle Dialog_Select
     *
     * @param dialogId
     * @param player
     * @param questId
     */
    public void onDialogSelect(int dialogId, Player player, int questId, int extendedRewardIndex) {
        // TODO Auto-generated method stub
    }

    public Future<?> getTask(TaskId taskId) {
        return _tasks.get(taskId.ordinal());
    }

    public boolean hasTask(TaskId taskId) {
        return getTask(taskId) != null;
    }

    public boolean hasScheduledTask(TaskId taskId) {
        Future<?> task = getTask(taskId);
        return task != null && !task.isDone();
    }

    public Future<?> cancelTask(TaskId taskId) {
        Future<?> task = _setTask(taskId, null);
        _cancelTask(task);
        return task;
    }

    /**
     * If task already exist - it will be canceled
     *
     * @param taskId
     * @param task
     */
    public void addTask(TaskId taskId, Future<?> task) {
        Future<?> prev = _setTask(taskId, task);
        _cancelTask(prev);
    }

    /**
     * Cancel all tasks associated with this controller (when deleting object)
     */
    public void cancelAllTasks() {
        for (TaskId taskId : TaskId.values()) {
            // FIXME should not be here
            if (taskId == TaskId.RESPAWN) {
                continue;
            }

            cancelTask(taskId);
        }
    }

    private void _cancelTask(Future<?> prev) {
        if (prev != null) {
            prev.cancel(false);
        }
    }

    private Future<?> _setTask(TaskId taskId, Future<?> task) {
        return _tasks.getAndSet(taskId.ordinal(), task);
    }

    @Override
    public void delete() {
        cancelAllTasks();
        super.delete();
    }

    /**
     * Die by reducing HP to 0
     */
    public void die() {
        getOwner().getLifeStats().reduceHp(getOwner().getLifeStats().getCurrentHp() + 1, getOwner());
    }

    /**
     * Use skill with default level 1
     */
    public final boolean useSkill(int skillId) {
        return useSkill(skillId, 1);
    }

    /**
     * @param skillId
     * @param skillLevel
     *
     * @return true if successful usage
     */
    public boolean useSkill(int skillId, int skillLevel) {
        try {
            Creature creature = getOwner();
            Skill skill = SkillEngine.getInstance().getSkill(creature, skillId, skillLevel, creature.getTarget());
            if (skill != null) {
                return skill.useSkill();
            }
        } catch (Exception ex) {
            _log.error("Exception during skill use: " + skillId, ex);
        }
        return false;
    }

    /**
     * Notify hate value to all visible creatures
     *
     * @param value
     */
    public void broadcastHate(int value) {
        for (VisibleObject visibleObject : getOwner().getKnownList().getKnownObjects().values()) {
            if (visibleObject instanceof Creature) {
                ((Creature) visibleObject).getAggroList().notifyHate(getOwner(), value);
            }
        }
    }

    public void abortCast() {
        Creature creature = getOwner();
        Skill skill = creature.getCastingSkill();
        if (skill == null) {
            return;
        }
        creature.setCasting(null);
        if (creature.getSkillNumber() > 0) {
            creature.setSkillNumber(creature.getSkillNumber() - 1);
        }
    }
    /**
     * Cancel current skill and remove cooldown
     */
    public void cancelCurrentSkill() {
        if (getOwner().getCastingSkill() == null) {
            return;
        }

        Creature creature = getOwner();
        Skill castingSkill = creature.getCastingSkill();
        castingSkill.cancelCast();
        creature.removeSkillCoolDown(castingSkill.getSkillTemplate().getCooldownId());
        creature.setCasting(null);
        PacketSendUtility.broadcastPacketAndReceive(creature, new SM_SKILL_CANCEL(creature, castingSkill.getSkillTemplate().getSkillId()));
        if (getOwner().getAi2() instanceof NpcAI2) {
            NpcAI2 npcAI = (NpcAI2) getOwner().getAi2();
            npcAI.setSubStateIfNot(AISubState.NONE);
            npcAI.onGeneralEvent(AIEventType.ATTACK_COMPLETE);
            if (creature.getSkillNumber() > 0) {
                creature.setSkillNumber(creature.getSkillNumber() - 1);
            }
        }
    }

    /**
     * Cancel use Item
     */
    public void cancelUseItem() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDespawn() {
        cancelTask(TaskId.DECAY);

        Creature owner = getOwner();
        if (owner == null || !owner.isSpawned()) {
            return;
        }
        owner.getAggroList().clear();
        owner.getObserveController().clear();
    }

    private static final class DelayedOnAttack implements Runnable {

    private Creature target;
    private Creature creature;
    private int finalDamage;
    private AttackStatus attackStatus;

    public DelayedOnAttack(Creature target, Creature creature, int finalDamage, AttackStatus attackStatus) {
      this.target = target;
      this.creature = creature;
      this.finalDamage = finalDamage;
      this.attackStatus = attackStatus;
    }

    @Override
    public void run() {
      target.getController().onAttack(creature, finalDamage, true, attackStatus);
      target = null;
      creature = null;
    }

  }

    public float getHealingSkillsBoost() {
        return healingSkillBoost;
    }

    public void setHealingSkillsBoost(float value) {
        this.healingSkillBoost = value;
    }

    @Override
    public void onAfterSpawn() {
        super.onAfterSpawn();
        getOwner().revalidateZones();
    }
}
