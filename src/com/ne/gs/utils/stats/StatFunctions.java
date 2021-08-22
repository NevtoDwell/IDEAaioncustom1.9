/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.stats;

import com.google.common.base.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.Rnd;
import com.ne.commons.utils.XMath;
import com.ne.gs.configs.main.FallDamageConfig;
import com.ne.gs.controllers.attack.AttackStatus;
import com.ne.gs.controllers.observer.AttackerCriticalStatus;
import com.ne.gs.model.SkillElement;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.Servant;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.model.gameobjects.Trap;
import com.ne.gs.model.gameobjects.player.Equipment;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.siege.SiegeNpc;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.siege.Influence;
import com.ne.gs.model.stats.calc.AdditionStat;
import com.ne.gs.model.stats.calc.Stat2;
import com.ne.gs.model.stats.container.CreatureGameStats;
import com.ne.gs.model.stats.container.PlayerGameStats;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.model.templates.item.WeaponStats;
import com.ne.gs.model.templates.item.WeaponType;
import com.ne.gs.model.templates.npc.NpcRating;
import com.ne.gs.model.templates.stats.NpcStatsTemplate;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.ne.gs.services.player.PlayerReviveService;

import static com.ne.gs.model.SkillElement.getResistanceForElement;

/**
 * @author ATracer
 * @author alexa026
 */
public final class StatFunctions {

    private static final Logger log = LoggerFactory.getLogger(StatFunctions.class);

    private static int getBaseXp(int maxXp, int maxHp) {
        return maxXp > maxHp * 2 ? maxXp : maxHp * 2;
    }

    /**
     * @param player
     * @param target
     *
     * @return XP reward from target
     */
    public static long calculateSoloExperienceReward(Player player, Creature target) {
        int playerLevel = player.getCommonData().getLevel();
        int targetLevel = target.getLevel();

        NpcStatsTemplate tpl = ((Npc) target).getObjectTemplate().getStatsTemplate();
        int baseXP = getBaseXp(tpl.getMaxXp(), tpl.getMaxHp());
        int xpPercentage = XPRewardEnum.xpRewardFrom(targetLevel - playerLevel);
        return Math.round(baseXP * (xpPercentage / 100d));
    }

    /**
     * @param target
     *
     * @return
     */
    public static long calculateGroupExperienceReward(int maxLevelInRange, Creature target) {
        int targetLevel = target.getLevel();
        NpcStatsTemplate tpl = ((Npc) target).getObjectTemplate().getStatsTemplate();
        int baseXP = getBaseXp(tpl.getMaxXp(), tpl.getMaxHp());
        int xpPercentage = XPRewardEnum.xpRewardFrom(targetLevel - maxLevelInRange);
        return Math.round(baseXP * (xpPercentage / 100d));
    }

    /**
     * ref: http://www.aionsource.com/forum/mechanic-analysis/42597-character-stats-xp-dp-origin-gerbator-team-july-2009- a.html
     *
     * @param player
     * @param target
     *
     * @return DP reward from target
     */

    public static int calculateSoloDPReward(Player player, Creature target) {
        int playerLevel = player.getCommonData().getLevel();
        int targetLevel = target.getLevel();
        NpcRating npcRating = ((Npc) target).getObjectTemplate().getRating();

        // TODO: fix to see monster Rating level, NORMAL lvl 1, 2 | ELITE lvl 1, 2 etc..
        // look at:
        // http://www.aionsource.com/forum/mechanic-analysis/42597-character-stats-xp-dp-origin-gerbator-team-july-2009-a.html
        int baseDP = targetLevel * calculateRatingMultipler(npcRating);

        int xpPercentage = XPRewardEnum.xpRewardFrom(targetLevel - playerLevel);
        float rate = player.getRates().getDpNpcRate();
        return (int) Math.floor(baseDP * xpPercentage * rate / 100);

    }

    /**
     * @param player
     * @param target
     *
     * @return AP reward
     */
    public static int calculatePvEApGained(Player player, Creature target) {
        float apPercentage = (target instanceof SiegeNpc) ? 100 : APRewardEnum.apReward(player.getAbyssRank().getRank().getId());
        boolean lvlDiff = player.getCommonData().getLevel() - target.getLevel() > 10;
        float apNpcRate = ApNpcRating(((Npc) target).getObjectTemplate().getRating());

        if (target.getName().equals("flame hoverstone")) {
            apNpcRate = 0.5F;
        }
        if ((target.getName().equals("controllera")) || (target.getName().equals("controllerb"))) {
            apNpcRate = 0.0F;
        }
        return lvlDiff ? 1 : (int) Math.floor(15 * apPercentage * player.getRates().getApNpcRate() * apNpcRate / 100);
    }

    /**
     * @param defeated
     * @param winner
     *
     * @return Points Lost in PvP Death
     */
    public static int calculatePvPApLost(Player defeated, Player winner) {
        int pointsLost = Math.round(defeated.getAbyssRank().getRank().getPointsLost() * defeated.getRates().getApPlayerLossRate());

        // Level penalty calculation
        int difference = winner.getLevel() - defeated.getLevel();

        if (difference > 4) {
            pointsLost = Math.round(pointsLost * 0.1f);
        } else {
            switch (difference) {
                case 3:
                    pointsLost = Math.round(pointsLost * 0.85f);
                    break;
                case 4:
                    pointsLost = Math.round(pointsLost * 0.65f);
                    break;
            }
        }
        return pointsLost;
    }

    /**
     * @param defeated
     *
     * @return Points Gained in PvP Kill
     */
    public static int calculatePvpApGained(Player defeated, int maxRank, int maxLevel) {
        int pointsGained = defeated.getAbyssRank().getRank().getPointsGained();

        // Level penalty calculation
        int difference = maxLevel - defeated.getLevel();

        if (difference > 4) {
            pointsGained = Math.round(pointsGained * 0.1f);
        } else if (difference < -3) {
            pointsGained = Math.round(pointsGained * 1.3f);
        } else {
            switch (difference) {
                case 3:
                    pointsGained = Math.round(pointsGained * 0.85f);
                    break;
                case 4:
                    pointsGained = Math.round(pointsGained * 0.65f);
                    break;
                case -2:
                    pointsGained = Math.round(pointsGained * 1.1f);
                    break;
                case -3:
                    pointsGained = Math.round(pointsGained * 1.2f);
                    break;
            }
        }

        // Abyss rank penalty calculation
        int defeatedAbyssRank = defeated.getAbyssRank().getRank().getId();
        int abyssRankDifference = maxRank - defeatedAbyssRank;

        if (maxRank <= 7 && abyssRankDifference > 0) {
            float penaltyPercent = abyssRankDifference * 0.05f;

            pointsGained -= Math.round(pointsGained * penaltyPercent);
        }

        return pointsGained;
    }

    /**
     * @param defeated
     *
     * @return XP Points Gained in PvP Kill TODO: Find the correct formula.
     */
    public static int calculatePvpXpGained(Player defeated, int maxRank, int maxLevel) {
        int pointsGained = 5000;

        // Level penalty calculation
        int difference = maxLevel - defeated.getLevel();

        if (difference > 4) {
            pointsGained = Math.round(pointsGained * 0.1f);
        } else if (difference < -3) {
            pointsGained = Math.round(pointsGained * 1.3f);
        } else {
            switch (difference) {
                case 3:
                    pointsGained = Math.round(pointsGained * 0.85f);
                    break;
                case 4:
                    pointsGained = Math.round(pointsGained * 0.65f);
                    break;
                case -2:
                    pointsGained = Math.round(pointsGained * 1.1f);
                    break;
                case -3:
                    pointsGained = Math.round(pointsGained * 1.2f);
                    break;
            }
        }

        // Abyss rank penalty calculation
        int defeatedAbyssRank = defeated.getAbyssRank().getRank().getId();
        int abyssRankDifference = maxRank - defeatedAbyssRank;

        if (maxRank <= 7 && abyssRankDifference > 0) {
            float penaltyPercent = abyssRankDifference * 0.05f;

            pointsGained -= Math.round(pointsGained * penaltyPercent);
        }

        return pointsGained;
    }

    public static int calculatePvpDpGained(Player defeated, int maxRank, int maxLevel) {
        int pointsGained;

        // base values
        int baseDp = 1064;
        int dpPerRank = 57;

        // adjust by rank
        pointsGained = (defeated.getAbyssRank().getRank().getId() - maxRank) * dpPerRank + baseDp;

        // adjust by level
        pointsGained = StatFunctions.adjustPvpDpGained(pointsGained, defeated.getLevel(), maxLevel);

        return pointsGained;
    }

    public static int adjustPvpDpGained(int points, int defeatedLvl, int killerLvl) {
        int pointsGained = points;

        int difference = killerLvl - defeatedLvl;
        // adjust by level
        if (difference >= 10) {
            pointsGained = 0;
        } else if (difference < 10 && difference >= 0) {
            pointsGained -= pointsGained * difference * 0.1;
        } else if (difference <= -10) {
            pointsGained *= 1.1;
        } else if (difference > -10 && difference < 0) {
            pointsGained += pointsGained * Math.abs(difference) * 0.01;
        }

        return pointsGained;
    }

    /**
     * @param player
     * @param target
     *
     * @return DP reward
     */
    public static int calculateGroupDPReward(Player player, Creature target) {
        int playerLevel = player.getCommonData().getLevel();
        int targetLevel = target.getLevel();
        NpcRating npcRating = ((Npc) target).getObjectTemplate().getRating();

        // TODO: fix to see monster Rating level, NORMAL lvl 1, 2 | ELITE lvl 1, 2 etc..
        int baseDP = targetLevel * calculateRatingMultipler(npcRating);
        int xpPercentage = XPRewardEnum.xpRewardFrom(targetLevel - playerLevel);
        float rate = player.getRates().getDpNpcRate();

        return (int) Math.floor(baseDP * xpPercentage * rate / 100);
    }

    /**
     * Hate based on BOOST_HATE stat Now used only from skills, probably need to use for regular attack
     *
     * @param creature
     * @param value
     *
     * @return
     */
    public static int calculateHate(Creature creature, int value) {
        Stat2 stat = new AdditionStat(StatEnum.BOOST_HATE, value, creature, 0.1f);
        return creature.getGameStats().getStat(StatEnum.BOOST_HATE, stat).getCurrent();
    }

    /**
     * @param target
     * @param isMainHand
     *
     * @return Damage made to target (-hp value)
     */
    public static int calculateAttackDamage(Creature attacker, Creature target, boolean isMainHand, SkillElement element) {
        int resultDamage;
        if (element == SkillElement.NONE) {
            resultDamage = calculatePhysicalAttackDamage(attacker, target, isMainHand);
        } else {
            resultDamage = calculateMagicalAttackDamage(attacker, target, element);
        }

        resultDamage = (int) adjustDamages(attacker, target, resultDamage, 0);

        if ((target instanceof Npc)) {
            return target.getAi2().modifyDamage(resultDamage);
        }
        if ((attacker instanceof Npc)) {
            return attacker.getAi2().modifyOwnerDamage(resultDamage);
        }
        return resultDamage;
    }

    public static int calculatePhysicalAttackDamage(Creature attacker, Creature target, boolean isMainHand) {
        Stat2 pAttack;
        if (isMainHand) {
            pAttack = attacker.getGameStats().getMainHandPAttack();
        } else {
            pAttack = ((Player) attacker).getGameStats().getOffHandPAttack();
        }
        float resultDamage = pAttack.getCurrent();
        if (attacker instanceof Player) {
            Equipment equipment = ((Player) attacker).getEquipment();
            Item weapon;
            if (isMainHand) {
                weapon = equipment.getMainHandWeapon();
            } else {
                weapon = equipment.getOffHandWeapon();
            }

            if (weapon != null) {
                WeaponStats weaponStat = weapon.getItemTemplate().getWeaponStats();
                if (weaponStat == null) {
                    return 0;
                }
                int totalMin = weaponStat.getMinDamage();
                int totalMax = weaponStat.getMaxDamage();
                if (totalMax - totalMin < 1) {
                    log.warn("Weapon stat MIN_MAX_DAMAGE resulted average zero in main-hand calculation");
                    log.warn("Weapon ID: " + String.valueOf(equipment.getMainHandWeapon().getItemTemplate().getTemplateId()));
                    log.warn("MIN_DAMAGE = " + String.valueOf(totalMin));
                    log.warn("MAX_DAMAGE = " + String.valueOf(totalMax));
                }
                float power = attacker.getGameStats().getPower().getCurrent() * 0.01f;
                int diff = totalMax - totalMin;
                resultDamage = (totalMax*power)-Rnd.get(diff);
                resultDamage = pAttack.getBonus() + getMovementModifier(attacker, StatEnum.PHYSICAL_ATTACK, resultDamage);

                // adjust with value from WeaponDualEffect
                // it makes lower cap of damage lower, so damage is more random on offhand
                

                
                // add powerShard damage
                if (attacker.isInState(CreatureState.POWERSHARD)) {
                    Item secondShard = null, firstShard;
                    if (weapon.getItemTemplate().isTwoHandWeapon()) {
                        firstShard = equipment.getMainHandPowerShard();
                        secondShard = equipment.getOffHandPowerShard();
                    } else if (isMainHand) {
                        firstShard = equipment.getMainHandPowerShard();
                    } else {
                        firstShard = equipment.getOffHandPowerShard();
                    }

                    if (firstShard != null) {
                        equipment.usePowerShard(firstShard, 1);
                        resultDamage += firstShard.getItemTemplate().getWeaponBoost();
                    }

                    if (secondShard != null) {
                        equipment.usePowerShard(secondShard, 1);
                        resultDamage += secondShard.getItemTemplate().getWeaponBoost();
                    }
                }

                // TODO move to controller
                if (weapon.getItemTemplate().getWeaponType() == WeaponType.BOW) {
                    equipment.useArrow();
                }
            } else {// if hand attack
                int totalMin = 16;
                int totalMax = 20;

                float power = attacker.getGameStats().getPower().getCurrent() * 0.01f;
                int diff = Math.round((totalMax - totalMin) * power / 2);
                resultDamage = pAttack.getBonus() + getMovementModifier(attacker, StatEnum.PHYSICAL_ATTACK, pAttack.getBase());
                resultDamage += Rnd.get(-diff, diff);
            }
        } else {
            int rnd = (int) (resultDamage * 0.25f);
            resultDamage += Rnd.get(-rnd, rnd);
        }

         
        resultDamage = calculatePdef(target,resultDamage, attacker);
        if(attacker.isInFlyingState()){
        	resultDamage += resultDamage*0.05f;
        }
        if (resultDamage <= 0) {
            resultDamage = 1;
        }

        return Math.round(resultDamage);
    }
    
    public static float calculatePdef(Object owner,float damage, Creature attacker){
    	float pDef = 0.0f;
    	float pDefbonus = 0.0f;
    	if (owner instanceof Creature){
    		pDef =((Creature)owner).getGameStats().getPDef().getBonus()
    	            + getMovementModifier(((Creature)owner), StatEnum.PHYSICAL_DEFENSE, ((Creature)owner).getGameStats().getPDef().getBase());
    	}
    	if(owner instanceof Player){
    		if(((Creature)owner).isInState(CreatureState.FLYING))
    			if(pDef > 1000.0f)
    				pDef = 1000.0f;
    			
    	}
    	if (attacker instanceof Player && owner instanceof Player){
    		pDefbonus = ((Player)owner).getGameStats().getStat(StatEnum.PVP_DEFEND_RATIO, 0).getCurrent()/10.0f;
    		pDefbonus -= ((Player)attacker).getGameStats().getStat(StatEnum.PVP_ATTACK_RATIO, 0).getCurrent()/10.0f;
    	}
    	float diffpDef = pDef / 50.0f;
    	diffpDef += pDefbonus;
    	diffpDef /= 100.0f;
    	return damage - (damage * diffpDef);
    }

    public static int calculateMagicalAttackDamage(Creature attacker, Creature target, SkillElement element) {
        Preconditions.checkNotNull(element, "Skill element should be NONE instead of null");
        Stat2 mAttack = attacker.getGameStats().getMAttack();
        float resultDamage = mAttack.getCurrent();
        if (attacker instanceof Player) {
        	
            Equipment equipment = ((Player) attacker).getEquipment();
            Item weapon = equipment.getMainHandWeapon();

            if (weapon != null) {
                WeaponStats weaponStat = weapon.getItemTemplate().getWeaponStats();
                if (weaponStat == null) {
                    return 0;
                }
                int totalMin = weaponStat.getMinDamage();
                int totalMax = weaponStat.getMaxDamage();
                if (totalMax - totalMin < 1) {
                    log.warn("Weapon stat MIN_MAX_DAMAGE resulted average zero in main-hand calculation");
                    log.warn("Weapon ID: " + String.valueOf(equipment.getMainHandWeapon().getItemTemplate().getTemplateId()));
                    log.warn("MIN_DAMAGE = " + String.valueOf(totalMin));
                    log.warn("MAX_DAMAGE = " + String.valueOf(totalMax));
                }
                float knowledge = attacker.getGameStats().getKnowledge().getCurrent() * 0.01f;
                int diff = Math.round((totalMax - totalMin) * knowledge / 2);
                resultDamage = mAttack.getBonus() + getMovementModifier(attacker, StatEnum.MAGICAL_ATTACK, mAttack.getBase());
                resultDamage += Rnd.get(-diff, diff);

                if (attacker.isInState(CreatureState.POWERSHARD)) {
                    Item firstShard = equipment.getMainHandPowerShard();
                    Item secondShard = equipment.getOffHandPowerShard();
                    if (firstShard != null) {
                        equipment.usePowerShard(firstShard, 1);
                        resultDamage += firstShard.getItemTemplate().getWeaponBoost();
                    }

                    /*if (secondShard != null) {
                        equipment.usePowerShard(secondShard, 1);
                        resultDamage += secondShard.getItemTemplate().getWeaponBoost();
                    
                    }*/
                    if(attacker.isInFlyingState()){
                    	resultDamage += resultDamage*0.05;
                    }
                }
            }
        }

        if (element != SkillElement.NONE) {
            float elementalDef = getMovementModifier(target, getResistanceForElement(element),
                target.getGameStats().getMagicalDefenseFor(element));
            resultDamage = Math.round(resultDamage * (1 - elementalDef / 1250f));
        }

        if (resultDamage <= 0) {
            resultDamage = 1;
        }

        return Math.round(resultDamage);
    }

    public static int calculateMagicalSkillDamage(Creature speller, Creature target, int baseDamages, int bonus,
                                                  SkillElement element, boolean useMagicBoost, boolean useKnowledge, boolean noReduce, int pvpDamage) {
        CreatureGameStats<?> sgs = speller.getGameStats();
        CreatureGameStats<?> tgs = target.getGameStats();

        int magicBoost = useMagicBoost ? sgs.getMBoost().getCurrent() : 0;

        magicBoost -= tgs.getMBResist().getCurrent();

        if (magicBoost < 0) {
            magicBoost = 0;
        } else if (magicBoost > 2700) {
            magicBoost += (magicBoost-2700)*0.3f;
        }

        int knowledge = useKnowledge ? sgs.getKnowledge().getCurrent() : 100;

        float damages = baseDamages * (knowledge / 100f + magicBoost / 1000f);

        damages = sgs.getStat(StatEnum.BOOST_SPELL_ATTACK, (int) damages).getCurrent();

        // add bonus damage
        damages += bonus;

        // element resist: fire, wind, water, eath
        //
        // 10 elemental resist ~ 1% reduce of magical baseDamages
        //
        if (!noReduce && element != SkillElement.NONE) {
            float elementalDef = getMovementModifier(target, getResistanceForElement(element), tgs.getMagicalDefenseFor(element));
            damages = Math.round(damages * (1 - (elementalDef / 1250f)));
        }

        damages = adjustDamages(speller, target, damages, pvpDamage);

        if (damages <= 0) {
            damages = 1;
        }
        if (target instanceof Npc) {
            return target.getAi2().modifyDamage((int) damages);
        }

        return Math.round(damages);
    }

    /**
     * Calculates MAGICAL CRITICAL chance
     *
     * @param attacker
     *
     * @return boolean
     */
    public static boolean calculateMagicalCriticalRate(Creature attacker, Creature attacked, int criticalProb) {
        int critical = attacker.getGameStats().getMCritical().getCurrent();
        critical = attacked.getGameStats().getPositiveReverseStat(StatEnum.MAGICAL_CRITICAL_RESIST, critical);

        critical = (int) (critical * (criticalProb / 100.0F));
        double criticalRate;
        if (critical <= 440) {
            criticalRate = critical * 0.1F;
        } else if (critical <= 600) {
            criticalRate = 44.0F + (critical - 440) * 0.05F;
        } else {
            criticalRate = 52.0F + (critical - 600) * 0.02F;
        }
        return Rnd.nextInt(100) < criticalRate;
    }

    /**
     * @param npcRating
     *
     * @return
     */
    public static int calculateRatingMultipler(NpcRating npcRating) {
        // FIXME: to correct formula, have any reference?
        int multipler;
        switch (npcRating) {
            case JUNK:
            case NORMAL:
                multipler = 2;
                break;
            case ELITE:
                multipler = 3;
                break;
            case HERO:
                multipler = 4;
                break;
            case LEGENDARY:
                multipler = 5;
                break;
            default:
                multipler = 1;
        }

        return multipler;
    }

    /**
     * @return
     */
    public static int ApNpcRating(NpcRating npcRating) {
        int multipler;
        switch (npcRating) {
            case JUNK:
                multipler = 1;
                break;
            case NORMAL:
                multipler = 2;
                break;
            case ELITE:
                multipler = 4;
                break;
            case HERO:
                multipler = 35;// need check
                break;
            case LEGENDARY:
                multipler = 2500;// need check
                break;
            default:
                multipler = 1;
        }

        return multipler;
    }

    /**
     * adjust baseDamages according to their level || is PVP?
     *
     * @param attacker
     *     lvl
     * @param target
     *     lvl
     *
     * @ref:
     */
    public static float adjustDamages(Creature attacker, Creature target, float damages, int pvpDamage) {
        // Artifacts haven't this limitation
        // TODO: maybe set correct artifact npc levels on npc_template.xml and delete this?
        if (attacker instanceof Npc) {
            if (attacker.getAi2() != null) {
                if (attacker.getAi2().getName().equalsIgnoreCase("artifact")) {
                    return damages;
                }
            }
        }
        if (attacker.isPvpTarget(target)) {
            if (pvpDamage > 0) {
                damages *= pvpDamage * 0.01;
            }

            // PVP damages is capped of 50% of the actual baseDamage
            damages = Math.round(damages * 0.50f);
            float pvpAttackBonus = attacker.getGameStats().getStat(StatEnum.PVP_ATTACK_RATIO, 0).getCurrent() * 0.001f;
            float pvpDefenceBonus = target.getGameStats().getStat(StatEnum.PVP_DEFEND_RATIO, 0).getCurrent() * 0.001f;
            damages = Math.round(damages + (damages * pvpAttackBonus) - (damages * pvpDefenceBonus));
            // Apply Race modifier
            if ((attacker.getRace() != target.getRace()) && (!attacker.isInInstance())) {
                damages *= Influence.getInstance().getPvpRaceBonus(attacker.getRace());
            }
        } else if (target instanceof Npc) {
            int levelDiff = target.getLevel() - attacker.getLevel();
            damages *= (1f - getNpcLevelDiffMod(levelDiff, 0));
        }

        return damages;
    }

    /**
     * Calculates DODGE chance
     *
     * @param attacker
     * @param attacked
     *
     * @return boolean
     */
    public static boolean calculatePhysicalDodgeRate(Creature attacker, Creature attacked, int accMod) {
        // check always dodge
        if (attacker.getObserveController().checkAttackerStatus(AttackStatus.DODGE)) {
            return true;
        }
        if (attacked.getObserveController().checkAttackStatus(AttackStatus.DODGE)) {
            return true;
        }

        float accuracy = attacker.getGameStats().getMainHandPAccuracy().getCurrent() + accMod;
        float dodge = attacked.getGameStats().getEvasion().getBonus()
            + getMovementModifier(attacked, StatEnum.EVASION, attacked.getGameStats().getEvasion().getBase());
        float dodgeRate = dodge - accuracy;
        if (attacked instanceof Npc) {
            int levelDiff = attacked.getLevel() - attacker.getLevel();
            dodgeRate *= 1 + getNpcLevelDiffMod(levelDiff, 0);
            if (((Npc) attacked).hasStatic()) {
                return false;
            }
        }
        int rate = GetDodgeDiff(dodgeRate);
        
        return Rnd.get(100)<=rate;
    }
    
    public static int GetDodgeDiff(float rate){
    	if(rate<0)
    		return 1; //over accuracy 1% max
    	if(rate < 100) //5% max
    		return 3 + (int)(rate/50);
    	
    	if(rate >= 100 && rate < 300) // 35% max 
    		return 5 + (int)(rate/10);
    	
    	if (rate >=300 && rate < 1000)  //75% max
    		return 40 + (int)(rate /30); 
    	
    	return 99;      //unattacked
    }
    

    /**
     * Calculates PARRY chance
     *
     * @param attacker
     * @param attacked
     *
     * @return int
     */
    public static boolean calculatePhysicalParryRate(Creature attacker, Creature attacked) {
        // check always parry
        if (attacked.getObserveController().checkAttackStatus(AttackStatus.PARRY)) {
            return true;
        }

        float accuracy = attacker.getGameStats().getMainHandPAccuracy().getCurrent();
        float parry = attacked.getGameStats().getParry().getBonus()
            + getMovementModifier(attacked, StatEnum.PARRY, attacked.getGameStats().getParry().getBase());
        float parryRate = parry - accuracy;
        return calculatePhysicalEvasion(parryRate, 400);
    }

    /**
     * Calculates BLOCK chance
     *
     * @param attacker
     * @param attacked
     *
     * @return int
     */
    public static boolean calculatePhysicalBlockRate(Creature attacker, Creature attacked) {
        // check always block
        if (attacked.getObserveController().checkAttackStatus(AttackStatus.BLOCK)) {
            return true;
        }

        float accuracy = attacker.getGameStats().getMainHandPAccuracy().getCurrent();

        float block = attacked.getGameStats().getBlock().getBonus()
            + getMovementModifier(attacked, StatEnum.BLOCK, attacked.getGameStats().getBlock().getBase());
        float blockRate = block - accuracy;
        // blockRate = blockRate*0.6f+50;
        if (blockRate > 500) {
            blockRate = 500;
        }
        return Rnd.nextInt(1000) < blockRate;
    }

    /**
     * Accuracy (includes evasion/parry/block formulas):
     * Accuracy formula is based on opponents evasion/parry/block vs your own Accuracy.
     * If your Accuracy is 300 or more above opponents evasion/parry/block then you can not be evaded, parried or blocked. <br>
     * https://docs.google.com/spreadsheet/ccc?key=0AqxBGNJV9RrzdF9tOWpwUlVLOXE5bVRWeHQtbGQxaUE&hl=en_US#gid=2
     */
    public static boolean calculatePhysicalEvasion(float diff, int upperCap) {
        diff = diff * 0.6f + 50;
        if (diff > upperCap) {
            diff = upperCap;
        }
        return Rnd.nextInt(1000) < diff;
    }

    /**
     * Calculates CRITICAL chance
     * http://www.wolframalpha.com/input/?i=quadratic+fit+%7B%7B300%2C+30.97%7D%2C+%7B320%2C+31.68%7D%2C+%7B340%2C+33.30%7D%2C+%7B360%
     * 2C+36.09%7D%2C+%7B380%2C+37.81
     * %7D%2C+%7B400%2C+40.72%7D%2C+%7B420%2C+42.12%7D%2C+%7B440%2C+44.03%7D%2C+%7B480%2C+44.66%7D%2C+%7B500%2C+45.96%7D%2C%7B604%2
     * C+51.84%7D%2C+%7B649%2C+52.69%7D%7D http://www.aionsource.com/topic/40542-character-stats-xp-dp-origin-gerbatorteam-july-2009/
     * http://www.wolframalpha.com/input/?i=-0.000126341+x%5E2%2B0.184411+x-13.7738
     * https://docs.google.com/spreadsheet/ccc?key=0AqxBGNJV9RrzdGNjbEhQNHN3S3M5bUVfUVQxRkVIT3c&hl=en_US#gid=0
     *
     * @param attacker
     *
     * @return double
     */
    public static boolean calculatePhysicalCriticalRate(Creature attacker, Creature attacked, boolean isMainHand, int criticalProb,
                                                        boolean isSkill) {
        int critical;
        if (attacker instanceof Player && !isMainHand) {
            critical = ((PlayerGameStats) attacker.getGameStats()).getOffHandPCritical().getCurrent();
        } else {
            critical = attacker.getGameStats().getMainHandPCritical().getCurrent();
        }

        AttackerCriticalStatus acStatus = attacker.getObserveController().checkAttackerCriticalStatus(AttackStatus.CRITICAL, isSkill);
        if (acStatus.isResult()) {
            if (acStatus.isPercent()) {
                critical = acStatus.getValue() * 10;
            } else {
                critical = acStatus.getValue();
            }
        }

        //critical = attacked.getGameStats().getPositiveReverseStat(StatEnum.PHYSICAL_CRITICAL_RESIST, critical);
        critical -= attacked.getGameStats().getPCDef().getCurrent();
        if(critical < 0)
            critical = 0;

        critical = (int) (critical * (criticalProb / 100.0F));
        double criticalRate;
        if (critical <= 440) {
            criticalRate = critical * 0.1F;
        } else if (critical <= 600) {
            criticalRate = 44.0F + (critical - 440) * 0.05F;
        } else {
            criticalRate = 56.0F + (critical - 680) * 0.02F;
        }
        return Rnd.nextInt(100) < criticalRate;
    }

    /**
     * Calculates RESIST chance
     * <p/>
     * шанс отражения = (маг.защ.цели - точн.маг.нап.) / 10 + мод.ур.
     * модификатор уровня = (ур.цели - ур.нап. - 2) * 10
     * <p/>
     * если (мод.ур. <= 0) мод.ур. = 1 (как указано в формуле resistRate)
     * если (мод.ур > 80) мод.ур = 80
     * если (шанс.отр > 95) шанс.отр = 95 (нельзя отразить больше 95% атак)
     *
     * @return int
     */
    public static int calculateMagicalResistRate(Creature attacker, Creature attacked, int accMod) {
		if (attacked.getObserveController().checkAttackStatus(AttackStatus.RESIST))
			return 1000;
		
		int attackerLevel = attacker.getLevel();
		int targetLevel = attacked.getLevel();
		if (attacker instanceof Player)
		{
			Player pl = (Player)attacker;
			switch(pl.getPlayerClass())
			{
			case CHANTER:
			case CLERIC:
			case PRIEST:
				
				break;
			case MAGE:
			case SORCERER:
			case SPIRIT_MASTER:
				accMod-=50.0f;
				break;
			case GLADIATOR:
			case RANGER:
			case ASSASSIN:
			case SCOUT:
			case TEMPLAR:
			case WARRIOR:
				accMod=accMod+200;
				break;
			default:
				break;
			
			}
		}
		if (attacker instanceof Trap)
		{
			accMod=accMod+700;
		}
		int resistRate = ((attacked.getGameStats().getMResist().getCurrent() + 100)
			- attacker.getGameStats().getMAccuracy().getCurrent()) - accMod;
		
		
		if (attacked instanceof Npc)
		{
			int rRate = attacked.getGameStats().getMResist().getCurrent();
			if (rRate>=5000)
				return 1000;
		}
		if (attacker instanceof Npc)
		{
			resistRate -= 600;
		}
		else if (attacker instanceof Summon)
		{
			resistRate -= 500;
		}
		if ((targetLevel - attackerLevel) > 2)
			resistRate += ((targetLevel - attackerLevel)) * 30;
		resistRate += resistRate/4; 
		if (attacked instanceof Servant)
		{
			resistRate = 1;
		}
		// if MR < MA - never resist
		if (resistRate <= 0) {
			resistRate = 1;// its 0.1% because its min possible
		}
		if (resistRate >= 1000)
			resistRate = 998;
		return resistRate;
	}

    /**
     * Calculates the fall damage
     *
     * @param player
     * @param distance
     *
     * @return True if the player is forced to his bind location.
     */
    public static boolean calculateFallDamage(Player player, float distance, boolean stoped) {
        if (player.isInvul()) {
            return false;
        }

        if (distance >= FallDamageConfig.MAXIMUM_DISTANCE_DAMAGE || !stoped) {
            player.getController().onStopMove();
            player.getFlyController().onStopGliding(false);
            // TODO [AT] don't call onDie directly - better reduce HP!
            player.getController().onDie(player, false);

            if (player.isInInstance()) {
                PlayerReviveService.instanceRevive(player);
            } else if (player.getKisk() != null) {
                PlayerReviveService.kiskRevive(player);
            } else {
                PlayerReviveService.bindRevive(player);
            }
            return true;
        } else if (distance >= FallDamageConfig.MINIMUM_DISTANCE_DAMAGE) {
            float dmgPerMeter = player.getLifeStats().getMaxHp() * FallDamageConfig.FALL_DAMAGE_PERCENTAGE / 100f;
            int damage = (int) (distance * dmgPerMeter);

            player.getLifeStats().reduceHp(damage, player);
            AionServerPacket packet = new SM_ATTACK_STATUS(player, SM_ATTACK_STATUS.TYPE.FALL_DAMAGE, 0, -damage);
            player.sendPck(packet);
        }

        return false;
    }

    public static float getMovementModifier(Creature creature, StatEnum stat, float value) {
        if (!(creature instanceof Player) || stat == null) {
            return value;
        }

        Player player = (Player) creature;
        int h = player.getMoveController().getMovementHeading();
        if (h < 0) {
            return value;
        }
        // 7 0 1
        // \ | /
        // 6- -2
        // / | \
        // 5 4 3
        switch (h) {
            case 7:
            case 0:
            case 1:
                switch (stat) {
                    case PHYSICAL_ATTACK:
                    case MAGICAL_ATTACK:
                        return value * 1.1f;
                    case WATER_RESISTANCE:
                    case WIND_RESISTANCE:
                    case FIRE_RESISTANCE:
                    case EARTH_RESISTANCE:
                    case ELEMENTAL_RESISTANCE_DARK:
                    case ELEMENTAL_RESISTANCE_LIGHT:
                    case PHYSICAL_DEFENSE:
                        return value * 0.8f;
                }
                break;
            case 6:
            case 2:
                switch (stat) {
                    case EVASION:
                        return value + 300;
                    case PHYSICAL_ATTACK:
                        return value * 0.3f;
                    case SPEED:
                        return value * 0.8f;
                }
                break;
            case 5:
            case 4:
            case 3:
                switch (stat) {
                    case PARRY:
                    case BLOCK:
                        return value + 500;
                    case PHYSICAL_ATTACK:
                        return value * 0.3f;
                    case SPEED:
                        return value * 0.6f;
                }
                break;
        }
        return value;
    }

    private static float getNpcLevelDiffMod(int levelDiff, int base) {
        switch (levelDiff) {
            case 3:
                return 0.1f;
            case 4:
                return 0.2f;
            case 5:
                return 0.3f;
            case 6:
                return 0.4f;
            case 7:
                return 0.5f;
            case 8:
                return 0.6f;
            case 9:
                return 0.7f;
            default:
                if (levelDiff > 9) {
                    return 0.8f;
                }
        }
        return base;
    }
}
