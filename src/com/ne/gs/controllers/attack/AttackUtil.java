/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.attack;

import com.ne.commons.utils.Rnd;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.SkillElement;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.model.templates.item.ItemAttackType;
import com.ne.gs.model.templates.item.WeaponType;
import com.ne.gs.network.aion.serverpackets.SM_TARGET_SELECTED;
import com.ne.gs.skillengine.change.Func;
import com.ne.gs.skillengine.effect.modifier.ActionModifier;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.HitType;
import com.ne.gs.skillengine.model.SkillTemplate;
import com.ne.gs.utils.stats.StatFunctions;
import com.ne.gs.world.knownlist.Visitor;

import java.util.ArrayList;
import java.util.List;

import static com.ne.gs.utils.stats.StatFunctions.calculateMagicalSkillDamage;
import static java.util.Collections.singletonList;

/**
 * @author ATracer
 */
public final class AttackUtil {

    /**
     * Calculate physical attack status and damage
     */
    public static List<AttackResult> calculatePhysicalAttackResult(Creature attacker, Creature attacked) {
        AttackStatus attackerStatus = null;
        int damage = StatFunctions.calculateAttackDamage(attacker, attacked, true, SkillElement.NONE);
        List<AttackResult> attackList = new ArrayList<>();
        AttackStatus mainHandStatus = calculateMainHandResult(attacker, attacked, attackerStatus, damage, attackList);
        if (attacker instanceof Player && ((Player) attacker).getEquipment().getOffHandWeaponType() != null) {
            calculateOffHandResult(attacker, attacked, mainHandStatus, attackList);
        }
        attacked.getObserveController().checkShieldStatus(attackList, attacker);
        return attackList;
    }

    /**
     * Calculate physical attack status and damage of the MAIN hand
     */
    private static AttackStatus calculateMainHandResult(Creature attacker, Creature attacked, AttackStatus attackerStatus,
                                                        int damage, List<AttackResult> attackList) {
        AttackStatus mainHandStatus = attackerStatus;
        if (mainHandStatus == null) {
            mainHandStatus = calculatePhysicalStatus(attacker, attacked, true);
        }

        int mainHandHits = 1;
        if (attacker instanceof Player) {
            Item mainHandWeapon = ((Player) attacker).getEquipment().getMainHandWeapon();
            if (mainHandWeapon != null) {
                mainHandHits = Rnd.get(1, mainHandWeapon.getItemTemplate().getWeaponStats().getHitCount());
            }
        } else {
            mainHandHits = Rnd.get(1, 3);
        }
        splitPhysicalDamage(attacker, attacked, mainHandHits, damage, mainHandStatus, attackList);
        return mainHandStatus;
    }

    /**
     * Calculate physical attack status and damage of the OFF hand
     */
    private static void calculateOffHandResult(Creature attacker, Creature attacked, AttackStatus mainHandStatus,
                                               List<AttackResult> attackList) {
        AttackStatus offHandStatus = AttackStatus.getOffHandStats(mainHandStatus);
        Item offHandWeapon = ((Player) attacker).getEquipment().getOffHandWeapon();
        int offHandDamage = StatFunctions.calculateAttackDamage(attacker, attacked, false, SkillElement.NONE);
        int offHandHits = Rnd.get(1, offHandWeapon.getItemTemplate().getWeaponStats().getHitCount());
        splitPhysicalDamage(attacker, attacked, offHandHits, offHandDamage, offHandStatus, attackList);
    }

    /**
     * Generate attack results based on weapon hit count
     */
    private static List<AttackResult> splitPhysicalDamage(Creature attacker, Creature attacked, int hitCount, int damage,
                                                          AttackStatus status, List<AttackResult> attackList) {
        WeaponType weaponType;

        switch (AttackStatus.getBaseStatus(status)) {
            case BLOCK:
                int reduce = damage - attacked.getGameStats().getPositiveReverseStat(StatEnum.DAMAGE_REDUCE, damage);
                if (attacked instanceof Player) {
                    Item shield = ((Player) attacked).getEquipment().getEquippedShield();
                    if (shield != null) {
                        int reduceMax = shield.getItemTemplate().getWeaponStats().getReduceMax();
                        if (reduceMax > 0 && reduceMax < reduce) {
                            reduce = reduceMax;
                        }
                    }
                }
                damage -= reduce;
                break;
            case DODGE:
                damage = 0;
                break;
            case PARRY:
                damage *= 0.6;
                break;
            default:
                break;
        }

        if (status.isCritical()) {
            if (attacker instanceof Player) {
                weaponType = ((Player) attacker).getEquipment().getMainHandWeaponType();
                damage = (int) calculateWeaponCritical(attacked, damage, weaponType, StatEnum.PHYSICAL_CRITICAL_DAMAGE_REDUCE);
                // Proc Stumble/Stagger on Crit calculation
                applyEffectOnCritical((Player) attacker, attacked);
            } else {
                damage = (int) calculateWeaponCritical(attacked, damage, null, StatEnum.PHYSICAL_CRITICAL_DAMAGE_REDUCE);
            }
        }

        if (damage < 1) {
            damage = 0;
        }

        int firstHit = (int) (damage * (1f - (0.1f * (hitCount - 1))));
        int otherHits = Math.round(damage * 0.1f);
        for (int i = 0; i < hitCount; i++) {
            int dmg = (i == 0 ? firstHit : otherHits);
            attackList.add(new AttackResult(dmg, status, HitType.PHHIT));
        }
        return attackList;
    }

    /**
     * [Critical] Spear : x1.5 Sword : x2.5 Dagger : x2.3 Mace : x2.0 Greatsword : x1.5 Orb : x2.0 Spellbook : x2.0 Bow : x1.4 Staff : x1.5
     */
    private static float calculateWeaponCritical(Creature attacked, float damages, WeaponType weaponType, StatEnum stat) {
        return calculateWeaponCritical(attacked, damages, weaponType, 0, stat);
    }

    private static float calculateWeaponCritical(Creature attacked, float damages, WeaponType weaponType, int critAddDmg, StatEnum stat) {
        float coeficient = 2;
        if (weaponType != null) {

            switch (weaponType) {
                case DAGGER_1H:
                    coeficient = 2.3f;
                    break;
                case SWORD_1H:
                    coeficient = 2.2F;
                    break;
                case MACE_1H:
                    coeficient = 2.0F;
                    break;
                case SWORD_2H:
                case POLEARM_2H:
                    coeficient = 1.8F;
                    break;
                case STAFF_2H:
                case BOW:
                    coeficient = 1.7F;
                    break;
                default:
                    coeficient = 1.5F;
                    break;
            }
            if (stat.equals(StatEnum.MAGICAL_CRITICAL_DAMAGE_REDUCE)) {
                coeficient = 1.5F;
            }
        }
        if ((attacked instanceof Player)) {
            Player player = (Player) attacked;
            int fortitude;
            switch (stat) {
                case PHYSICAL_CRITICAL_DAMAGE_REDUCE:
                case MAGICAL_CRITICAL_DAMAGE_REDUCE:
                    fortitude = player.getGameStats().getStat(stat, 0).getCurrent();
                    coeficient -= Math.round(fortitude / 1000.0F);
            }

        }

        coeficient += critAddDmg / 100.0F;

        damages = Math.round(damages * coeficient);
        return damages;
    }

    public static void calculateSkillResult(Effect effect, int skillDamage, ActionModifier modifier, Func func, int randomDamage,
                                            int accMod, int criticalProb, int critAddDmg) {
        Creature effector = effect.getEffector();
        Creature effected = effect.getEffected();

        int damage;
        int baseAttack;
        if (effector.getAttackType() == ItemAttackType.PHYSICAL) {
            baseAttack = effector.getGameStats().getMainHandPAttack().getBase();
            damage = StatFunctions.calculatePhysicalAttackDamage(effect.getEffector(), effect.getEffected(), true);
        } else {
            baseAttack = effector.getGameStats().getMAttack().getBase();
            damage = StatFunctions.calculateMagicalAttackDamage(effect.getEffector(), effect.getEffected(), effector.getAttackType()
                    .getMagicalElement());
        }

        if (func != null) {
            switch (func) {
                case ADD:
                    damage += skillDamage;
                    break;
                case PERCENT:
                    damage = (int) (damage + baseAttack * skillDamage / 100.0F);
            }
        }

        if (modifier != null) {
            int bonus = modifier.analyze(effect);
            switch (modifier.getFunc()) {
                case ADD:
                    damage += bonus;
                    break;
                case PERCENT:
                    damage = (int) (damage + baseAttack * bonus / 100.0F);
            }

        }

        damage = (int) StatFunctions.adjustDamages(effect.getEffector(), effect.getEffected(), damage, effect.getPvpDamage());
        float damageMultiplier = effector.getObserveController().getBasePhysicalDamageMultiplier(true);
        damage = Math.round(damage * damageMultiplier);

        // implementation of random damage for skills like Stunning Shot, etc
        if (randomDamage > 0) {
            int randomChance = Rnd.get(100);

            switch (randomDamage) {
                case 1:
                    if (randomChance <= 40) {
                        damage /= 2;
                    } else if (randomChance <= 70) {
                        damage *= 1.5;
                    }
                    break;
                case 2:
                    if (randomChance <= 25) {
                        damage *= 3;
                    }
                    break;
                case 6:
                    if (randomChance <= 30) {
                        damage *= 2;
                    }
                    break;
                // TODO rest of the cases
                default:
                    /*
                     * chance to do from 50% to 200% damage This must NOT be calculated after critical status check, or it will be over powered and not retail
					 */
                    damage *= (Rnd.get(25, 100) * 0.02f);
                    break;
            }
        }

        AttackStatus status;
        if (effector.getAttackType() == ItemAttackType.PHYSICAL) {
            status = calculatePhysicalStatus(effector, effected, true, accMod, criticalProb, true);
        } else {
            status = calculateMagicalStatus(effector, effected, criticalProb, true);
        }

        switch (AttackStatus.getBaseStatus(status)) {
            case BLOCK:
                int reduce = damage - effected.getGameStats().getPositiveReverseStat(StatEnum.DAMAGE_REDUCE, damage);
                if (effected instanceof Player) {
                    Item shield = ((Player) effected).getEquipment().getEquippedShield();
                    if (shield != null) {
                        int reduceMax = shield.getItemTemplate().getWeaponStats().getReduceMax();
                        if (reduceMax > 0 && reduceMax < reduce) {
                            reduce = reduceMax;
                        }
                    }
                }
                damage -= reduce;
                break;
            case PARRY:
                damage *= 0.6;
                break;
            default:
                break;
        }

        if (status.isCritical()) {
            if (effector instanceof Player) {
                WeaponType weaponType = ((Player) effector).getEquipment().getMainHandWeaponType();
                damage = (int) calculateWeaponCritical(effected, damage, weaponType, critAddDmg, StatEnum.PHYSICAL_CRITICAL_DAMAGE_REDUCE);
                // Set effect as critical damage effect
                //applyEffectOnCritical((Player) effector, effected);
            } else {
                damage = (int) calculateWeaponCritical(effected, damage, null, critAddDmg, StatEnum.PHYSICAL_CRITICAL_DAMAGE_REDUCE);
            }
        }

        if (effected instanceof Npc) {
            damage = effected.getAi2().modifyDamage(damage);
        }
        if (effector instanceof Npc) {
            damage = effector.getAi2().modifyOwnerDamage(damage);
        }

        if (damage < 0) {
            damage = 0;
        }

        calculateEffectResult(effect, effected, damage, status, HitType.PHHIT);
    }

    private static void calculateEffectResult(Effect effect, Creature effected, int damage, AttackStatus status, HitType hitType) {
        AttackResult attackResult = new AttackResult(damage, status, hitType);
        effected.getObserveController().checkShieldStatus(singletonList(attackResult), effect.getEffector());
        effect.setReserved1(attackResult.getDamage());
        effect.setAttackStatus(attackResult.getAttackStatus());
        effect.setLaunchSubEffect(attackResult.isLaunchSubEffect());
        effect.setReflectedDamage(attackResult.getReflectedDamage());
        effect.setReflectedSkillId(attackResult.getReflectedSkillId());
        effect.setProtectedDamage(attackResult.getProtectedDamage());
        effect.setProtectedSkillId(attackResult.getProtectedSkillId());
        effect.setProtectorId(attackResult.getProtectorId());
        effect.setShieldDefense(attackResult.getShieldType());
    }

    public static List<AttackResult> calculateMagicalAttackResult(Creature attacker, Creature attacked, SkillElement elem) {
        int damage = StatFunctions.calculateAttackDamage(attacker, attacked, true, elem);

        AttackStatus status = calculateMagicalStatus(attacker, attacked, 100, false);
        List<AttackResult> attackList = new ArrayList<>();
        switch (status) {
            case RESIST:
                damage = 0;
                break;
            case CRITICAL:
                if (attacker instanceof Player) {
                    damage = (int) calculateWeaponCritical(attacked, damage, ((Player) attacker).getEquipment()
                            .getMainHandWeaponType(),
                            StatEnum.MAGICAL_CRITICAL_DAMAGE_REDUCE);
                } else {
                    damage = (int) calculateWeaponCritical(attacked, damage, null, StatEnum.MAGICAL_CRITICAL_DAMAGE_REDUCE);
                }
                break;
        }
        attackList.add(new AttackResult(damage, status));
        attacked.getObserveController().checkShieldStatus(attackList, attacker);
        return attackList;

    }

    public static int calculateMagicalOverTimeSkillResult(Effect effect, int skillDamage, SkillElement element, int position,
                                                          boolean useMagicBoost, int criticalProb, int critAddDmg) {
        Creature effector = effect.getEffector();
        Creature effected = effect.getEffected();

        // TODO is damage multiplier used on dot?
        float damageMultiplier = effector.getObserveController().getBaseMagicalDamageMultiplier();

        int damage = Math.round(calculateMagicalSkillDamage(effect.getEffector(), effect.getEffected(), skillDamage, 0, element, useMagicBoost,
                false, false, effect.getSkillTemplate().getPvpDamage()) * damageMultiplier);

        AttackStatus status = effect.getAttackStatus();
        // calculate attack status only if it has not been forced already
        if (status == AttackStatus.NORMALHIT && position == 1) {
            status = calculateMagicalStatus(effector, effected, criticalProb, true);
        }
        switch (status) {
            case CRITICAL:
                if (effector instanceof Player) {
                    WeaponType weaponType = ((Player) effector).getEquipment().getMainHandWeaponType();
                    // Set effect as critical damage effect
                    damage = (int) calculateWeaponCritical(effected, damage, weaponType, critAddDmg, StatEnum.MAGICAL_CRITICAL_DAMAGE_REDUCE);
                } else {
                    damage = (int) calculateWeaponCritical(effected, damage, null, critAddDmg, StatEnum.MAGICAL_CRITICAL_DAMAGE_REDUCE);
                }
                break;
            default:
                break;
        }

        if (damage <= 0) {
            damage = 1;
        }

        return damage;
    }

    public static void calculateMagicalSkillResult(Effect effect, int skillDamage, ActionModifier modifier, SkillElement element) {
        calculateMagicalSkillResult(effect, skillDamage, modifier, element, true, true, false, Func.ADD, 100, 0);
    }

    public static void calculateMagicalSkillResult(Effect effect, int skillDamage, ActionModifier modifier, SkillElement element,
                                                   boolean useMagicBoost, boolean useKnowledge, boolean noReduce, Func func, int criticalProb, int critAddDmg) {
        Creature effector = effect.getEffector();
        Creature effected = effect.getEffected();

        float damageMultiplier = effector.getObserveController().getBaseMagicalDamageMultiplier();
        int baseAttack = effector.getGameStats().getMainHandPAttack().getBase();
        int damage;
        int bonus = 0;

        if ((func.equals(Func.PERCENT)) && ((effector instanceof Npc))) {
            damage = Math.round(baseAttack * skillDamage / 100.0F);
        } else {
            damage = skillDamage;
        }

        if (modifier != null) {
            bonus = modifier.analyze(effect);
            switch (modifier.getFunc()) {
                case ADD:
                    break;
                case PERCENT:
                    if ((effector instanceof Npc)) {
                        bonus = Math.round(baseAttack * bonus / 100.0F);
                    }
                    break;
            }
        }

        damage = calculateMagicalSkillDamage(effect.getEffector(), effect.getEffected(),
                damage, bonus, element, useMagicBoost,
                useKnowledge, noReduce, effect.getSkillTemplate().getPvpDamage());

        damage = Math.round(damage * damageMultiplier);

        AttackStatus status = calculateMagicalStatus(effector, effected, criticalProb, true);
        switch (status) {
            case CRITICAL:
                if (effector instanceof Player) {
                    WeaponType weaponType = ((Player) effector).getEquipment().getMainHandWeaponType();
                    // Set effect as critical damage effect
                    damage = (int) calculateWeaponCritical(effected, damage, weaponType, critAddDmg, StatEnum.MAGICAL_CRITICAL_DAMAGE_REDUCE);
                } else {
                    damage = (int) calculateWeaponCritical(effected, damage, null, critAddDmg, StatEnum.MAGICAL_CRITICAL_DAMAGE_REDUCE);
                }
                break;
            default:
                break;
        }

        calculateEffectResult(effect, effected, damage, status, HitType.MAHIT);
    }

    /**
     * Manage attack status rate
     *
     * @return AttackStatus
     * @source http://www.aionsource.com/forum/mechanic-analysis/42597-character-stats-xp-dp-origin-gerbator-team-july-2009 -a.html
     */
    public static AttackStatus calculatePhysicalStatus(Creature attacker, Creature attacked, boolean isMainHand) {
        return calculatePhysicalStatus(attacker, attacked, isMainHand, 0, 100, false);
    }

    public static AttackStatus calculatePhysicalStatus(Creature attacker, Creature attacked, boolean isMainHand, int accMod,
                                                       int criticalProb, boolean isSkill) {
        AttackStatus status = AttackStatus.NORMALHIT;
        if (!isMainHand) {
            status = AttackStatus.OFFHAND_NORMALHIT;
        }

        if (attacked instanceof Player && ((Player) attacked).getEquipment()
                .isShieldEquipped() && StatFunctions.calculatePhysicalBlockRate(attacker, attacked)) {
            // accMod
            status = AttackStatus.BLOCK;
        } else if (attacked instanceof Player && ((Player) attacked).getEquipment().getMainHandWeaponType() != null
                && StatFunctions.calculatePhysicalParryRate(attacker, attacked)) {
            status = AttackStatus.PARRY;
        } else if (!isSkill) {
            if (StatFunctions.calculatePhysicalDodgeRate(attacker, attacked, accMod)) {
                status = AttackStatus.DODGE;
            }
        }

        if (StatFunctions.calculatePhysicalCriticalRate(attacker, attacked, isMainHand, criticalProb, isSkill)) {
            switch (status) {
                case BLOCK:
                    if (isMainHand) {
                        status = AttackStatus.CRITICAL_BLOCK;
                    } else {
                        status = AttackStatus.OFFHAND_CRITICAL_BLOCK;
                    }
                    break;
                case PARRY:
                    if (isMainHand) {
                        status = AttackStatus.CRITICAL_PARRY;
                    } else {
                        status = AttackStatus.OFFHAND_CRITICAL_PARRY;
                    }
                    break;
                case DODGE:
                    if (isMainHand) {
                        status = AttackStatus.CRITICAL_DODGE;
                    } else {
                        status = AttackStatus.OFFHAND_CRITICAL_DODGE;
                    }
                    break;
                default:
                    if (isMainHand) {
                        status = AttackStatus.CRITICAL;
                    } else {
                        status = AttackStatus.OFFHAND_CRITICAL;
                    }
                    break;
            }
        }

        return status;
    }

    /**
     * Every + 100 delta of (MR - MA) = + 10% to resist<br>
     * if the difference is 1000 = 100% resist
     */
    public static AttackStatus calculateMagicalStatus(Creature attacker, Creature attacked, int criticalProb, boolean isSkill) {
        if (!isSkill) {
            if (Rnd.get(0, 1000) < StatFunctions.calculateMagicalResistRate(attacker, attacked, 0)) {
                return AttackStatus.RESIST;
            }
        }

        if (StatFunctions.calculateMagicalCriticalRate(attacker, attacked, criticalProb)) {
            return AttackStatus.CRITICAL;
        }

        return AttackStatus.NORMALHIT;
    }

    public static void cancelCastOn(final Creature target) {
        target.getKnownList().doOnAllPlayers(new Visitor<Player>() {

            @Override
            public void visit(Player observer) {
                if (observer.getTarget() == target) {
                    AttackUtil.cancelCast(observer, target);
                }
            }
        });
        target.getKnownList().doOnAllNpcs(new Visitor<Npc>() {

            @Override
            public void visit(Npc observer) {
                if (observer.getTarget() == target) {
                    AttackUtil.cancelCast(observer, target);
                }
            }
        });
    }

    private static void cancelCast(Creature creature, Creature target) {
        if (target != null && creature.getCastingSkill() != null && creature.getCastingSkill()
                .getFirstTarget()
                .equals(target)) {
            creature.getController().cancelCurrentSkill();
        }
    }

    public static void removeTargetFrom(Creature object) {
        removeTargetFrom(object, false);
    }

    public static void removeTargetFrom(final Creature object, final boolean validateSee) {
        object.getKnownList().doOnAllPlayers(new Visitor<Player>() {
            @Override
            public void visit(Player other) {
                if (validateSee && (other.getTarget() == object)) {
                    if (!other.canSee(object)) {
                        other.setTarget(null);
                        other.getController().cancelCurrentSkill();
                        other.sendPck(new SM_TARGET_SELECTED(other));
                    }
                } else if (other.getTarget() == object) {
                    other.setTarget(null);
                    other.getController().cancelCurrentSkill();
                    other.sendPck(new SM_TARGET_SELECTED(other));
                }
            }
        });
    }

    public static void applyEffectOnCritical(Player attacker, Creature attacked) {
        int skillId = 0;
        WeaponType mainHandWeaponType = attacker.getEquipment().getMainHandWeaponType();
        if (mainHandWeaponType != null) {
            switch (mainHandWeaponType) {
                case POLEARM_2H:
                case STAFF_2H:
                case SWORD_2H:
                    skillId = 8218;
                    break;
                case BOW:
                    skillId = 8217;
                default:
                    break;
            }
        }

        if (skillId == 0) {
            return;
        }
        // On retail this effect apply on each crit with 10% of base chance
        // plus bonus effect penetration calculated above
        if (Rnd.get(100) > (10 * attacked.getCriticalEffectMulti())) {
            return;
        }

        SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skillId);
        if (template == null) {
            return;
        }
        Effect e = new Effect(attacker, attacked, template, template.getLvl(), 0);
        e.initialize();
        e.applyEffect();
    }
}
