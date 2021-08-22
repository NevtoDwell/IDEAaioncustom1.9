/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Iterator;

import com.ne.commons.utils.Rnd;
import com.ne.gs.controllers.attack.AttackStatus;
import com.ne.gs.skillengine.action.Actions;
import com.ne.gs.skillengine.condition.ChainCondition;
import com.ne.gs.skillengine.condition.Condition;
import com.ne.gs.skillengine.condition.Conditions;
import com.ne.gs.skillengine.effect.EffectTemplate;
import com.ne.gs.skillengine.effect.EffectType;
import com.ne.gs.skillengine.effect.Effects;
import com.ne.gs.skillengine.periodicaction.PeriodicActions;
import com.ne.gs.skillengine.properties.Properties;

/**
 * @author ATracer modified by Wakizashi
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "skillTemplate", propOrder = {"properties", "startconditions", "useconditions", "useequipmentconditions", "effects", "actions", "periodicActions", "motion"})
public class SkillTemplate {

    protected Properties properties;
    protected Conditions startconditions;
    protected Conditions useconditions;
    protected Conditions useequipmentconditions;
    protected Effects effects;
    protected Actions actions;
    @XmlElement(name = "periodicactions")
    protected PeriodicActions periodicActions;
    protected Motion motion;

    @XmlAttribute(name = "skill_id", required = true)
    protected int skillId;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute(name = "nameId", required = true)
    protected int nameId;
    @XmlAttribute
    protected String stack = "NONE";
    @XmlAttribute
    protected int cooldownId;
    @XmlAttribute
    protected int lvl;
    @XmlAttribute(name = "skilltype", required = true)
    protected SkillType type = SkillType.NONE;
    @XmlAttribute(name = "skillsubtype", required = true)
    protected SkillSubType subType;
    @XmlAttribute(name = "tslot")
    protected SkillTargetSlot targetSlot;
    @XmlAttribute(name = "tslot_level")
    protected int targetSlotLevel;
    @XmlAttribute(name = "dispel_category")
    protected DispelCategoryType dispelCategory = DispelCategoryType.NONE;
    @XmlAttribute(name = "req_dispel_level")
    protected int reqDispelLevel;
    @XmlAttribute(name = "activation", required = true)
    protected ActivationAttribute activationAttribute;
    @XmlAttribute(required = true)
    protected int duration;
    @XmlAttribute(name = "cooldown")
    protected int cooldown;
    @XmlAttribute(name = "penalty_skill_id")
    protected int penaltySkillId;
    @XmlAttribute(name = "pvp_damage")
    protected int pvpDamage;
    @XmlAttribute(name = "pvp_duration")
    protected int pvpDuration;
    @XmlAttribute(name = "chain_skill_prob")
    protected int chainSkillProb;
    @XmlAttribute(name = "cancel_rate")
    protected int cancelRate;
    @XmlAttribute(name = "stance")
    protected boolean stance;
    @XmlAttribute(name = "skillset_exception")
    protected int skillSetException;
    @XmlAttribute(name = "skillset_maxoccur")
    protected int skillSetMaxOccur;
    @XmlAttribute(name = "avatar")
    protected boolean isDeityAvatar;
    @XmlAttribute(name = "ground")
    protected boolean isGroundSkill;// TODO remove!
    @XmlAttribute(name = "unpottable")
    protected boolean isUndispellableByPotions;
    @XmlAttribute(name = "ammospeed")
    protected int ammoSpeed;
    @XmlAttribute(name = "conflict_id")
    protected int conflictId;
    @XmlAttribute(name = "counter_skill")
    protected AttackStatus counterSkill = null;
    @XmlAttribute(name = "noremoveatdie")
    protected boolean noRemoveAtDie = false;
    @XmlAttribute(name = "stigma")
    protected StigmaType stigmaType = StigmaType.NONE;

    @XmlAttribute(name = "stigmaId")
    private int _stigmaId;

    /**
     * @return the Properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Gets the value of the startconditions property.
     *
     * @return possible object is {@link Conditions }
     */
    public Conditions getStartconditions() {
        return startconditions;
    }

    /**
     * Gets the value of the useconditions property.
     *
     * @return possible object is {@link Conditions }
     */
    public Conditions getUseconditions() {
        return useconditions;
    }

    /**
     * Gets the value of the useequipmentconditions property.
     *
     * @return possible object is {@link Conditions }
     */
    public Conditions getUseEquipmentconditions() {
        return useequipmentconditions;
    }

    /**
     * Gets the value of the effects property.
     *
     * @return possible object is {@link Effects }
     */
    public Effects getEffects() {
        return effects;
    }

    /**
     * Gets the value of the actions property.
     *
     * @return possible object is {@link Actions }
     */
    public Actions getActions() {
        return actions;
    }

    /**
     * Gets the value of the periodicActions property.
     *
     * @return possible object is {@link PeriodicActions }
     */
    public PeriodicActions getPeriodicActions() {
        return periodicActions;
    }

    /**
     * Gets the value of the motion property.
     *
     * @return possible object is {@link Motion }
     */
    public Motion getMotion() {
        return motion;
    }

    /**
     * Gets the value of the skillId property.
     */
    public int getSkillId() {
        return skillId;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * @return the nameId
     */
    public int getNameId() {
        return nameId;
    }

    /**
     * @return the stack
     */
    public String getStack() {
        return stack;
    }

    /**
     * @return the lvl
     */
    public int getLvl() {
        return lvl;
    }

    /**
     * Gets the value of the type property.
     *
     * @return possible object is {@link SkillType }
     */
    public SkillType getType() {
        return type;
    }

    /**
     * @return the subType
     */
    public SkillSubType getSubType() {
        return subType;
    }

    /**
     * @return the targetSlot
     */
    public SkillTargetSlot getTargetSlot() {
        return targetSlot;
    }

    /**
     * @return the targetSlot Level
     */
    public int getTargetSlotLevel() {
        return targetSlotLevel;
    }

    /**
     * @return the dispelCategory
     */
    public DispelCategoryType getDispelCategory() {
        return dispelCategory;
    }

    /**
     * @return the reqDispelLevel
     */
    public int getReqDispelLevel() {
        return reqDispelLevel;
    }

    /**
     * @return the duration
     */
    public int getDuration() {
        return duration;
    }

    /**
     * @return the activationAttribute
     */
    public ActivationAttribute getActivationAttribute() {
        return activationAttribute;
    }

    public boolean isPassive() {
        return activationAttribute == ActivationAttribute.PASSIVE;
    }

    public boolean isToggle() {
        return activationAttribute == ActivationAttribute.TOGGLE;
    }

    public boolean isProvoked() {
        return activationAttribute == ActivationAttribute.PROVOKED;
    }

    public boolean isMaintain() {
        return activationAttribute == ActivationAttribute.MAINTAIN;
    }

    public boolean isActive() {
        return activationAttribute == ActivationAttribute.ACTIVE;
    }

    /**
     * @param position
     *
     * @return EffectTemplate
     */
    public EffectTemplate getEffectTemplate(int position) {
        return effects != null && effects.getEffects().size() >= position ? effects.getEffects().get(position - 1) : null;

    }

    /**
     * @return the cooldown
     */
    public int getCooldown() {
        return cooldown;
    }

    /**
     * @return the penaltySkillId
     */
    public int getPenaltySkillId() {
        return penaltySkillId;
    }

    /**
     * @return the pvpDamage
     */
    public int getPvpDamage() {
        return pvpDamage;
    }

    /**
     * @return the pvpDuration
     */
    public int getPvpDuration() {
        return pvpDuration;
    }

    /**
     * @return chainSkillProb
     */
    public int getChainSkillProb() {
        return chainSkillProb;
    }

    /**
     * @return cancelRate
     */
    public int getCancelRate() {
        return cancelRate;
    }

    /**
     * @return stance
     */
    public boolean isStance() {
        return stance;
    }

    /**
     * @return skillSetException
     */
    public int getSkillSetException() {
        return skillSetException;
    }

    /**
     * @return skillSetMaxOccur
     */
    public int getSkillSetMaxOccur() {
        return skillSetMaxOccur;
    }

    public boolean hasResurrectEffect() {
        return getEffects() != null && getEffects().isResurrect();
    }

    public boolean hasItemHealFpEffect() {
        return getEffects() != null && getEffects().isEffectTypePresent(EffectType.PROCFPHEALINSTANT);
    }

    public boolean hasEvadeEffect() {
        return getEffects() != null && getEffects().isEffectTypePresent(EffectType.EVADE);
    }

    public boolean hasRecallInstant() {
        return getEffects() != null && getEffects().isEffectTypePresent(EffectType.RECALLINSTANT);
    }

    public int getCooldownId() {
        return (cooldownId > 0) ? cooldownId : skillId;
    }

    public boolean isDeityAvatar() {
        return isDeityAvatar;
    }

    public boolean isGroundSkill() {
        return isGroundSkill;
    }

    public AttackStatus getCounterSkill() {
        return counterSkill;
    }

    public boolean isUndispellableByPotions() {
        return isUndispellableByPotions;
    }

    public int getAmmoSpeed() {
        return ammoSpeed;
    }

    public int getConflictId() {
        return conflictId;
    }

    public boolean isNoRemoveAtDie() {
        return noRemoveAtDie;
    }

    public int getEffectsDuration(int skillLevel) {
        int duration = 0;
        Iterator<EffectTemplate> itr = getEffects().getEffects().iterator();
        while (itr.hasNext() && duration == 0) {
            EffectTemplate et = itr.next();
            int effectDuration = et.getDuration2() + et.getDuration1() * skillLevel;
            if (et.getRandomTime() > 0) {
                effectDuration -= Rnd.get(et.getRandomTime());
            }
            duration = duration > effectDuration ? duration : effectDuration;
        }

        return duration;
    }

    public ChainCondition getChainCondition() {
        if (startconditions != null) {
            for (Condition cond : startconditions.getConditions()) {
                if (cond instanceof ChainCondition) {
                    return (ChainCondition) cond;
                }
            }
        }
        return null;
    }

    public int getStigmaId() {
        return _stigmaId;
    }

    public void setStigmaId(int stigmaId) {
        _stigmaId = stigmaId;
    }

    public void setPeriodicActions(PeriodicActions periodicActions) {
        this.periodicActions = periodicActions;
    }

    public void setActivationAttribute(ActivationAttribute activationAttribute) {
        this.activationAttribute = activationAttribute;
    }
    public StigmaType getStigmaType() {
        return stigmaType;
    }
}
