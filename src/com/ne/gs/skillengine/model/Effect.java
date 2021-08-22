/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.model;

import java.util.*;
import java.util.concurrent.Future;

import com.ne.commons.annotations.Nullable;
import com.ne.gs.dataholders.DataManager;
import javolution.util.FastMap;

import com.ne.commons.utils.Rnd;
import com.ne.gs.controllers.attack.AttackStatus;
import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.AttackCalcObserver;
import com.ne.gs.controllers.observer.ObserverType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.calc.StatOwner;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.network.aion.serverpackets.SM_PLAYER_STANCE;
import com.ne.gs.network.aion.serverpackets.SM_SKILL_ACTIVATION;
import com.ne.gs.services.custom.ItemTimeTuningService;
import com.ne.gs.skillengine.condition.Conditions;
import com.ne.gs.skillengine.effect.DamageEffect;
import com.ne.gs.skillengine.effect.DelayedSpellAttackInstantEffect;
import com.ne.gs.skillengine.effect.EffectTemplate;
import com.ne.gs.skillengine.effect.EffectType;
import com.ne.gs.skillengine.effect.Effects;
import com.ne.gs.skillengine.effect.FearEffect;
import com.ne.gs.skillengine.effect.PetOrderUseUltraSkillEffect;
import com.ne.gs.skillengine.effect.SummonEffect;
import com.ne.gs.skillengine.effect.TransformEffect;
import com.ne.gs.skillengine.periodicaction.PeriodicAction;
import com.ne.gs.skillengine.periodicaction.PeriodicActions;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author ATracer
 * @modified by Wakizashi
 * @modified by Sippolo
 * @modified by kecimis
 */
public class Effect implements StatOwner {

    private Skill skill;
    private SkillTemplate skillTemplate;
    private int skillLevel;
    private int duration;
    private long endTime;
    private PeriodicActions periodicActions;
    private SkillMoveType skillMoveType = SkillMoveType.DEFAULT;
    private Creature effected;
    private Creature effector;
    private Future<?> task = null;
    private Future<?>[] periodicTasks = null;
    private Future<?> periodicActionsTask = null;

    private float targetX = 0;
    private float targetY = 0;
    private float targetZ = 0;
    /**
     * Used for damage/heal values
     */
    private int reserved1;
    /**
     * Used for shield total hit damage;
     */
    private int reserved2;
    /**
     * Used for shield hit damage
     */
    private int reserved3;
    /**
     * Used for tick heals from HoT's (Heal Over Time)
     */
    private int reserved4;
    /**
     * Used for tick damages from DoT's (Damage Over Time)
     */
    private int reserved5;

    private int[] reservedInts;

    /**
     * Spell Status 1 : stumble 2 : knockback 4 : open aerial 8 : close aerial 16 : spin 32 : block 64 : parry 128 : dodge
     * 256 : resist
     */
    private SpellStatus spellStatus = SpellStatus.NONE;
    private DashStatus dashStatus = DashStatus.NONE;

    private AttackStatus attackStatus = AttackStatus.NORMALHIT;

    /**
     * shield effects related
     */
    private int shieldDefense;
    private int reflectedDamage = 0;
    private int reflectedSkillId = 0;
    private int protectedSkillId = 0;
    private int protectedDamage = 0;
    private int protectorId = 0;

    private boolean addedToController;
    private AttackCalcObserver[] attackStatusObserver;

    private AttackCalcObserver[] attackShieldObserver;

    private boolean launchSubEffect = true;
    private Effect subEffect;

    private boolean isStopped;

    private boolean isDelayedDamage;

    private boolean isDamageEffect;
    private boolean isPetOrder;
    private boolean isSummoning;
    private boolean isXpBoost;

    private boolean isCancelOnDmg;

    private boolean subEffectAbortedBySubConditions;

    private ItemTemplate itemTemplate;
    private boolean isNoDeathPenalty;
    private boolean isNoResurrectPenalty;
    private boolean isHiPass;
    
    
    private boolean isSkipAfter;

    private boolean isPhysicalState = false;
    private boolean isMagicalState = false;
    /**
     * Hate that will be placed on effected list
     */
    private int tauntHate;
    /**
     * Total hate that will be broadcasted
     */
    private int effectHate;

    private Map<Integer, EffectTemplate> successEffects = new FastMap<Integer, EffectTemplate>().shared();

    private int carvedSignet = 0;

    private int signetBurstedCount = 0;

    protected int abnormals;
	
    private EffectResult effectResult = EffectResult.NORMAL;
    /**
     * Action observer that should be removed after effect end
     */
    private final ActionObserver[] actionObserver = new ActionObserver[4];

    float x, y, z;
    int worldId, instanceId;

    /**
     * used to force duration, you should be very careful when to use it
     */
    private boolean forcedDuration = false;
    private boolean isForcedEffect = false;
    /**
     * power of effect ( used for dispels)
     */
    private int power = 10;

    private int accModBoost = 0;

    public final Skill getSkill() {
        return skill;
    }

    public void setAbnormal(int mask) {
        abnormals |= mask;
    }

    public int getAbnormals() {
        return abnormals;
    }

    public Effect(Creature effector, Creature effected, SkillTemplate skillTemplate, int skillLevel, int duration) {
        this.effector = effector;
        this.effected = effected;
        this.skillTemplate = skillTemplate;
        this.skillLevel = skillLevel;
        this.duration = duration;
        this.periodicActions = skillTemplate.getPeriodicActions();

        power = initializePower(skillTemplate);
    }

    public Effect(Creature effector, Creature effected, SkillTemplate skillTemplate, int skillLevel, int duration, ItemTemplate itemTemplate) {
        this(effector, effected, skillTemplate, skillLevel, duration);
        this.itemTemplate = itemTemplate;
        ItemTimeTuningService.getInstance().onItemEffectCreate(this);
    }

    public Effect(Skill skill, Creature effected, int duration, ItemTemplate itemTemplate) {
        this(skill.getEffector(), effected, skill.getSkillTemplate(), skill.getSkillLevel(), duration, itemTemplate);
        this.skill = skill;
    }

    public void setWorldPosition(int worldId, int instanceId, float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldId = worldId;
        this.instanceId = instanceId;
    }

    public int getEffectorId() {
        return effector.getObjectId();
    }

    public int getSkillId() {
        return skillTemplate.getSkillId();
    }

    public String getSkillName() {
        return skillTemplate.getName();
    }

    public final SkillTemplate getSkillTemplate() {
        return skillTemplate;
    }

    public SkillSubType getSkillSubType() {
        return skillTemplate.getSubType();
    }

    public int getSkillSetException() {
        return skillTemplate.getSkillSetException();
    }

    public int getSkillSetMaxOccur() {
        return skillTemplate.getSkillSetMaxOccur();
    }

    public String getStack() {
        return skillTemplate.getStack();
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public int getSkillStackLvl() {
        return skillTemplate.getLvl();
    }

    public SkillType getSkillType() {
        return skillTemplate.getType();
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int newDuration) {
        this.duration = newDuration;
    }

    public Creature getEffected() {
        return effected;
    }

    public Creature getEffector() {
        return effector;
    }

    public boolean isPassive() {
        return skillTemplate.isPassive();
    }

    public void setTask(Future<?> task) {
        this.task = task;
    }

    public Future<?> getPeriodicTask(int i) {
        return periodicTasks[i - 1];
    }

    public void setPeriodicTask(Future<?> periodicTask, int i) {
        if (periodicTasks == null) {
            periodicTasks = new Future<?>[4];
        }
        this.periodicTasks[i - 1] = periodicTask;
    }

    public int getReserved1() {
        return reserved1;
    }

    public void setReserved1(int reserved1) {
        this.reserved1 = reserved1;
    }

    public int getReserved2() {
        return reserved2;
    }

    public void setReserved2(int reserved2) {
        this.reserved2 = reserved2;
    }

    public int getReserved3() {
        return reserved3;
    }

    public void setReserved3(int reserved3) {
        this.reserved3 = reserved3;
    }

    public int getReserved4() {
        return reserved4;
    }

    public void setReserved4(int reserved4) {
        this.reserved4 = reserved4;
    }

    public int getReserved5() {
        return reserved5;
    }

    public void setReserved5(int reserved5) {
        this.reserved5 = reserved5;
    }

    public AttackStatus getAttackStatus() {
        return attackStatus;
    }

    public void setAttackStatus(AttackStatus attackStatus) {
        this.attackStatus = attackStatus;
    }

    public List<EffectTemplate> getEffectTemplates() {
        return skillTemplate.getEffects().getEffects();
    }

    public boolean isMphealInstant() {
        Effects effects = skillTemplate.getEffects();
        return effects != null && effects.isMpHealInstant();
    }

    public boolean isToggle() {
        return skillTemplate.getActivationAttribute() == ActivationAttribute.TOGGLE;
    }

    public boolean isChant() {
        return skillTemplate.getTargetSlot() == SkillTargetSlot.CHANT;
    }

    public int getTargetSlot() {
        return skillTemplate.getTargetSlot().ordinal();
    }

    public SkillTargetSlot getTargetSlotEnum() {
        return skillTemplate.getTargetSlot();
    }

    public int getTargetSlotLevel() {
        return skillTemplate.getTargetSlotLevel();
    }

    public DispelCategoryType getDispelCategory() {
        return skillTemplate.getDispelCategory();
    }

    public int getReqDispelLevel() {
        return skillTemplate.getReqDispelLevel();
    }

    public AttackCalcObserver getAttackStatusObserver(int i) {
        return attackStatusObserver != null ? attackStatusObserver[i - 1] : null;
    }

    public void setAttackStatusObserver(AttackCalcObserver attackStatusObserver, int i) {
        if (this.attackStatusObserver == null) {
            this.attackStatusObserver = new AttackCalcObserver[4];
        }
        this.attackStatusObserver[i - 1] = attackStatusObserver;
    }

    public @Nullable AttackCalcObserver getAttackShieldObserver(int i) {
        return attackShieldObserver != null ? attackShieldObserver[i - 1] : null;
    }

    public void setAttackShieldObserver(AttackCalcObserver attackShieldObserver, int i) {
        if (this.attackShieldObserver == null) {
            this.attackShieldObserver = new AttackCalcObserver[4];
        }
        this.attackShieldObserver[i - 1] = attackShieldObserver;
    }

    public int getReservedInt(int i) {
        return reservedInts != null ? reservedInts[i - 1] : 0;
    }

    public void setReservedInt(int i, int value) {
        if (this.reservedInts == null) {
            this.reservedInts = new int[4];
        }
        this.reservedInts[i - 1] = value;
    }

    public boolean isLaunchSubEffect() {
        return launchSubEffect;
    }

    public void setLaunchSubEffect(boolean launchSubEffect) {
        this.launchSubEffect = launchSubEffect;
    }
	
    public final EffectResult getEffectResult() {
        return effectResult;
    }

    public int getShieldDefense() {
        return shieldDefense;
    }

    public void setShieldDefense(int shieldDefense) {
        this.shieldDefense = shieldDefense;
    }

    public int getReflectedDamage() {
        return this.reflectedDamage;
    }

    public void setReflectedDamage(int value) {
        this.reflectedDamage = value;
    }

    public int getReflectedSkillId() {
        return this.reflectedSkillId;
    }

    public void setReflectedSkillId(int value) {
        this.reflectedSkillId = value;
    }

    public int getProtectedSkillId() {
        return this.protectedSkillId;
    }

    public void setProtectedSkillId(int skillId) {
        this.protectedSkillId = skillId;
    }

    public int getProtectedDamage() {
        return this.protectedDamage;
    }

    public void setProtectedDamage(int protectedDamage) {
        this.protectedDamage = protectedDamage;
    }

    public int getProtectorId() {
        return this.protectorId;
    }

    public void setProtectorId(int protectorId) {
        this.protectorId = protectorId;
    }

    public SpellStatus getSpellStatus() {
        return spellStatus;
    }

    public void setSpellStatus(SpellStatus spellStatus) {
        this.spellStatus = spellStatus;
    }

    public DashStatus getDashStatus() {
        return dashStatus;
    }

    public void setDashStatus(DashStatus dashStatus) {
        this.dashStatus = dashStatus;
    }

    /**
     * Number of signets carved on target
     *
     * @return
     */
    public int getCarvedSignet() {
        return this.carvedSignet;
    }

    public void setCarvedSignet(int value) {
        this.carvedSignet = value;
    }

    public Effect getSubEffect() {
        return subEffect;
    }

    public void setSubEffect(Effect subEffect) {
        this.subEffect = subEffect;
    }

    public Collection<EffectTemplate> getSuccessSubEffects() {
        if (getSubEffect() != null) {
            return getSubEffect().getSuccessEffects();
        }
        return Collections.emptyList();
    }

    public boolean containsEffectId(int effectId) {
        for (EffectTemplate template : successEffects.values()) {
            if (template.getEffectid() == effectId) {
                return true;
            }
        }
        return false;
    }

    public TransformType getTransformType() {
        for (EffectTemplate et : skillTemplate.getEffects().getEffects()) {
            if (et instanceof TransformEffect) {
                return ((TransformEffect) et).getTransformType();
            }
        }
        return TransformType.NONE;
    }

    public void setForcedDuration(boolean forcedDuration) {
        this.forcedDuration = forcedDuration;
    }

    public void setIsForcedEffect(boolean isForcedEffect) {
        this.isForcedEffect = isForcedEffect;
    }

    public boolean getIsForcedEffect() {
        return isForcedEffect || DataManager.MATERIAL_DATA.isMaterialSkill(this.getSkillId());
    }

    /**
     * Correct lifecycle of Effect - INITIALIZE - APPLY - START - END
     */

    /**
     * Do initialization with proper calculations
     */
    public void initialize() {
        if (skillTemplate.getEffects() == null) {
            return;
        }

        for (EffectTemplate template : getEffectTemplates()) {
            template.calculate(this);

            if (template instanceof DelayedSpellAttackInstantEffect) {
                setDelayedDamage(true);
            }
            if ((template instanceof PetOrderUseUltraSkillEffect)) {
                setPetOrder(true);
            }
            if ((template instanceof SummonEffect)) {
                setSumonning(true);
            }
            if (template instanceof DamageEffect) {
                setDamageEffect(true);
            }
        }

        for (EffectTemplate template : getEffectTemplates()) {
            template.calculateHate(this);
        }
        if (this.isLaunchSubEffect()) {
            for (EffectTemplate template : successEffects.values()) {
                template.calculateSubEffect(this);
            }
        }

        if (successEffects.isEmpty()) {
            skillMoveType = SkillMoveType.RESIST;
            if (getSkillType() == SkillType.PHYSICAL) {
                if (getAttackStatus() == AttackStatus.CRITICAL) {
                    setAttackStatus(AttackStatus.CRITICAL_DODGE);
                } else {
                    setAttackStatus(AttackStatus.DODGE);
                }
            } else {
                if (getAttackStatus() == AttackStatus.CRITICAL) {
                    setAttackStatus(AttackStatus.CRITICAL_RESIST);// TODO recheck
                } else {
                    setAttackStatus(AttackStatus.RESIST);
                }
            }
        }

        // set spellstatus for sm_castspell_end packet
        switch (AttackStatus.getBaseStatus(getAttackStatus())) {
            case DODGE:
                setSpellStatus(SpellStatus.DODGE);
                break;
            case PARRY:
                if (getSpellStatus() == SpellStatus.NONE) {
                    setSpellStatus(SpellStatus.PARRY);
                }
                break;
            case BLOCK:
                if (getSpellStatus() == SpellStatus.NONE) {
                    setSpellStatus(SpellStatus.BLOCK);
                }
                break;
            case RESIST:
                setSpellStatus(SpellStatus.RESIST);
                break;
        }
    }

    /**
     * Apply all effect templates
     */
    public void applyEffect() {
        /**
         * broadcast final hate to all visible objects
         */
        // TODO hostile_type?
        if (effectHate != 0) {
            if (((getEffected() instanceof Npc)) && (!isDelayedDamage()) && (!isPetOrder()) && (!isSummoning())) {
                getEffected().getAggroList().addHate(effector, 1);
            }
            effector.getController().broadcastHate(effectHate);
        }

        if ((skillTemplate.getEffects() == null) || (successEffects.isEmpty())) {
            return;
        }

        for (EffectTemplate template : successEffects.values()) {
            if (getEffected() != null) {
                if (getEffected().getLifeStats().isAlreadyDead() && !skillTemplate.hasResurrectEffect()) {
                    continue;
                }
            }
            template.applyEffect(this);
            template.startSubEffect(this);
        }
    }

    /**
     * Start effect which includes:
     * - start effect defined in template
     * - start subeffect if possible
     * - activate toogle skill if needed
     * - schedule end of effect
     */
    public void startEffect(boolean restored) {
        if (successEffects.isEmpty()) {
            return;
        }
        shedulePeriodicActions();

        for (EffectTemplate template : successEffects.values()) {
            template.startEffect(this);
            checkUseEquipmentConditions();
            checkCancelOnDmg();
        }

        if (isToggle() && effector instanceof Player) {
            activateToggleSkill();
        }
        if (!restored && !forcedDuration) {
            duration = getEffectsDuration();
        }
        if (duration == 0) {
            return;
        }
        endTime = System.currentTimeMillis() + duration;

        task = ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                endEffect();
            }
        }, duration);
    }

    /**
     * Will activate toggle skill and start checking task
     */
    private void activateToggleSkill() {
        ((Player) effector).sendPck(new SM_SKILL_ACTIVATION(getSkillId(), true));
    }

    /**
     * Will deactivate toggle skill and stop checking task
     */
    private void deactivateToggleSkill() {
        ((Player) effector).sendPck(new SM_SKILL_ACTIVATION(getSkillId(), false));
    }

    /**
     * End effect and all effect actions This method is synchronized and prevented to be called several times which could
     * cause unexpected behavior
     */
    public synchronized void endEffect() {
        if (isStopped) {
            return;
        }

        for (EffectTemplate template : successEffects.values()) {
            template.endEffect(this);
        }

        // if effect is a stance, remove stance from player
        if (effector instanceof Player) {
            Player player = (Player) effector;
            if (player.getController().getStanceSkillId() == getSkillId()) {
                player.sendPck(new SM_PLAYER_STANCE(player, 0));
                player.getController().startStance(0);
            }
        }

        // TODO better way to finish
        if (getSkillTemplate().getTargetSlot() == SkillTargetSlot.SPEC2) {
            getEffected().getLifeStats().increaseHp(TYPE.HP, (int) (getEffected().getLifeStats().getMaxHp() * 0.2));
            getEffected().getLifeStats().increaseMp(TYPE.MP, (int) (getEffected().getLifeStats().getMaxMp() * 0.2));
        }

        if (isToggle() && effector instanceof Player) {
            deactivateToggleSkill();
        }
        stopTasks();
        effected.getEffectController().clearEffect(this);
        this.isStopped = true;
    }

    /**
     * Stop all scheduled tasks
     */
    public void stopTasks() {
        if (task != null) {
            task.cancel(false);
            task = null;
        }

        if (periodicTasks != null) {
            for (int i = 0; i < periodicTasks.length; i++) {
                Future<?> t = periodicTasks[i];
                if (t != null) {
                    t.cancel(false);
                    periodicTasks[i] = null;
                }
            }
        }

        stopPeriodicActions();
    }

    public int getRemainingTime() {
        return this.getDuration() >= 86400000 ? -1 : (int) (endTime - System.currentTimeMillis());
    }

    public long getEndTime() {
        return endTime;
    }

    public int getPvpDamage() {
        return skillTemplate.getPvpDamage();
    }

    public ItemTemplate getItemTemplate() {
        return itemTemplate;
    }

    public void addToEffectedController() {
        if ((!addedToController) && (effected.getLifeStats() != null) && (!effected.getLifeStats().isAlreadyDead())) {
            effected.getEffectController().addEffect(this);
            addedToController = true;
        }
    }

    public int getEffectHate() {
        return effectHate;
    }

    public void setEffectHate(int effectHate) {
        this.effectHate = effectHate;
    }

    public int getTauntHate() {
        return tauntHate;
    }

    public void setTauntHate(int tauntHate) {
        this.tauntHate = tauntHate;
    }

    public ActionObserver getActionObserver(int i) {
        return actionObserver[i - 1];
    }

    public void setActionObserver(ActionObserver observer, int i) {
        actionObserver[i - 1] = observer;
    }

    public void addSucessEffect(EffectTemplate effect) {
        successEffects.put(effect.getPosition(), effect);
    }

    public boolean isInSuccessEffects(int position) {
        return successEffects.get(position) != null;
    }

    public Collection<EffectTemplate> getSuccessEffects() {
        return successEffects.values();
    }

    public void addAllEffectToSucess() {
        successEffects.clear();
        for (EffectTemplate template : getEffectTemplates()) {
            successEffects.put(template.getPosition(), template);
        }
    }

    public void clearSucessEffects() {
        successEffects.clear();
    }

    private void shedulePeriodicActions() {
        if (periodicActions == null || periodicActions.getPeriodicActions() == null || periodicActions.getPeriodicActions().isEmpty()) {
            return;
        }
        int checktime = periodicActions.getChecktime();
        periodicActionsTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                for (PeriodicAction action : periodicActions.getPeriodicActions()) {
                    action.act(Effect.this);
                }
            }
        }, 0, checktime);
    }

    private void stopPeriodicActions() {
        if (periodicActionsTask != null) {
            periodicActionsTask.cancel(false);
            periodicActionsTask = null;
        }
    }

    public int getEffectsDuration() {
        int duration = 0;
        Iterator<EffectTemplate> itr = this.successEffects.values().iterator();
        while (itr.hasNext() && duration == 0) {
            EffectTemplate et = itr.next();
            int effectDuration = et.getDuration2() + et.getDuration1() * getSkillLevel();
            if (et.getRandomTime() > 0) {
                effectDuration -= Rnd.get(et.getRandomTime());
            }
            duration = duration > effectDuration ? duration : effectDuration;
        }
        // adjust with pvp duration
        switch (skillTemplate.getSubType()) {
            case BUFF:
                duration = effector.getGameStats().getStat(StatEnum.BOOST_DURATION_BUFF, duration).getCurrent();
        }
        if (effected instanceof Player && skillTemplate.getPvpDuration() != 0) {
            duration = duration * skillTemplate.getPvpDuration() / 100;
        }

        if (duration > 86400000) {
            duration = 86400000;
        }
        return duration;
    }

    public boolean isDeityAvatar() {
        return skillTemplate.isDeityAvatar();
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public int getWorldId() {
        return worldId;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public SkillMoveType getSkillMoveType() {
        return skillMoveType;
    }

    public void setSkillMoveType(SkillMoveType skillMoveType) {
        this.skillMoveType = skillMoveType;
    }

    public float getTargetX() {
        return targetX;
    }

    public float getTargetY() {
        return targetY;
    }

    public float getTargetZ() {
        return targetZ;
    }

    public void setTargetLoc(float x, float y, float z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
    }

    public void setSubEffectAborted(boolean value) {
        this.subEffectAbortedBySubConditions = value;
    }

    public boolean isSubEffectAbortedBySubConditions() {
        return this.subEffectAbortedBySubConditions;
    }

    public void setXpBoost(boolean value) {
        this.isXpBoost = value;
    }

    public boolean isXpBoost() {
        return this.isXpBoost;
    }

    public void setNoDeathPenalty(boolean value) {
        isNoDeathPenalty = value;
    }

    public boolean isNoDeathPenalty() {
        return isNoDeathPenalty;
    }

    public void setNoResurrectPenalty(boolean value) {
        isNoResurrectPenalty = value;
    }

    public boolean isNoResurrectPenalty() {
        return isNoResurrectPenalty;
    }

    public void setHiPass(boolean value) {
        isHiPass = value;
    }

    public boolean isHiPass() {
        return isHiPass;
    }
    
    public void setSkipAfter(boolean value) {
        this.isSkipAfter = value;
    }

    public boolean isSkipAfter() {
        return this.isSkipAfter;
    }

    /**
     * Check all in use equipment conditions
     *
     * @return true if all conditions have been satisfied
     */
    private boolean useEquipmentConditionsCheck() {
        Conditions useEquipConditions = skillTemplate.getUseEquipmentconditions();
        return useEquipConditions == null || useEquipConditions.validate(this);
    }

    /**
     * Check use equipment conditions by adding Unequip observer if needed
     */
    private void checkUseEquipmentConditions() {
        // If skill has use equipment conditions
        // Observe for unequip event and remove effect if event occurs
        if ((getSkillTemplate().getUseEquipmentconditions() != null) && (getSkillTemplate().getUseEquipmentconditions().getConditions().size() > 0)) {
            ActionObserver observer = new ActionObserver(ObserverType.UNEQUIP) {

                @Override
                public void unequip(Item item, Player owner) {
                    if (!useEquipmentConditionsCheck()) {
                        endEffect();
                        effected.getObserveController().removeObserver(this);
                    }
                }
            };
            effected.getObserveController().addObserver(observer);
        }
    }

    /**
     * Add Attacked/Dot_Attacked observers if this effect needs to be removed on damage received by effected
     */
    private void checkCancelOnDmg() {
        if (isCancelOnDmg()) {
            effected.getObserveController().attach(new ActionObserver(ObserverType.ATTACKED) {

                @Override
                public void attacked(Creature creature) {
                    effected.getEffectController().removeEffect(getSkillId());
                }
            });

            effected.getObserveController().attach(new ActionObserver(ObserverType.DOT_ATTACKED) {

                @Override
                public void dotattacked(Creature creature, Effect dotEffect) {
                    if(dotEffect.getSkillId() == 50056){
                    }
                    else{
                    effected.getEffectController().removeEffect(getSkillId());
                    }
                }
            });
        }
    }

    public void setCancelOnDmg(boolean value) {
        this.isCancelOnDmg = value;
    }

    public boolean isCancelOnDmg() {
        return this.isCancelOnDmg;
    }

    public void endEffects() {
        for (EffectTemplate template : successEffects.values()) {
            template.endEffect(this);
        }
    }

    public boolean isFearEffect() {
        for (EffectTemplate template : successEffects.values()) {
            if (template instanceof FearEffect) {
                return true;
            }
        }
        return false;
    }

    public boolean isDelayedDamage() {
        return this.isDelayedDamage;
    }

    public void setDelayedDamage(boolean value) {
        this.isDelayedDamage = value;
    }

    public boolean isPetOrder() {
        return isPetOrder;
    }

    public void setPetOrder(boolean value) {
        isPetOrder = value;
    }

    public boolean isSummoning() {
        return isSummoning;
    }

    public void setSumonning(boolean value) {
        isSummoning = value;
    }

    private int initializePower(SkillTemplate skill) { //снятие банками//снятие заклом
        if (skill.getActivationAttribute().equals(ActivationAttribute.MAINTAIN)) {
            return 30;
        }
        switch (skill.getSkillId()) {
            case 287:
            case 322:
            case 390:
            case 391:
            case 426:
            case 537:
            case 671:
            case 672:
            case 1040:
            case 1343:
            case 1472:
            case 1509:
            case 1560:
            case 1561:
            case 1794:
            case 1978:
            case 2001:
            case 2010:
            case 2089:
            case 2090:
            case 2129:
            case 2136:
            case 2152:
            case 2196:
            case 2578:
            case 18214:
            case 18232:
            case 18239:
                return 30;
            case 1774:
            case 2225:
                return 40;
            case 18889:
            case 18892:
            case 18994:
            case 19090:
            case 19148:
            case 19504:
            case 19505:
            case 19512:
            case 19513:
            case 19514:
            case 19515:
            case 19516:
            case 19644:
            case 19647:
                return 255;
        }

        return 10;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int removePower(int power) {
        this.power -= power;

        return this.power;
    }

    public void setAccModBoost(int accModBoost) {
        this.accModBoost = accModBoost;
    }

   /**
   * functions that check for given effecttype
   */
    
    public boolean isHideEffect() {
        return this.getSkillTemplate().getEffects() != null && this.getSkillTemplate().getEffects().isEffectTypePresent(EffectType.HIDE);
    }
    
    public boolean isParalyzeEffect() {
        return this.getSkillTemplate().getEffects() != null && this.getSkillTemplate().getEffects().isEffectTypePresent(EffectType.PARALYZE);
    }
    
    public boolean isSanctuaryEffect() {
    return this.getSkillTemplate().getEffects() != null && this.getSkillTemplate().getEffects().isEffectTypePresent(EffectType.SANCTUARY);
  }
    
    public int getAccModBoost() {
        return accModBoost;
    }

    public boolean isDamageEffect() {
        return isDamageEffect;
    }

    public void setDamageEffect(boolean isDamageEffect) {
        this.isDamageEffect = isDamageEffect;
    }

    public int getSignetBurstedCount() {
        return signetBurstedCount;
    }

    public void setSignetBurstedCount(int signetBurstedCount) {
        this.signetBurstedCount = signetBurstedCount;
    }

    public boolean isPhysicalState() {
        return isPhysicalState;
    }

    public void setIsPhysicalState(boolean isPhysicalState) {
        this.isPhysicalState = isPhysicalState;
    }

    public boolean isMagicalState() {
        return isMagicalState;
    }

    public void setIsMagicalState(boolean isMagicalState) {
        this.isMagicalState = isMagicalState;
    }

    public void setEffectResult(EffectResult conflict) {
    }
}
