/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ne.gs.model.skill.SkillId;
import com.ne.gs.skillengine.action.HpUseAction;
import com.ne.gs.world.knownlist.Visitor;
import mw.engines.geo.GeoHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.Rnd;
import com.ne.gs.ai2.NpcAI2;
import com.ne.gs.ai2.handler.ShoutEventHandler;
import com.ne.gs.ai2.poll.AIQuestion;
import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.configs.main.GeoDataConfig;
import com.ne.gs.configs.main.SecurityConfig;
import com.ne.gs.controllers.attack.AttackStatus;
import com.ne.gs.controllers.observer.StartMovingListener;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.calc.Stat2;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.network.aion.serverpackets.SM_CASTSPELL;
import com.ne.gs.network.aion.serverpackets.SM_CASTSPELL_RESULT;
import com.ne.gs.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.ne.gs.network.aion.serverpackets.SM_QUIT_RESPONSE;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.restrictions.RestrictionsManager;
import com.ne.gs.services.MotionLoggingService;
import com.ne.gs.services.abyss.AbyssService;
import com.ne.gs.services.item.ItemPacketService.ItemUpdateType;
import com.ne.gs.skillengine.SkillEngine;
import com.ne.gs.skillengine.action.Action;
import com.ne.gs.skillengine.action.Actions;
import com.ne.gs.skillengine.condition.Conditions;
import com.ne.gs.skillengine.effect.AbnormalState;
import com.ne.gs.skillengine.properties.FirstTargetAttribute;
import com.ne.gs.skillengine.properties.Properties;
import com.ne.gs.skillengine.properties.TargetRangeAttribute;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.utils.audit.AuditLogger;

/**
 * @author ATracer Modified by Wakzashi
 */
public class Skill {

    private SkillMethod skillMethod = SkillMethod.CAST;

    private List<Creature> effectedList;

    private Creature firstTarget;

    private Creature effector;

    private int skillLevel;

    private int skillStackLvl;

    private StartMovingListener conditionChangeListener;

    private SkillTemplate skillTemplate;

    private boolean firstTargetRangeCheck = true;

    private ItemTemplate itemTemplate;
    private int itemObjectId = 0;

    private int targetType;

    private boolean chainSuccess;

    private boolean isCancelled = false;

    private float x;
    private float y;
    private float z;
    private int h;

    private int boostSkillCost;

    private FirstTargetAttribute firstTargetAttribute;
    private TargetRangeAttribute targetRangeAttribute;

    /**
     * Duration that depends on BOOST_CASTING_TIME
     */
    private int duration;
    private int hitTime;// from CM_CASTSPELL
    private int serverTime;// time when effect is applied

    private String chainCategory = null;
    private volatile boolean isMultiCast = false;
    private boolean isPetDopingSkill;

    private static final Logger log = LoggerFactory.getLogger(Skill.class);

    /**
     * Each skill is a separate object upon invocation Skill level will be
     * populated from player SkillList
     *
     * @param skillTemplate
     * @param effector
     */
    public Skill(SkillTemplate skillTemplate, Player effector, Creature firstTarget) {
        this(skillTemplate, effector, effector.getSkillList()
                .getSkillLevel(skillTemplate.getSkillId()), firstTarget, null);
    }

    public Skill(SkillTemplate skillTemplate, Player effector, Creature firstTarget,
            int skillLevel) {
        this(skillTemplate, effector, skillLevel, firstTarget, null);
    }

    /**
     * @param skillTemplate
     * @param effector
     * @param skillLvl
     * @param firstTarget
     */
    public Skill(SkillTemplate skillTemplate, Creature effector, int skillLvl,
            Creature firstTarget, ItemTemplate itemTemplate) {
        this.effectedList = new ArrayList<>();
        this.conditionChangeListener = new StartMovingListener();
        this.firstTarget = firstTarget;
        this.skillLevel = skillLvl;
        this.skillStackLvl = skillTemplate.getLvl();
        this.skillTemplate = skillTemplate;
        this.effector = effector;
        this.duration = skillTemplate.getDuration();
        this.itemTemplate = itemTemplate;

        if (itemTemplate != null) {
            skillMethod = SkillMethod.ITEM;
        } else if (skillTemplate.isPassive()) {
            skillMethod = SkillMethod.PASSIVE;
        } else if (skillTemplate.isProvoked()) {
            skillMethod = SkillMethod.PROVOKED;
        }
    }

    /**
     * Check if the skill can be used
     *
     * @return True if the skill can be used
     */
    public boolean canUseSkill() {
        Properties properties = skillTemplate.getProperties();
        if (properties != null && !properties.validate(this)) {
            log.debug("properties failed");
            return false;
        }

        if (!preCastCheck()) {
            return false;
        }

        // check for counter skill
        if (effector instanceof Player) {
            Player player = (Player) effector;
            if (this.skillTemplate.getCounterSkill() != null) {
                long time = player.getLastCounterSkill(skillTemplate.getCounterSkill());
                if ((time + 5000) < System.currentTimeMillis()) {
                    log.debug("chain skill failed, too late");
                    return false;
                }
            }
            if ((skillMethod == SkillMethod.ITEM) && (duration > 0) && (player.getMoveController().isInMove())) {
                player.sendPck(SM_SYSTEM_MESSAGE
                        .STR_ITEM_CANCELED(DescId.of(getItemTemplate().getNameId())));

                return false;
            }
        }

        Iterator<Creature> effectedIter = effectedList.iterator();
        while (effectedIter.hasNext()) {
            Creature effected = effectedIter.next();
            if (effected == null) {
                effected = effector;
            }

            if (effector instanceof Player) {
                if (!RestrictionsManager.canAffectBySkill((Player) effector, effected, this)) {
                    effectedIter.remove();
                }
            } else if (effector.getEffectController().isAbnormalState(AbnormalState.CANT_ATTACK_STATE)) {
                effectedIter.remove();
            }
        }

        // TODO: Enable non-targeted, non-point AOE skills to trigger.
        if (targetType == 0 && effectedList.isEmpty() && firstTargetAttribute != FirstTargetAttribute.ME && targetRangeAttribute
                != TargetRangeAttribute.AREA) {
            log.debug("targettype failed");
            return false;
        }
        return true;
    }

    public boolean useSkill() {
        return useSkill(true);
    }

    public boolean useNoAnimationSkill() {
        return useSkill(false);
    }

    private boolean useSkill(boolean checkAnimation) {
        if (!canUseSkill()) {
            return false;
        }

        if (this.getEffector() instanceof Player) {
            Player p = (Player) this.getEffector();
            if (p.isGM()) {
                PacketSendUtility.sendBrightYellowMessage(p, "SkillId: " + getSkillId());
            }
        }
        calculateSkillDuration();

        if (SecurityConfig.MOTION_TIME) {
            if (checkAnimation && !checkAnimationTime()) {
                log.debug("check animation time failed");
                return false;
            }
        }

        boostSkillCost = 0;

        // notify skill use observers
        if (skillMethod == SkillMethod.CAST) {
            effector.getObserveController().notifySkilluseObservers(this);
        }

        // start casting
        if (this.isPetDopingSkill && !effector.isCasting()) {
            effector.setCasting(this);
        } else if (!this.isPetDopingSkill) {
            effector.setCasting(this);
        }

        // log skill time if effector instance of player
        // TODO config
        if (effector instanceof Player) {
            MotionLoggingService.getInstance()
                    .logTime((Player) effector, this.getSkillTemplate(), this.getHitTime(), MathUtil
                            .getDistance(effector, firstTarget));
        }
        boolean setCooldowns = true;

        if ((effector instanceof Player) && isMulticast() && ((Player) effector).getChainSkills()
                .getChainCount((Player) effector, getSkillTemplate(), chainCategory) != 0) {
            setCooldowns = false;
            duration = 0;
        }

        if (setCooldowns) {
            setCooldowns();
        }

        // send packets to start casting
        if (skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.ITEM) {
            startCast();
        }

        effector.getObserveController().attach(conditionChangeListener);

        if (this.duration > 0) {
            schedule(this.duration);
        } else {
            endCast();
        }
        return true;
    }

    private void setCooldowns() {
        int cooldown = effector.getSkillCooldown(skillTemplate);
        if (cooldown != 0) {
            effector.setSkillCoolDown(skillTemplate.getCooldownId(), cooldown * 100 + duration + System
                    .currentTimeMillis());

            effector.setSkillCoolDownBase(skillTemplate.getCooldownId(), System.currentTimeMillis());
        }
    }

    protected void calculateSkillDuration() {
        // Skills that are not affected by boost casting time
        duration = 0;
        if (isCastTimeFixed()) {
            duration = skillTemplate.getDuration();
            return;
        }
        duration = effector.getGameStats()
                .getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME, skillTemplate.getDuration());
        switch (skillTemplate.getSubType()) {
            case SUMMON:
                duration = effector.getGameStats().getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME_SUMMON, duration);
                break;
            case SUMMONHOMING:
                duration = effector.getGameStats()
                        .getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME_SUMMONHOMING, duration);
                break;
            case SUMMONTRAP:
                duration = effector.getGameStats().getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME_TRAP, duration);
                break;
            case HEAL:
                duration = effector.getGameStats().getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME_HEAL, duration);
                break;
            case ATTACK:
                if (skillTemplate.getType() == SkillType.MAGICAL) {
                    duration = effector.getGameStats()
                            .getPositiveReverseStat(StatEnum.BOOST_CASTING_TIME_ATTACK, duration);
                }
                break;
        }

        // 70% of base skill duration cap
        // No cast speed cap for skill Summoning Alacrity I(skillId: 1778)
        if (!effector.getEffectController().hasAbnormalEffect(1778) && !effector.getEffectController()
                .hasAbnormalEffect(2386)) {
            int baseDurationCap = Math.round(skillTemplate.getDuration() * 0.3f);
            if (duration < baseDurationCap) {
                duration = baseDurationCap;
            }
        }

        if (duration < 0) {
            duration = 0;
        }
    }

    private boolean checkAnimationTime() {
        if (!(effector instanceof Player) || skillMethod != SkillMethod.CAST)// TODO item skills?
        {
            return true;
        }
        Player player = (Player) effector;

        // if player is without weapon, dont check animation time
        if (player.getEquipment().getMainHandWeaponType() == null) {
            return true;
        }

        /**
         * exceptions for certain skills -herb and mana treatment -traps
         */
        // dont check herb and mana treatment
        switch (this.getSkillId()) {
            case 1078:
            case 1125:
            case 1468:
            case 1803:
            case 1804:
            case 1805:
            case 1823:
            case 1824:
            case 1825:
            case 1826:
            case 1827:
            case 1828:
            case 2672:
            case 2673:
            case 11580:
                return true;
        }
        if (this.getSkillTemplate().getSubType() == SkillSubType.SUMMONTRAP) {
            return true;
        }

        Motion motion = this.getSkillTemplate().getMotion();

        if (motion == null || motion.getName() == null) {
            log.warn("missing motion for skillId: " + getSkillId());
            return true;
        }

        if (motion.getInstantSkill() && hitTime != 0) {
            log.warn("Instant and hitTime not 0! modified client_skills? player objectid: " + player.getObjectId());
            return false;
        } else if (!motion.getInstantSkill() && hitTime == 0) {
            log.warn("modified client_skills! player objectid: " + player.getObjectId());
            return false;
        }

        MotionTime motionTime = DataManager.MOTION_DATA.getMotionTime(motion.getName());

        if (motionTime == null) {
            log.warn("missing motiontime for motionName: " + motion.getName() + " skillId: " + getSkillId());
            return true;
        }

        WeaponTypeWrapper weapons = new WeaponTypeWrapper(player.getEquipment().getMainHandWeaponType(), player
                .getEquipment().getOffHandWeaponType());
        float serverTime = motionTime.getTimeForWeapon(weapons);
        int clientTime = hitTime;

        if (serverTime == 0) {
            log.warn("missing weapon time for motionName: " + motion.getName() + " weapons: " + weapons
                    .toString() + " skillId: " + getSkillId());
            return true;
        }

        // adjust client time with ammotime
        long ammoTime = 0;
        double distance = MathUtil.getDistance(effector, firstTarget);
        if (getSkillTemplate().getAmmoSpeed() != 0) {
            ammoTime = Math.round(distance / getSkillTemplate().getAmmoSpeed() * 1000);// checked with client
        }
        clientTime -= ammoTime;

        // adjust servertime with motion play speed
        if (motion.getSpeed() != 100) {
            serverTime /= 100f;
            serverTime *= (float) motion.getSpeed();
        }

        Stat2 attackSpeed = player.getGameStats().getAttackSpeed();

        // adjust serverTime with attackSpeed
        if (attackSpeed.getBase() != attackSpeed.getCurrent()) {
            serverTime *= ((float) attackSpeed.getCurrent() / (float) attackSpeed.getBase());
        }

        // 10% tolerance
        serverTime *= 0.9f;

        int finalTime = Math.round(serverTime);
        if (motion.getInstantSkill() && hitTime == 0) {
            this.serverTime = (int) ammoTime;
        } else {
            if (clientTime < finalTime) {
                // temp log to console for debugging
                if (SecurityConfig.NO_ANIMATION) {
                    float checkTme = clientTime / serverTime;

                    if (clientTime < 0 || checkTme < SecurityConfig.NO_ANIMATION_VALUE) {
                        if (SecurityConfig.NO_ANIMATION_KICK) {
                            player.getClientConnection().close(new SM_QUIT_RESPONSE(), false);
                            AuditLogger.info(player,
                                    "Modified client_skills:" + getSkillId() + " (clientTime<finalTime:" + clientTime
                                    + "/" + finalTime + ") Kicking Player: " + player
                                    .getName());
                        } else {
                            AuditLogger
                                    .info(player, "Modified client_skills:" + getSkillId() + " (clientTime<finalTime:"
                                            + clientTime + "/" + finalTime + ")");
                        }
                        return false;
                    }
                }
                //log.warn("Possible modified client_skills:" + getSkillId() + " (clientTime<finalTime:" + clientTime
                // + "/" + finalTime + ") player Name: " + player.getName());
            }
            this.serverTime = hitTime;
        }
        player.setNextSkillUse(System.currentTimeMillis() + duration + finalTime);
        return true;
    }

    /**
     * Penalty success skill
     */
    private void startPenaltySkill() {
        int penaltySkill = skillTemplate.getPenaltySkillId();
        if (penaltySkill == 0) {
            return;
        }

        SkillEngine.getInstance().applyEffectDirectly(penaltySkill, firstTarget, effector, 0);
    }

    /**
     * Start casting of skill
     */
    private void startCast() {
        int targetObjId = firstTarget != null ? firstTarget.getObjectId() : 0;

        if (skillMethod == SkillMethod.CAST) {
            switch (targetType) {
                case 0: // PlayerObjectId as Target

                    PacketSendUtility
                            .broadcastPacketAndReceive(effector, new SM_CASTSPELL(effector.getObjectId(), skillTemplate
                                    .getSkillId(), skillLevel, targetType, targetObjId,
                                    this.duration));
                    if (effector instanceof Npc && firstTarget instanceof Player) {
                        NpcAI2 ai = (NpcAI2) effector.getAi2();
                        if (ai.poll(AIQuestion.CAN_SHOUT)) {
                            ShoutEventHandler.onCast(ai, firstTarget);
                        }
                    }
                    break;

                case 3: // Target not in sight?
                    PacketSendUtility
                            .broadcastPacketAndReceive(effector, new SM_CASTSPELL(effector.getObjectId(), skillTemplate
                                    .getSkillId(), skillLevel, targetType, 0, this.duration));
                    break;

                case 1: // XYZ as Target
                    PacketSendUtility.broadcastPacketAndReceive(effector,
                            new SM_CASTSPELL(effector.getObjectId(), skillTemplate
                                    .getSkillId(), skillLevel, targetType, x, y, z, this.duration));
                    break;
            }
        } else if (skillMethod == SkillMethod.ITEM && duration > 0) {
            PacketSendUtility
                    .broadcastPacketAndReceive(effector,
                            new SM_ITEM_USAGE_ANIMATION(effector.getObjectId(), targetObjId, this.itemObjectId, itemTemplate.getTemplateId(), this.duration, 0, 0));
        }
    }

    /**
     * Set this skill as canceled
     */
    public void cancelCast() {
        isCancelled = true;
    }

    /**
     * Apply effects and perform actions specified in skill template
     */
    private void endCast() {
        if (!effector.isCasting() || isCancelled) {
            return;
        }

        // if target out of range
        if (skillTemplate == null) {
            return;
        }

        // Check if target is out of skill range
        Properties properties = skillTemplate.getProperties();
        if (properties != null && !properties.endCastValidate(this)) {
            effector.getController().cancelCurrentSkill();
            return;
        }

        if (!preUsageCheck()) {
            return;
        }

        if (this.isPetDopingSkill && effector.getCastingSkill().getSkillId() == this.skillTemplate.getSkillId()) {
            effector.setCasting(null);
        } else if (!this.isPetDopingSkill) {
            effector.setCasting(null);
        }

		if (this.getSkillTemplate().isDeityAvatar() && effector instanceof Player) {
			AbyssService.rankerSkillAnnounce((Player) effector, this.getSkillTemplate().getNameId());
		}
        
        /**
         * try removing item, if its not possible return to prevent exploits
         */
        if (effector instanceof Player && skillMethod == SkillMethod.ITEM) {
            Item item = ((Player) effector).getInventory().getItemByObjId(this.itemObjectId);
            if (item == null) {
                return;
            }
            if (item.getActivationCount() > 1) {
                item.setActivationCount(item.getActivationCount() - 1);
            } else if (!((Player) effector).getInventory()
                    .decreaseByObjectId(item.getObjectId(), 1, ItemUpdateType.DEC_USE)) {
                return;
            }
        }

        //sry for that
        //FIXME temporary fix for issue with 'Benevolence I' and 'Sacrificial Power I'
        if(skillTemplate.skillId == 1032)
            effector.getEffectController().removeNoshowEffect(2371);
        else if(skillTemplate.skillId == 2371)
            effector.getEffectController().removeNoshowEffect(1032);

        /**
         * Create effects and precalculate result
         */
        int spellStatus = 0;
        int dashStatus = 0;
		
        boolean blockedChain = false;
		boolean blockedStance = false;

        final List<Effect> effects = new ArrayList<>();
        if (skillTemplate.getEffects() != null) {
			
            boolean blockAOESpread = false;

            blockedChain = !effectedList.isEmpty();

            for (Creature effected : effectedList) {
                Effect effect = new Effect(this, effected, 0, itemTemplate);
				if (effected instanceof Player) {
                    if (effect.getEffectResult() == EffectResult.CONFLICT) {
                        blockedStance = true;
                    }
                }
                // Force RESIST status if AOE spell spread must be blocked
                if (blockAOESpread) {
                    effect.setAttackStatus(AttackStatus.RESIST);
                }
                effect.initialize();
                int worldId = effector.getWorldId();
                int instanceId = effector.getInstanceId();
                effect.setWorldPosition(worldId, instanceId, x, y, z);

                checkSkillSetException(effected);
                effects.add(effect);
                spellStatus = effect.getSpellStatus().getId();
                dashStatus = effect.getDashStatus().getId();

                // Block AOE propagation if firstTarget resists the spell
                if ((!blockAOESpread) && (effect.getAttackStatus() == AttackStatus.RESIST) && (isTargetAOE())) {
                    blockAOESpread = true;
                }

                boolean resisted = (effect.getAttackStatus() == AttackStatus.RESIST) ||
                        (effect.getAttackStatus() == AttackStatus.DODGE);

                blockedChain &=
                        resisted;
            }

            if (effectedList.isEmpty() && this.isPointPointSkill()) {
                Effect effect = new Effect(this, null, 0, itemTemplate);
                effect.initialize();
                int worldId = effector.getWorldId();
                int instanceId = effector.getInstanceId();
                effect.setWorldPosition(worldId, instanceId, x, y, z);
                effects.add(effect);
                spellStatus = effect.getSpellStatus().getId();
            }
        }

        if ((effector instanceof Player) && skillMethod == SkillMethod.CAST) {
            Player playerEffector = (Player) effector;
            if (playerEffector.getController().isUnderStance()) {
                playerEffector.getController().stopStance();
            }
            if (skillTemplate.isStance() && !blockedStance) {
                playerEffector.getController().startStance(skillTemplate.getSkillId());
            }
        }

        // Check Chain Skill Trigger Rate
        if (CustomConfig.SKILL_CHAIN_TRIGGERRATE) {
            int chainProb = skillTemplate.getChainSkillProb();
            if (chainProb != 0 && !blockedChain) {
                this.chainSuccess = Rnd.get(90) < chainProb;
            }

        } else {
            this.chainSuccess = true;
        }

        /**
         * set variables for chaincondition check
         */
        if (((effector instanceof Player)) && (chainSuccess) && (chainCategory != null)) {
            ((Player) effector).getChainSkills().addChainSkill(chainCategory, isMulticast());
        }


        List<Action> _lazyActions = new ArrayList<>();
        /**
         * Perform necessary actions (use mp,dp items etc)
         */
        Actions skillActions = skillTemplate.getActions();
        if (skillActions != null) {
            for (Action action : skillActions.getActions()) {

                //prevent Noble Energy IV npcs death
                if(!(effector instanceof Player) && action instanceof HpUseAction)
                    _lazyActions.add(action);
                else
                    action.act(this);
            }
        }

        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                applyEffect(effects);
            }
        }, hitTime);

        // send packets to end casting
        if (skillMethod == SkillMethod.CAST || skillMethod == SkillMethod.ITEM) {
            sendCastspellEnd(spellStatus, dashStatus, effects);
        }

        if(_lazyActions.size() > 0){
            for (Action action : _lazyActions) {
                action.act(this);
            }
        }
    }

    public void applyEffect(List<Effect> effects) {
        /**
         * Apply effects to effected objects
         */

        for (Effect effect : effects) {
            effect.applyEffect();
        }

        if (effector instanceof Player) {
            final Player player = (Player) effector;
            if (ArrayUtils.contains(SkillId.TransformationGuardianGeneral.skillIds, getSkillId())) {
                final SM_SYSTEM_MESSAGE msg = SM_SYSTEM_MESSAGE.STR_SKILL_ABYSS_SKILL_IS_FIRED(player, DescId.of(getSkillTemplate().getNameId()));
                player.getPosition().getWorldMapInstance().doOnAllPlayers(new Visitor<Player>() {
                    @Override
                    public void visit(Player p) {
                        if (p != player) {
                            p.sendPck(msg);
                        }
                    }
                });
            }
        }

        //TODO unfortianaly, i don't know mechanic, but possible if no creatures affected, penalty skill must be skipped for all skills?
        if(getSkillId() == 2269|| getSkillId() == 2270|| getSkillId() == 2460)
        {
            if(effectedList.isEmpty())
                return;
        }
        /**
         * Use penalty skill (now 100% success)
         */
        startPenaltySkill();
    }

    /**
     * @param spellStatus
     * @param effects
     */
    private void sendCastspellEnd(int spellStatus, int dashStatus, List<Effect> effects) {
        if (skillMethod == SkillMethod.CAST) {
            switch (targetType) {
                case 0: // PlayerObjectId as Target
                    PacketSendUtility
                            .broadcastPacketAndReceive(effector, new SM_CASTSPELL_RESULT(this, effects, serverTime,
                                    chainSuccess, spellStatus, dashStatus));
                    break;

                case 3: // Target not in sight?
                    PacketSendUtility
                            .broadcastPacketAndReceive(effector, new SM_CASTSPELL_RESULT(this, effects, serverTime,
                                    chainSuccess, spellStatus, dashStatus));
                    break;

                case 1: // XYZ as Target
                    PacketSendUtility
                            .broadcastPacketAndReceive(effector, new SM_CASTSPELL_RESULT(this, effects, serverTime,
                                    chainSuccess, spellStatus, dashStatus, 1));
                    break;
            }
        } else if (skillMethod == SkillMethod.ITEM) {
            PacketSendUtility
                    .broadcastPacketAndReceive(effector, new SM_ITEM_USAGE_ANIMATION(effector.getObjectId(), firstTarget
                            .getObjectId(), (this.itemObjectId == 0 ? 0
                                    : this.itemObjectId), itemTemplate.getTemplateId(), 0, 1, 0));
            if (effector instanceof Player) {
                ((Player) effector).sendPck(SM_SYSTEM_MESSAGE
                        .STR_USE_ITEM(DescId.of(getItemTemplate().getNameId())));
            }
        }
    }

    /**
     * Schedule actions/effects of skill (channeled skills)
     */
    private void schedule(int delay) {
        ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                endCast();
            }
        }, delay);
    }

    /**
     * Check all conditions before starting cast
     */
    private boolean preCastCheck() {
        Conditions skillConditions = skillTemplate.getStartconditions();
        return skillConditions == null || skillConditions.validate(this);
    }

    /**
     * Check all conditions before using skill
     */
    private boolean preUsageCheck() {
        Conditions skillConditions = skillTemplate.getUseconditions();
        return skillConditions == null || skillConditions.validate(this);
    }

    private void checkSkillSetException(Creature effected) {
        int setNumber = skillTemplate.getSkillSetException();
        int maxOccur = skillTemplate.getSkillSetMaxOccur();
        if (setNumber != 0) {
            if (maxOccur != 0) {
                effected.getEffectController().removeEffectBySetNumber(setNumber, maxOccur, skillTemplate.getSkillId());
            } else {
                effected.getEffectController().removeEffectBySetNumber(setNumber, 1);
            }
        } else {
            effected.getEffectController().removeEffectWithSetNumberReserved();
        }
    }

    /**
     * @param value is the changeMpConsumptionValue to set
     */
    public void setBoostSkillCost(int value) {
        boostSkillCost = value;
    }

    /**
     * @return the changeMpConsumptionValue
     */
    public int getBoostSkillCost() {
        return boostSkillCost;
    }

    /**
     * @return the effectedList
     */
    public List<Creature> getEffectedList() {
        return effectedList;
    }

    public boolean useWithoutPropSkill() {
        return useSkill(false);
    }

    /**
     * @return the effector
     */
    public Creature getEffector() {
        return effector;
    }

    /**
     * @return the skillLevel
     */
    public int getSkillLevel() {
        return skillLevel;
    }

    /**
     * @return the skillId
     */
    public int getSkillId() {
        return skillTemplate.getSkillId();
    }

    /**
     * @return the skillStackLvl
     */
    public int getSkillStackLvl() {
        return skillStackLvl;
    }

    /**
     * @return the conditionChangeListener
     */
    public StartMovingListener getConditionChangeListener() {
        return conditionChangeListener;
    }

    /**
     * @return the skillTemplate
     */
    public SkillTemplate getSkillTemplate() {
        return skillTemplate;
    }

    /**
     * @return the firstTarget
     */
    public Creature getFirstTarget() {
        return firstTarget;
    }

    /**
     * @param firstTarget the firstTarget to set
     */
    public void setFirstTarget(Creature firstTarget) {
        this.firstTarget = firstTarget;
    }

    /**
     * @return true or false
     */
    public boolean isPassive() {
        return skillTemplate.getActivationAttribute() == ActivationAttribute.PASSIVE;
    }

    /**
     * @return the firstTargetRangeCheck
     */
    public boolean isFirstTargetRangeCheck() {
        return firstTargetRangeCheck;
    }

    public void setFirstTargetAttribute(FirstTargetAttribute firstTargetAttribute) {
        this.firstTargetAttribute = firstTargetAttribute;
    }

    /**
     * @return true if the present skill is a non-targeted, non-point AOE skill
     */
    public boolean checkNonTargetAOE() {
        return (firstTargetAttribute == FirstTargetAttribute.ME && targetRangeAttribute == TargetRangeAttribute.AREA);
    }

    /**
     * @return true if the present skill is a targeted AOE skill
     */
    public boolean isTargetAOE() {
        return (firstTargetAttribute == FirstTargetAttribute.TARGET && targetRangeAttribute == TargetRangeAttribute.AREA);
    }

    /**
     * @return true if the present skill is a self buff includes items (such as
     * scroll buffs)
     */
    public boolean isSelfBuff() {
        return (firstTargetAttribute == FirstTargetAttribute.ME) && (targetRangeAttribute == TargetRangeAttribute.ONLYONE) && (skillTemplate
                .getSubType() == SkillSubType.BUFF);
    }

    /**
     * @return true if the present skill has self as first target
     */
    public boolean isFirstTargetSelf() {
        return firstTargetAttribute == FirstTargetAttribute.ME;
    }

    public boolean isPointSkill() {
        return firstTargetAttribute == FirstTargetAttribute.POINT;
    }

    public void setFirstTargetRangeCheck(boolean firstTargetRangeCheck) {
        this.firstTargetRangeCheck = firstTargetRangeCheck;
    }

    /**
     * @param itemTemplate the itemTemplate to set
     */
    public void setItemTemplate(ItemTemplate itemTemplate) {
        this.itemTemplate = itemTemplate;
    }

    public ItemTemplate getItemTemplate() {
        return this.itemTemplate;
    }

    public void setItemObjectId(int id) {
        this.itemObjectId = id;
    }

    public int getItemObjectId() {
        return this.itemObjectId;
    }

    /**
     * @param targetRangeAttribute the targetRangeAttribute to set
     */
    public void setTargetRangeAttribute(TargetRangeAttribute targetRangeAttribute) {
        this.targetRangeAttribute = targetRangeAttribute;
    }

    /**
     * @param targetType
     * @param x
     * @param y
     * @param z
     */
    public void setTargetType(int targetType, float x, float y, float z) {
        this.targetType = targetType;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Calculated position after skill
     *
     * @param x
     * @param y
     * @param z
     * @param h
     */
    public void setTargetPosition(float x, float y, float z, int h) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.h = h;
    }

    public void setDuration(int t) {
        this.duration = t;
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

    public final int getH() {
        return h;
    }

    /**
     * @return Returns the time.
     */
    public int getHitTime() {
        return hitTime;
    }

    /**
     * @param time The time to set.
     */
    public void setHitTime(int time) {
        this.hitTime = time;
    }

    /**
     * @return true if skill must not be affected by boost casting time this
     * comes from old 1.5.0.5 patch notes and still applies on 2.5 (confirmed)
     * TODO: maybe another implementation? At the moment this doesnt seem to be
     * handled on client infos, so it's hard coded
     */
    private boolean isCastTimeFixed() {
        if (skillMethod != SkillMethod.CAST) // only casted skills are affected
        {
            return true;
        }

        switch (this.getSkillId()) {
            case 1430:
            case 1443:
            case 1454:
            case 1495:
            case 1497:
            case 1636:
            case 1685:
            case 1801:
            case 1803:
            case 1804:
            case 1805:
            case 1823:
            case 1824:
            case 1825:
            case 1826:
            case 1827:
            case 1828:
            case 2006:
            case 2011:
            case 2316:
            case 2317:
            case 2318:
            case 2319:
            case 11885:
            case 11886:
            case 11887:
            case 11888:
            case 11889:
            case 11890:
            case 11891:
            case 11892:
            case 11893:
            case 11894:
                return true;
        }

        return false;
    }

    public boolean isGroundSkill() {
        return skillTemplate.isGroundSkill();
    }

    public boolean shouldAffectTarget(VisibleObject object) {
        if ((isGroundSkill())
                && ((object.getZ() - GeoHelper.getZ(object) > 1.0F) || (object.getZ() - GeoHelper.getZ(object) < -2.0F))) {
            return false;
        }
        return true;
    }

    public void setChainCategory(String chainCategory) {
        this.chainCategory = chainCategory;
    }

    public SkillMethod getSkillMethod() {
        return this.skillMethod;
    }

    public boolean isPointPointSkill() {
        if (this.getSkillTemplate().getProperties().getFirstTarget() == FirstTargetAttribute.POINT
                && this.getSkillTemplate().getProperties().getTargetType() == TargetRangeAttribute.POINT) {
            return true;
        }

        return false;
    }

    public boolean isMulticast() {
        return isMultiCast;
    }

    public void setIsMultiCast(boolean isMultiCast) {
        this.isMultiCast = isMultiCast;
    }

    public enum SkillMethod {
        CAST,
        ITEM,
        PASSIVE,
        PROVOKED;
    }

    public boolean isPetDopingSkill() {
        return isPetDopingSkill;
    }

    public void setPetDopingSkill(boolean isPetDopingSkill) {
        this.isPetDopingSkill = isPetDopingSkill;
    }
}
