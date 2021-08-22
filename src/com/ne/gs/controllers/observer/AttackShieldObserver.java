/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.observer;

import java.util.List;

import com.ne.commons.utils.Rnd;
import com.ne.gs.controllers.attack.AttackResult;
import com.ne.gs.controllers.attack.AttackStatus;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.summons.SummonMode;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.HealType;
import com.ne.gs.skillengine.model.HitType;
import com.ne.gs.utils.MathUtil;

/**
 * @author ATracer modified by Sippolo, kecimis
 */
public class AttackShieldObserver extends AttackCalcObserver {

    private final int hit;
    private int totalHit;
    private final boolean hitPercent;
    private final boolean totalHitPercent;
    private final Effect effect;
    private final HitType hitType;
    private final int shieldType;
    private int probability = 100;
    private int minradius = 0;
    private int maxradius = 100;
    private HealType healType = null;
    private boolean totalHitPercentSet = false;
    private int effectorDamage;

    public AttackShieldObserver(int hit, int totalHit, boolean percent, Effect effect, HitType type, int shieldType,
                                int probability) {
        this(hit, totalHit, percent, false, effect, type, shieldType, probability, 0, 100, null, 0);
    }

    public AttackShieldObserver(int hit, int effectorDamage, int totalHit, boolean percent, Effect effect, HitType type, int shieldType,
                                int probability) {
        this(hit, totalHit, percent, false, effect, type, shieldType, probability, 0, 100, null, effectorDamage);
    }

    public AttackShieldObserver(int hit, int totalHit, boolean hitPercent, boolean totalHitPercent, Effect effect,
                                HitType type, int shieldType, int probability, int minradius, int maxradius, HealType healType){
        this(hit, totalHit, hitPercent, totalHitPercent, effect, type, shieldType,probability,minradius, maxradius, healType, 0);
    }
    public AttackShieldObserver(int hit, int totalHit, boolean hitPercent, boolean totalHitPercent, Effect effect,
                                HitType type, int shieldType, int probability, int minradius, int maxradius, HealType healType, int effectorDamage) {
        this.hit = hit;
        this.totalHit = totalHit;// totalHit is radius
        this.effect = effect;
        this.hitPercent = hitPercent;
        this.totalHitPercent = totalHitPercent;
        hitType = type;
        this.shieldType = shieldType;
        this.probability = probability;
        this.minradius = minradius;// implemented only for reflected shield
        this.maxradius = maxradius;
        this.healType = healType;
        this.effectorDamage = effectorDamage;
    }

    @Override
    public void checkShield(List<AttackResult> attackList, Creature attacker) {
        for (AttackResult attackResult : attackList) {

            if (AttackStatus.getBaseStatus(attackResult.getAttackStatus()) == AttackStatus.DODGE
                || AttackStatus.getBaseStatus(attackResult.getAttackStatus()) == AttackStatus.RESIST) {
                continue;
            }
            // Handle Hit Types for Shields
            if (hitType != HitType.EVERYHIT) {
                if (attackResult.getDamageType() != null && attackResult.getDamageType() != hitType) {
                    continue;
                }
            }

            if (!Rnd.chance(probability)) {
                continue;
            }

            // shield type 2, normal shield
            if (shieldType == 2) {
                int damage = attackResult.getDamage();

                int absorbedDamage = 0;
                if (hitPercent) {
                    absorbedDamage = damage * hit / 100;
                } else {
                    absorbedDamage = damage >= hit ? hit : damage;
                }

                absorbedDamage = absorbedDamage >= totalHit ? totalHit : absorbedDamage;
                totalHit -= absorbedDamage;

                if (absorbedDamage > 0) {
                    attackResult.setShieldType(shieldType);
                }
                attackResult.setDamage(damage - absorbedDamage);

                // dont launch subeffect if damage is fully absorbed
                if (totalHit > 0) {
                    attackResult.setLaunchSubEffect(false);
                }

                if (totalHit <= 0) {
                    effect.endEffect();
                    return;
                }
            }
            // shield type 1, reflected damage
            else if (shieldType == 1) {
                // totalHit is radius
                if (minradius != 0) {
                    if (MathUtil.isIn3dRange(attacker, effect.getEffected(), minradius)) {
                        continue;
                    }
                }
                if (MathUtil.isIn3dRange(attacker, effect.getEffected(), maxradius)) {
                    int reflectedDamage = attackResult.getDamage() * totalHit / 100;
                    int reflectedHit = Math.max(reflectedDamage, hit);

                    if (attacker instanceof Npc) {
                        reflectedHit = attacker.getAi2().modifyReflectedDamage(reflectedHit);
                    }

                    attackResult.setShieldType(shieldType);
                    attackResult.setReflectedDamage(reflectedHit);
                    attackResult.setReflectedSkillId(effect.getSkillId());
                    attacker.getController().onAttack(effect.getEffected(), reflectedHit, false);

                    if (effect.getEffected() instanceof Player) {
                        ((Player) effect.getEffected()).sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_PROC_EFFECT_OCCURRED(effect.getSkillTemplate().getNameId()));
                    }
                }
                break;
            }
            // shield type 8, protect effect (ex. skillId: 417 Bodyguard I)
            else if (shieldType == 8) {
                // totalHit is radius
                if (effect.getEffector() == null || effect.getEffector().getLifeStats().isAlreadyDead()) {
                    effect.endEffect();
                    break;
                }

                if (((effect.getEffector() instanceof Summon))
                    && ((((Summon) effect.getEffector()).getMode() == SummonMode.RELEASE) || (((Summon) effect.getEffector()).getMaster() == null))) {
                    effect.endEffect();
                    break;
                }

                if (MathUtil.isIn3dRange(effect.getEffector(), effect.getEffected(), totalHit)) {
                    int damageProtected = 0;
                    int effectorDamage = 0;

                    if (hitPercent) {
                        damageProtected = ((int)(attackResult.getDamage() * hit * 0.01));
                        if (this.effectorDamage == 0)
                            this.effectorDamage = 100;
                        effectorDamage = ((int)(attackResult.getDamage() * this.effectorDamage * 0.01));
                    }
                    else
                        damageProtected = hit;

                    int finalDamage = attackResult.getDamage() - damageProtected;

                    attackResult.setDamage(finalDamage <= 0 ? 0 : finalDamage);
                    attackResult.setShieldType(shieldType);
                    attackResult.setProtectedSkillId(effect.getSkillId());
                    attackResult.setProtectedDamage(effectorDamage);
                    attackResult.setProtectorId(effect.getEffectorId());
                    effect.getEffector().getController().onAttack(attacker, effect.getSkillId(), TYPE.PROTECTDMG, effectorDamage, false, LOG.REGULAR, attackResult.getAttackStatus());
                }
            } else if (shieldType == 0) {
                int damage = attackResult.getDamage();

                int absorbedDamage = damage;

                if (totalHitPercent && !totalHitPercentSet) {
                    totalHit = (int) (totalHit * 0.01D * effect.getEffected().getGameStats().getHealth().getCurrent());
                    totalHitPercentSet = true;
                }

                absorbedDamage = absorbedDamage >= totalHit ? totalHit : absorbedDamage;
                totalHit -= absorbedDamage;

                attackResult.setDamage(damage - absorbedDamage);

                int healValue = 0;
                if (hitPercent) {
                    healValue = damage * hit / 100;
                } else {
                    healValue = hit;
                }
                switch (healType) {
                    case HP:
                        effect.getEffected().getLifeStats().increaseHp(TYPE.HP, healValue, effect.getSkillId(), LOG.REGULAR);
                        break;
                    case MP:
                        effect.getEffected().getLifeStats().increaseMp(TYPE.HEAL_MP, healValue, effect.getSkillId(), LOG.REGULAR);
                }

                if (absorbedDamage >= damage) {
                    attackResult.setLaunchSubEffect(false);
                }
                if (totalHit <= 0) {
                    effect.endEffect();
                    return;
                }
            }
        }
    }
}
