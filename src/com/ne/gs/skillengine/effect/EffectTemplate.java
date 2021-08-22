/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.effect;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import javolution.util.FastList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.Rnd;
import com.ne.gs.ai2.poll.AIQuestion;
import com.ne.gs.controllers.attack.AttackStatus;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.SkillElement;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Kisk;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.skillengine.change.Change;
import com.ne.gs.skillengine.condition.Conditions;
import com.ne.gs.skillengine.effect.modifier.ActionModifier;
import com.ne.gs.skillengine.effect.modifier.ActionModifiers;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.HitType;
import com.ne.gs.skillengine.model.HopType;
import com.ne.gs.skillengine.model.SkillTemplate;
import com.ne.gs.skillengine.model.SkillType;
import com.ne.gs.skillengine.model.SpellStatus;
import com.ne.gs.utils.stats.StatFunctions;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Effect")
public abstract class EffectTemplate {

    protected ActionModifiers modifiers;
    protected List<Change> change;
    @XmlAttribute
    protected int effectid;
    @XmlAttribute(required = true)
    protected int duration2;
    @XmlAttribute
    protected int duration1;
    @XmlAttribute(name = "randomtime")
    protected int randomTime;
    @XmlAttribute(name = "e")
    protected int position;
    @XmlAttribute(name = "basiclvl")
    protected int basicLvl;
    @XmlAttribute(name = "hittype", required = false)
    protected HitType hitType = HitType.EVERYHIT;
    @XmlAttribute(name = "hittypeprob2", required = false)
    protected int hitTypeProb = 100;
    @XmlAttribute(name = "element")
    protected SkillElement element = SkillElement.NONE;
    @XmlElement(name = "subeffect")
    protected SubEffect subEffect;
    @XmlElement(name = "conditions")
    protected Conditions effectConditions;
    @XmlElement(name = "subconditions")
    protected Conditions effectSubConditions;
    @XmlAttribute(name = "hoptype")
    protected HopType hopType;
    @XmlAttribute(name = "hopa")
    protected int hopA; // effects the agro-value (hate)
    @XmlAttribute(name = "hopb")
    protected int hopB; // effects the agro-value (hate)
    @XmlAttribute(name = "noresist")
    protected boolean _noResist;
    @XmlAttribute(name = "accmod1")
    protected int accMod1;// accdelta
    @XmlAttribute(name = "accmod2")
    protected int accMod2;// accvalue
    @XmlAttribute(name = "preeffect")
    protected String preEffect;
    @XmlAttribute(name = "preeffect_prob")
    protected int preEffectProb = 100;

    @XmlAttribute(name = "critprobmod2")
    protected int critProbMod2 = 100;

    @XmlAttribute(name = "critadddmg1")
    protected int critAddDmg1 = 0;

    @XmlAttribute(name = "critadddmg2")
    protected int critAddDmg2 = 0;
    @XmlAttribute
    protected int value;
    @XmlAttribute
    protected int delta;

    @XmlTransient
    protected EffectType effectType = null;

    @XmlTransient
    protected static Logger log = LoggerFactory.getLogger(EffectTemplate.class);

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * @return the delta
     */
    public int getDelta() {
        return delta;
    }

    public int getDuration2() {
        return duration2;
    }

    /**
     * @return the duration
     */
    public int getDuration1() {
        return duration1;
    }

    /**
     * @return the randomtime
     */
    public int getRandomTime() {
        return randomTime;
    }

    /**
     * @return the modifiers
     */
    public ActionModifiers getModifiers() {
        return modifiers;
    }

    /**
     * @return the change
     */
    public List<Change> getChange() {
        return change;
    }

    /**
     * @return the effectid
     */
    public int getEffectid() {
        return effectid;
    }

    /**
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * @return the basicLvl
     */
    public int getBasicLvl() {
        return basicLvl;
    }

    /**
     * @return the element
     */
    public SkillElement getElement() {
        return element;
    }

    /**
     * @return the preEffect
     */
    public String getPreEffect() {
        return preEffect;
    }

    /**
     * @return the preEffectProb
     */
    public int getPreEffectProb() {
        return preEffectProb;
    }

    public int getCritProbMod2() {
        return critProbMod2;
    }

    public int getCritAddDmg1() {
        return critAddDmg1;
    }

    public int getCritAddDmg2() {
        return critAddDmg2;
    }

    /**
     * Gets the effect conditions status
     *
     * @return list of Conditions for effect template
     */
    public Conditions getEffectConditions() {
        return effectConditions;
    }

    /**
     * Gets the sub effect conditions status
     *
     * @return list of Conditions for sub effects within effect template
     */
    public Conditions getEffectSubConditions() {
        return effectSubConditions;
    }

    /**
     * @param effect
     *
     * @return
     */
    public ActionModifier getActionModifiers(Effect effect) {
        if (modifiers == null) {
            return null;
        }

        for (ActionModifier modifier : modifiers.getActionModifiers()) {
            if (modifier.check(effect)) {
                return modifier;
            }
        }
        return null;
    }

    /**
     * @return the effectType
     */
    public EffectType getEffectType() {
        return effectType;
    }

    /**
     * Calculate effect result
     *
     * @param effect
     */
    public void calculate(Effect effect) {
        calculate(effect, null, null);
    }

    public boolean calculate(Effect effect, StatEnum statEnum, SpellStatus spellStatus) {
        
        if (effect.isSkipAfter()) {
        return false;
        }
        
        return calculate(effect, statEnum, spellStatus, false);
        
    }

    /**
     * 1) check conditions 2) check preeffect 3) check effectresistrate 4) check noresist 5) decide if its magical or
     * physical effect 6) physical - check cannotmiss 7) check magic resist / dodge 8) addsuccess exceptions: buffbind
     * buffsilence buffsleep buffstun randommoveloc recallinstant returneffect returnpoint shieldeffect signeteffect
     * summoneffect xpboosteffect
     *
     * @param effect
     * @param statEnum
     * @param spellStatus
     */
    public boolean calculate(Effect effect, StatEnum statEnum, SpellStatus spellStatus, boolean ignoreResist) {
        if (effect.getSkillTemplate().isPassive()) {
            this.addSuccessEffect(effect, spellStatus);
            return true;
        }

        if ((statEnum != null) && (isAlteredState(statEnum)) && (isImuneToAbnormal(effect, statEnum))) {
            return false;
        }

        if (effect.getIsForcedEffect()) {
            addSuccessEffect(effect, spellStatus);
            return true;
        }
        if (!effectConditionsCheck(effect)) {
            return false;
        }

        // preeffects
        if (this.getPosition() > 1) {
            FastList<Integer> positions = getPreEffects();
            for (int pos : positions) {
                if (!effect.isInSuccessEffects(pos)) {
                    return false;
                }
            }

            // check preeffect probability
            if (Rnd.get(0, 100) > this.getPreEffectProb()) {
                return false;
            }
        }
        

        
        
        
        // check effectresistrate
        if (!this.calculateEffectResistRate(effect, statEnum)) {
            if (!effect.isDamageEffect()) {
                effect.clearSucessEffects();
            }

            effect.setAttackStatus(AttackStatus.BUF);
            return false;
        }

        SkillType skillType = effect.getSkillType();
        // certain effects are magical by default
        if (isMagicalEffectTemp()) {
            skillType = SkillType.MAGICAL;
        }

        if (!ignoreResist && !_noResist) {
            int boostResist = 0;
            switch (effect.getSkillTemplate().getSubType()) {
                case DEBUFF:
                    boostResist = effect.getEffector().getGameStats().getStat(StatEnum.BOOST_RESIST_DEBUFF, 0).getCurrent();
            }
            int accMod = accMod2 + accMod1 * effect.getSkillLevel() + effect.getAccModBoost() + boostResist;
            switch (skillType) {
                case PHYSICAL: // cannotMiss = true - скилы проходят 100%
                    boolean cannotMiss = false;
                    if (this instanceof SkillAttackInstantEffect) {
                                      
                        cannotMiss = ((SkillAttackInstantEffect) this).isCannotmiss();
                    }
                    
                    final Creature effected = effect.getEffected();
                    if (effected.getEffectController().hasAbnormalEffect(572) && cannotMiss == true) {
                        effected.getEffectController().removeEffect(572); 
                    }
                    
                    if (!cannotMiss && StatFunctions.calculatePhysicalDodgeRate(effect.getEffector(), effect.getEffected(), accMod)) {
                        return false;
                    }
                    break;
                case MAGICAL:
                    if (Rnd.get(0, 1000) < StatFunctions.calculateMagicalResistRate(effect.getEffector(), effect.getEffected(), accMod)) {
                        return false;
                    }
            }
        }

        this.addSuccessEffect(effect, spellStatus);
        return true;
    }

    protected void addSuccessEffect(Effect effect, SpellStatus spellStatus) {
        effect.addSucessEffect(this);
        if (spellStatus != null) {
            effect.setSpellStatus(spellStatus);
        }
    }

    /**
     * Check all condition statuses for effect template
     */
    private boolean effectConditionsCheck(Effect effect) {
        Conditions effectConditions = getEffectConditions();
        return effectConditions == null || effectConditions.validate(effect);
    }

    private FastList<Integer> getPreEffects() {
        FastList<Integer> preEffects = new FastList<>();

        if (this.getPreEffect() == null) {
            return preEffects;
        }

        String[] parts = this.getPreEffect().split("_");
        for (String part : parts) {
            preEffects.add(Integer.parseInt(part));
        }

        return preEffects;
    }

    /**
     * Apply effect to effected
     *
     * @param effect
     */
    public abstract void applyEffect(Effect effect);

    /**
     * Start effect on effected
     *
     * @param effect
     */
    public void startEffect(Effect effect) {
    }

    /**
     * @param effect
     */
    public void calculateSubEffect(Effect effect) {
        if (subEffect == null) {
            return;
        }
        // Pre-Check for sub effect conditions
        if (!effectSubConditionsCheck(effect)) {
            effect.setSubEffectAborted(true);
            return;
        }

        // chance to trigger subeffect
        if (!Rnd.chance(subEffect.getChance())) {
            return;
        }

        SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(subEffect.getSkillId());
        int level = 1;
        if (subEffect.isAddEffect()) {
            level = effect.getSignetBurstedCount();
        }
        Effect newEffect = new Effect(effect.getEffector(), effect.getEffected(), template, level, 0);
        newEffect.setAccModBoost(effect.getAccModBoost());
        newEffect.initialize();
        
        //фикс воздушных оков
        switch (newEffect.getSpellStatus()) {
            case DODGE:
            case RESIST:
            case OPENAERIAL:
            break;
            default:
            effect.setSpellStatus(newEffect.getSpellStatus());
            break;
        }
        
        effect.setSubEffect(newEffect);
        effect.setSkillMoveType(newEffect.getSkillMoveType());
        effect.setTargetLoc(newEffect.getTargetX(), newEffect.getTargetY(), newEffect.getTargetZ());
    }

    /**
     * Check all sub effect condition statuses for effect
     */
    private boolean effectSubConditionsCheck(Effect effect) {
        return effectSubConditions == null || effectSubConditions.validate(effect);
    }

    /**
     * Hate will be added to result value only if particular effect template has success result
     *
     * @param effect
     */
    public void calculateHate(Effect effect) {
        if (hopType == null) {
            return;
        }

        if (effect.getSuccessEffects().isEmpty()) {
            return;
        }

        int currentHate = effect.getEffectHate();
        if (hopType != null) {
            switch (hopType) {
                case DAMAGE:
                    currentHate += effect.getReserved1();
                    break;
                case SKILLLV:
                    int skillLvl = effect.getSkillLevel();
                    currentHate += hopB + hopA * skillLvl; // Agro-value of the effect
                default:
                    break;
            }
        }
        if (currentHate == 0) {
            currentHate = 1;
        }
        effect.setEffectHate(StatFunctions.calculateHate(effect.getEffector(), currentHate));
    }

    /**
     * @param effect
     */
    public void startSubEffect(Effect effect) {
        if (subEffect == null) {
            return;
        }

        // Apply-Check for sub effect conditions
        if (effect.isSubEffectAbortedBySubConditions()) {
            return;
        }
        if (effect.getSubEffect() != null) {
            effect.getSubEffect().applyEffect();
        }
    }

    /**
     * Do periodic effect on effected
     *
     * @param effect
     */
    public void onPeriodicAction(Effect effect) {
    }

    /**
     * End effect on effected
     *
     * @param effect
     */
    public void endEffect(Effect effect) {
    }

    /**
     * @param effect
     * @param statEnum
     *
     * @return true = no resist, false = resisted
     */
    public boolean calculateEffectResistRate(Effect effect, StatEnum statEnum) {

        if (effect.getEffected() == null || effect.getEffected().getGameStats() == null || effect.getEffector() == null || effect.getEffector().getGameStats() == null) {
            return false;
        }

        Creature effected = effect.getEffected();
        Creature effector = effect.getEffector();

        if (statEnum == null) {
            return true;
        }

        int effectPower = 1000;

        if (isAlteredState(statEnum)) {
            effectPower -= effect.getEffected().getGameStats().getStat(StatEnum.ABNORMAL_RESISTANCE_ALL, 0).getCurrent();
        }

        // effect resistance
        effectPower -= effect.getEffected().getGameStats().getStat(statEnum, 0).getCurrent();

        // penetration
        StatEnum penetrationStat = this.getPenetrationStat(statEnum);
        if (penetrationStat != null) {
            effectPower += effector.getGameStats().getStat(penetrationStat, 0).getCurrent();
        }

        // resist mod pvp
        if (effector.isPvpTarget(effect.getEffected())) {
            int differ = (effected.getLevel() - effector.getLevel());
            if (differ > 2 && differ < 8) {
                effectPower -= Math.round((effectPower * (differ - 2) / 15f));
            } else if (differ >= 8) {
                effectPower *= 0.1f;
            }
        }

        // resist mod PvE
        if (effect.getEffected() instanceof Npc) {
            Npc effectrd = (Npc) effect.getEffected();
            int hpGaugeMod = effectrd.getObjectTemplate().getRank().ordinal() - 1;
            effectPower -= hpGaugeMod * 100;
        }

        return Rnd.get(1000) <= effectPower;
    }

    private boolean isImuneToAbnormal(Effect effect, StatEnum statEnum) {
        Creature effected = effect.getEffected();

        if(effected != null && statEnum == StatEnum.STUMBLE_RESISTANCE){
            if(effected.getEffectController().isUnderShield())
                return true;
        }

        if ((effected instanceof Npc) && effected != effect.getEffector()) {
            Npc npc = (Npc) effected;

            if (npc.isBoss() || npc.hasStatic() || (npc instanceof Kisk) || npc.getAi2().ask(AIQuestion.CAN_RESIST_ABNORMAL).isPositive()) {
                return true;
            }
            if (npc.getObjectTemplate().getStatsTemplate().getRunSpeed() == 0.0F
                && (statEnum == StatEnum.PULLED_RESISTANCE || statEnum == StatEnum.STAGGER_RESISTANCE || statEnum == StatEnum.STUMBLE_RESISTANCE)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAlteredState(StatEnum stat) {
        switch (stat) {
            case BIND_RESISTANCE:
            case BLIND_RESISTANCE:
            case CHARM_RESISTANCE:
            case CONFUSE_RESISTANCE:
            case CURSE_RESISTANCE:
            case DEFORM_RESISTANCE:
            case FEAR_RESISTANCE:
            case OPENAREIAL_RESISTANCE:
            case PARALYZE_RESISTANCE:
            case PULLED_RESISTANCE:
            case ROOT_RESISTANCE:
            case SILENCE_RESISTANCE:
            case SLEEP_RESISTANCE:
            case SLOW_RESISTANCE:
            case SNARE_RESISTANCE:
            case SPIN_RESISTANCE:
            case STAGGER_RESISTANCE:
            case STUMBLE_RESISTANCE:
            case STUN_RESISTANCE:
                return true;
        }
        return false;
    }

    private StatEnum getPenetrationStat(StatEnum statEnum) {
        switch (statEnum) {
            case BLEED_RESISTANCE_PENETRATION:
                return StatEnum.BLEED_RESISTANCE_PENETRATION;
            case BLIND_RESISTANCE_PENETRATION:
                return StatEnum.BLIND_RESISTANCE_PENETRATION;
            case CHARM_RESISTANCE_PENETRATION:
                return StatEnum.CHARM_RESISTANCE_PENETRATION;
            case CONFUSE_RESISTANCE_PENETRATION:
                return StatEnum.CONFUSE_RESISTANCE_PENETRATION;
            case CURSE_RESISTANCE_PENETRATION:
                return StatEnum.CURSE_RESISTANCE_PENETRATION;
            case DISEASE_RESISTANCE_PENETRATION:
                return StatEnum.DISEASE_RESISTANCE_PENETRATION;
            case FEAR_RESISTANCE_PENETRATION:
                return StatEnum.FEAR_RESISTANCE_PENETRATION;
            case OPENAREIAL_RESISTANCE_PENETRATION:
                return StatEnum.OPENAREIAL_RESISTANCE_PENETRATION;
            case PARALYZE_RESISTANCE_PENETRATION:
                return StatEnum.PARALYZE_RESISTANCE_PENETRATION;
            case PERIFICATION_RESISTANCE_PENETRATION:
                return StatEnum.PERIFICATION_RESISTANCE_PENETRATION;
            case POISON_RESISTANCE_PENETRATION:
                return StatEnum.POISON_RESISTANCE_PENETRATION;
            case ROOT_RESISTANCE_PENETRATION:
                return StatEnum.ROOT_RESISTANCE_PENETRATION;
            case SILENCE_RESISTANCE_PENETRATION:
                return StatEnum.SILENCE_RESISTANCE_PENETRATION;
            case SLEEP_RESISTANCE_PENETRATION:
                return StatEnum.SLEEP_RESISTANCE_PENETRATION;
            case SLOW_RESISTANCE_PENETRATION:
                return StatEnum.SLOW_RESISTANCE_PENETRATION;
            case SNARE_RESISTANCE_PENETRATION:
                return StatEnum.SNARE_RESISTANCE_PENETRATION;
            case SPIN_RESISTANCE_PENETRATION:
                return StatEnum.SPIN_RESISTANCE_PENETRATION;
            case STAGGER_RESISTANCE_PENETRATION:
                return StatEnum.STAGGER_RESISTANCE_PENETRATION;
            case STUMBLE_RESISTANCE:
                return StatEnum.STUMBLE_RESISTANCE_PENETRATION;
            case STUN_RESISTANCE:
                return StatEnum.STUN_RESISTANCE_PENETRATION;
            default:
                return null;
        }
    }

    /**
     * certain effects are magical even when used in physical skills it includes stuns from chanter/sin/ranger etc these
     * effects(effecttemplates) are dependent on magical accuracy and magical resist
     *
     * @return
     */
    private boolean isMagicalEffectTemp() {
        return this instanceof SilenceEffect || this instanceof SleepEffect || this instanceof RootEffect || this instanceof SnareEffect || this instanceof StunEffect || this instanceof PoisonEffect
            || this instanceof BindEffect || this instanceof BleedEffect || this instanceof BlindEffect || this instanceof DeboostHealEffect || this instanceof ParalyzeEffect || this instanceof
            SlowEffect;

    }

    void afterUnmarshal(Unmarshaller u, Object parent) {
        EffectType temp = null;
        String effectName = getClass().getSimpleName().replaceAll("Effect", "").toUpperCase();
        try {
            temp = EffectType.valueOf(effectName);
        } catch (Exception e) {
            log.info("missing effectype for " + effectName);
        }

        this.effectType = temp;
    }
}
