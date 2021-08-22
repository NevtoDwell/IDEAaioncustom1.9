/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers;

import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.Future;

import com.ne.commons.utils.EventNotifier;
import com.ne.gs.model.events.PlayerSpawn;
import com.ne.gs.model.gameobjects.siege.SiegeNpc;
import com.ne.gs.model.templates.npc.AbyssNpcType;
import com.ne.gs.services.custom.CustomQuestsService;
import javolution.util.FastMap;
import mw.engines.geo.GeoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.annotations.NotNull;
import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.configs.main.HTMLConfig;
import com.ne.gs.configs.main.MembershipConfig;
import com.ne.gs.configs.main.SecurityConfig;
import static com.ne.gs.controllers.WindstreamController.WINDSTREAM_EXIT;
import com.ne.gs.controllers.attack.AttackStatus;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.DescId;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.WindstreamAction;
import com.ne.gs.model.actions.PlayerMode;
import com.ne.gs.model.gameobjects.*;
import com.ne.gs.model.gameobjects.player.AbyssRank;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.gameobjects.state.CreatureVisualState;
import com.ne.gs.model.handlers.EffectResurrectBaseHandler;
import com.ne.gs.model.skill.PlayerSkillEntry;
import com.ne.gs.model.stats.container.PlayerGameStats;
import com.ne.gs.model.summons.SummonMode;
import com.ne.gs.model.summons.UnsummonType;
import com.ne.gs.model.team2.group.PlayerFilters.ExcludePlayerFilter;
import com.ne.gs.model.templates.flypath.FlyPathEntry;
import com.ne.gs.model.templates.panels.SkillPanel;
import com.ne.gs.model.templates.quest.QuestItems;
import com.ne.gs.model.templates.stats.PlayerStatsTemplate;
import com.ne.gs.modules.housing.HouseInfo;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.serverpackets.*;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.restrictions.RestrictionsManager;
import com.ne.gs.services.*;
import com.ne.gs.services.abyss.AbyssService;
import com.ne.gs.services.craft.CraftSkillUpdateService;
import com.ne.gs.services.instance.InstanceService;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.services.summons.SummonsService;
import com.ne.gs.skillengine.SkillEngine;
import com.ne.gs.skillengine.model.DispelCategoryType;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.HealType;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.skillengine.model.Skill.SkillMethod;
import com.ne.gs.skillengine.model.SkillTemplate;
import com.ne.gs.taskmanager.tasks.PlayerMoveTaskManager;
import com.ne.gs.taskmanager.tasks.TeamEffectUpdater;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.utils.audit.AuditLogger;
import com.ne.gs.world.MapRegion;
import com.ne.gs.world.WorldType;
import com.ne.gs.world.zone.ZoneInstance;
import com.ne.gs.world.zone.ZoneName;
import com.ne.gs.services.DuelService;
import static com.ne.gs.modules.housing.House.HouseType.*;
import com.ne.gs.skillengine.model.SkillTargetSlot;
import static com.ne.gs.utils.PacketSendUtility.broadcastPacket;

/**
 * This class is for controlling players.
 *
 * @author -Nemesiss-, ATracer, xavier, Sarynth, RotO, xTz, KID modified by Sippolo
 */
public class PlayerController extends CreatureController<Player> {

    private static final Logger log = LoggerFactory.getLogger(PlayerController.class);

    /* Delay, before custom quests will be updated after player upgrade*/
    private static final int CQUESTS_UPDATE_DELAY_MS = 10000;

    private boolean isInShutdownProgress;
    private long lastAttackMilis = 0;
    private long lastAttackedMilis = 0;
    private int stance = 0;

    @Override
    public void setOwner(Creature owner) {
        if (getOwner() != null) {
            getOwner().getChainer().detach(EffectResurrectBaseHandler.STATIC);
        }
        super.setOwner(owner);
        if (getOwner() != null) {
            getOwner().getChainer().attach(EffectResurrectBaseHandler.STATIC);
        }
    }

    @Override
    public void see(VisibleObject object) {
        super.see(object);
        if (object instanceof Player) {
            Player player = (Player) object;
            getOwner().sendPck(new SM_PLAYER_INFO(player, getOwner().isAggroIconTo(player)));
            getOwner().sendPck(new SM_MOTION(player.getObjectId(), player.getMotions().getActiveMotions()));
            if (player.isInPlayerMode(PlayerMode.RIDE)) {
                getOwner().sendPck(new SM_EMOTION(player, EmotionType.RIDE, 0, player.ride.getNpcId()));
            }
            if (player.getPet() != null) {
                getOwner().sendPck(new SM_PET(3, player.getPet()));
            }
            player.getEffectController().sendEffectIconsTo(getOwner());
        } else if (object instanceof Kisk) {
            Kisk kisk = ((Kisk) object);
            getOwner().sendPck(new SM_NPC_INFO(kisk, getOwner()));
            if (getOwner().getRace() == kisk.getOwnerRace()) {
                getOwner().sendPck(new SM_KISK_UPDATE(kisk));
            }
        } else if (object instanceof Npc) {
            Npc npc = ((Npc) object);
            getOwner().sendPck(new SM_NPC_INFO(npc, getOwner()));
            if (!npc.getEffectController().isEmpty()) {
                npc.getEffectController().sendEffectIconsTo(getOwner());
            }
        } else if (object instanceof Summon) {
            Summon npc = ((Summon) object);
            getOwner().sendPck(new SM_NPC_INFO(npc));
            if (!npc.getEffectController().isEmpty()) {
                npc.getEffectController().sendEffectIconsTo(getOwner());
            }
        } else if (object instanceof Gatherable || object instanceof StaticObject) {
            getOwner().sendPck(new SM_GATHERABLE_INFO(object));
        } else if (object instanceof Pet) {
            getOwner().sendPck(new SM_PET(3, (Pet) object));
        }
    }

    @Override
    public void notSee(VisibleObject object, boolean isOutOfRange) {
        super.notSee(object, isOutOfRange);
        if (object instanceof Pet) {
            getOwner().sendPck(new SM_PET(4, (Pet) object));
        } else {
            AionServerPacket packet = new SM_DELETE(object, isOutOfRange ? 0 : 15);
            getOwner().sendPck(packet);
        }
    }

    public void updateNearbyQuests() {
        FastMap<Integer, Integer> nearbyQuestList = FastMap.newInstance();
        for (int questId : getOwner().getPosition().getMapRegion().getParent().getQuestIds()) {
            if (QuestService.checkStartConditions(new QuestEnv(null, getOwner(), questId, 0), false)) {
                if (!nearbyQuestList.containsKey(questId)) {
                    boolean minLevelOk = QuestService.checkLevelRequirement(questId, getOwner().getCommonData()
                        .getLevel());
                    nearbyQuestList.put(questId, minLevelOk ? 0 : 2);
                }
            }
        }
        getOwner().sendPck(new SM_NEARBY_QUESTS(nearbyQuestList));
    }

    @Override
    public void onEnterZone(ZoneInstance zone) {
        Player player = getOwner();
        if (CustomConfig.ENABLE_RIDE_RESTRICTION && (!zone.canRide()) && (player.isInPlayerMode(PlayerMode.RIDE))) {
            player.unsetPlayerMode(PlayerMode.RIDE);
        }
        InstanceService.onEnterZone(player, zone);
        if (zone.getAreaTemplate().getZoneName() == null) {
            log.error("No name found for a Zone in the map " + zone.getAreaTemplate().getWorldId());
        } else {
            QuestEngine.getInstance()
                       .onEnterZone(new QuestEnv(null, player, 0, 0), zone.getAreaTemplate().getZoneName());
        }
        player.getController().updateNearbyQuests();
    }

    @Override
    public void onLeaveZone(ZoneInstance zone) {
        Player player = getOwner();
        InstanceService.onLeaveZone(player, zone);
        ZoneName zoneName = zone.getAreaTemplate().getZoneName();
        if (zoneName == null) {
            log.warn("No name for zone template in " + zone.getAreaTemplate().getWorldId());
            return;
        }
        QuestEngine.getInstance().onLeaveZone(new QuestEnv(null, player, 0, 0), zoneName);
        
        if (DuelService.getInstance().isDueling(player.getObjectId())) {
            DuelService.getInstance().loseDuel(player);
        }
    }

    /**
     * {@inheritDoc} Should only be triggered from one place (life stats)
     */
    // TODO [AT] move
    public void onEnterWorld() {

        InstanceService.onEnterInstance(getOwner());
        if (getOwner().getPosition().getWorldMapInstance().getParent().isExceptBuff()) {
            getOwner().getEffectController().removeAllEffects();
        }
        for (Effect ef : getOwner().getEffectController().getAbnormalEffects()) {
            if (ef.isDeityAvatar()) {
                // remove abyss transformation if worldtype != abyss && worldtype != balaurea
                if (getOwner().getWorldType() != WorldType.ABYSS && getOwner().getWorldType() != WorldType.BALAUREA
                    || getOwner().isInInstance()) {
                    ef.endEffect();
                    getOwner().getEffectController().clearEffect(ef);
                }
            } else if (ef.getSkillTemplate().getDispelCategory() == DispelCategoryType.NPC_BUFF) {
                ef.endEffect();
                getOwner().getEffectController().clearEffect(ef);
            }
        }
    }

    // TODO [AT] move
    public void onLeaveWorld() {
        Player player = getOwner();
        if (DuelService.getInstance().isDueling(player.getObjectId())) {
            DuelService.getInstance().loseDuel(player);
        }
        InstanceService.onLeaveInstance(getOwner());   
    }

    public void onDie(@NotNull Creature lastAttacker, boolean showPacket) {
        Player player = this.getOwner();
        getOwner().getController().cancelCurrentSkill();
        getOwner().setRebirthRevive(getOwner().haveSelfRezEffect());
        showPacket = player.hasResurrectBase() ? false : showPacket;
        Creature master = lastAttacker.getMaster();
        if (getOwner().isInPlayerMode(PlayerMode.WINDSTREAM_STARTED) || (getOwner().isInState(CreatureState.ENTERED_WINDS))) {
            //PacketSendUtility.sendYellowMessageOnCenter(player, "ПРОВЕРКА");
            getOwner().getWindstreamControllder().updateAndFixStream();
        }
        // High ranked kill announce
        AbyssRank ar = player.getAbyssRank();
        if (AbyssService.isOnPvpMap(player) && ar != null) {
            if (ar.getRank().getId() >= 10) {
                AbyssService.rankedKillAnnounce(player);
            }
        }

        if (DuelService.getInstance().isDueling(player.getObjectId())) {
            if (master != null && DuelService.getInstance().isDueling(player.getObjectId(), master.getObjectId())) {
            DuelService.getInstance().loseDuel(player);
            player.getEffectController().removeAbnormalEffectsByTargetSlot(SkillTargetSlot.DEBUFF);
            player.getLifeStats().setCurrentHpPercent(33);
            player.getLifeStats().setCurrentMpPercent(33);
            return;
      }
      DuelService.getInstance().loseDuel(player);
    }
      Summon summon = player.getSummon();
            if (summon != null) {
                SummonsService.doMode(SummonMode.RELEASE, summon, UnsummonType.UNSPECIFIED);
            }

            if (player.isInState(CreatureState.FLYING)) {
                player.setIsFlyingBeforeDeath(true);
            }

            player.setPlayerMode(PlayerMode.RIDE, null);
            player.unsetState(CreatureState.RESTING);
            player.unsetState(CreatureState.FLOATING_CORPSE);

            player.unsetState(CreatureState.FLYING);
            player.unsetState(CreatureState.GLIDING);
            player.setFlyState(0);
            if (player.isInInstance()) {
                if (player.getPosition().getWorldMapInstance().getInstanceHandler().onDie(player, lastAttacker)) {
                    PlayerController.super.onDie(lastAttacker);
                    return; // break
                }
            }

            MapRegion mapRegion = player.getPosition().getMapRegion();
            if (mapRegion != null && mapRegion.onDie(lastAttacker, player)) {
                return; // break
            }

            doReward();

            if (master instanceof Npc || master == player) {
                if (player.getLevel() > 4 && !isNoDeathPenaltyInEffect()) {
                    player.getCommonData().calculateExpLoss();
                }
            }

            // Effects removed with super.onDie()
            PlayerController.super.onDie(lastAttacker);

            // send sm_emotion with DIE
            // have to be send after state is updated!
            sendDieFromCreature(lastAttacker, showPacket);

            QuestEngine.getInstance().onDie(new QuestEnv(null, player, 0, 0));

            if (player.isInGroup2()) {
                player.getPlayerGroup2()
                      .sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_FRIENDLY_DEATH(player.getName()), new ExcludePlayerFilter(player));
            }             
    }

    @Override
    public void onDie(@NotNull Creature lastAttacker) {
        this.onDie(lastAttacker, true);
    }

    public void sendDie() {
        sendDieFromCreature(getOwner(), true);
    }

    private void sendDieFromCreature(@NotNull Creature lastAttacker, boolean showPacket) {
        Player player = getOwner();
        PacketSendUtility.broadcastPacket(player,
	    new SM_EMOTION(player, EmotionType.DIE, 0, player.equals(lastAttacker) ? 0 : lastAttacker.getObjectId()), true);
        if (showPacket) {
            int kiskTimeRemaining = (player.getKisk() != null ? player.getKisk().getRemainingLifetime() : 0);
            player.sendPck(new SM_DIE(player.canUseRebirthRevive(), player.haveSelfRezItem(), kiskTimeRemaining, 0));
        }
        player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_MY_DEATH);
    }

    @Override
    public void doReward() {
        PvpService.doReward(getOwner());
    }

    @Override
    public void onBeforeSpawn() {
        super.onBeforeSpawn();
        startProtectionActiveTask();
        if (getOwner().getIsFlyingBeforeDeath()) {
            getOwner().unsetState(CreatureState.FLOATING_CORPSE);
        } else {
            getOwner().unsetState(CreatureState.DEAD);
        }
        getOwner().setState(CreatureState.ACTIVE);
    }

    @Override
    public void attackTarget(Creature target, int time) {

        PlayerGameStats gameStats = getOwner().getGameStats();

        if (!RestrictionsManager.canAttack(getOwner(), target)) {
            return;
        }

        // Normal attack is already limited client side (ex. Press C and attacker approaches target)
        // but need a check server side too also for Z axis issue

        if (!MathUtil.isInAttackRange(getOwner(), target, getOwner().getGameStats()
            .getAttackRange()
            .getCurrent() / 1000f + 1)) {
            return;
        }

        /*if (!GeoService.getInstance().canSee(getOwner(), target)) {
            getOwner().sendPck(SM_SYSTEM_MESSAGE.STR_ATTACK_OBSTACLE_EXIST);
            return;
        }*/


        //MW FIXME Temporary solution to pass abyss gate attacks
        if(!(target instanceof SiegeNpc && ((SiegeNpc)target).getObjectTemplate().getAbyssNpcType() == AbyssNpcType.DOOR)) {
            if (!GeoHelper.canSee(getOwner(), target)) {
                getOwner().sendPck(SM_SYSTEM_MESSAGE.STR_ATTACK_OBSTACLE_EXIST);
                return;
            }
        }


        int attackSpeed = gameStats.getAttackSpeed().getCurrent();

        long milis = System.currentTimeMillis();
        // network ping..
        if (milis - lastAttackMilis + 300 < attackSpeed) {
            // hack
            return;
        }
        lastAttackMilis = milis;

        /**
         * notify attack observers
         */
        super.attackTarget(target, time);

    }

    @Override
    public void onAttack(Creature creature, int skillId, TYPE type, int damage, boolean notifyAttack, LOG log, AttackStatus attackStatus) {
        if (getOwner().getLifeStats().isAlreadyDead()) {
            return;
        }

        if (getOwner().isInvul() || getOwner().isProtectionActive()) {
            damage = 0;
        }

        cancelGathering();         
        cancelUseItem();
        cancelCurrentSkill();
        super.onAttack(creature, skillId, type, damage, notifyAttack, log, attackStatus);
        lastAttackedMilis = System.currentTimeMillis();

        PacketSendUtility.broadcastPacket(getOwner(), new SM_ATTACK_STATUS(getOwner(), type, skillId, damage, log), true);
    }

    /**
     * @param skillId
     * @param targetType
     * @param x
     * @param y
     * @param z
     */
    public void useSkill(int skillId, int targetType, float x, float y, float z, int time) {
        Player player = getOwner();

        Skill skill = SkillEngine.getInstance().getSkillFor(player, skillId, player.getTarget());

        if (skill != null) {
            if (!RestrictionsManager.canUseSkill(player, skill)) {
                return;
            }

            skill.setTargetType(targetType, x, y, z);
            skill.setHitTime(time);
            skill.useSkill();
        }
    }

    public void useSkill(SkillTemplate template, int targetType,
                         float x, float y, float z,
                         int clientHitTime, int skillLevel) {
        Player player = getOwner();

        Skill skill = SkillEngine.getInstance().getSkillFor(player, template, player.getTarget());
        if ((skill == null) && (player.isTransformed())) {
            SkillPanel panel = DataManager.PANEL_SKILL_DATA.getSkillPanel(player.getTransformModel().getPanelId());
            if ((panel != null) && (panel.canUseSkill(template.getSkillId(), skillLevel))) {
                skill = SkillEngine.getInstance().getSkillFor(player, template, player.getTarget(), skillLevel);
            }
        }

        if (skill != null) {
            if (!RestrictionsManager.canUseSkill(player, skill)) {
                return;
            }

            skill.setTargetType(targetType, x, y, z);
            skill.setHitTime(clientHitTime);
            skill.useSkill();
            QuestEnv env = new QuestEnv(player.getTarget(), player, 0, 0);
            QuestEngine.getInstance().onUseSkill(env, template.getSkillId());
        }
    }

    @Override
    public void onMove() {
        getOwner().getObserveController().notifyMoveObservers();
        super.onMove();
    }

    @Override
    public void onStopMove() {
        PlayerMoveTaskManager.removePlayer(getOwner());
        getOwner().getObserveController().notifyMoveObservers();
        getOwner().getMoveController().setInMove(false);
        cancelCurrentSkill();
        updateZone();
        super.onStopMove();
    }

    @Override
    public void onStartMove() {
        getOwner().getMoveController().setInMove(true);
        PlayerMoveTaskManager.addPlayer(getOwner());
        cancelUseItem();
        cancelCurrentSkill();
        super.onStartMove();
    }

    @Override
    public void cancelCurrentSkill() {
        Player player = getOwner();
        Skill castingSkill = player.getCastingSkill();

        if(castingSkill == null)
            return;

        castingSkill.cancelCast();
        player.removeSkillCoolDown(castingSkill.getSkillTemplate().getCooldownId());
        player.setCasting(null);
        player.setNextSkillUse(0);
        if (castingSkill.getSkillMethod() == SkillMethod.CAST) {
            PacketSendUtility.broadcastPacket(player, new SM_SKILL_CANCEL(player, castingSkill.getSkillTemplate()
                    .getSkillId()), true);
            player.sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_CANCELED);
        } else if (castingSkill.getSkillMethod() == SkillMethod.ITEM) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED(DescId.of(castingSkill.getItemTemplate().getNameId())));
            player.removeItemCoolDown(castingSkill.getItemTemplate().getUseLimits().getDelayId());
            PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), castingSkill.getFirstTarget()
                    .getObjectId(),
                    castingSkill.getItemObjectId(), castingSkill.getItemTemplate().getTemplateId(), 0, 3, 0), true);
        }

        return;
    }

    @Override
    public void cancelUseItem() {
        Player player = getOwner();
        Item usingItem = player.getUsingItem();
        player.setUsingItem(null);
        if (hasTask(TaskId.ITEM_USE)) {
            cancelTask(TaskId.ITEM_USE);
            PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), usingItem == null ? 0 : usingItem
                .getObjectId(),
                usingItem == null ? 0 : usingItem.getItemTemplate().getTemplateId(), 0, 3, 0), true);
        }
    }

    public void cancelGathering() {
        Player player = getOwner();
        if (player.getTarget() instanceof Gatherable) {
            Gatherable g = (Gatherable) player.getTarget();
            g.getController().finishGathering(player);
        }
    }

    public void updatePassiveStats() {
        Player player = getOwner();
        for (PlayerSkillEntry skillEntry : player.getSkillList().getAllSkills()) {
            Skill skill = SkillEngine.getInstance().getSkillFor(player, skillEntry.getSkillId(), null);
            if (skill != null && skill.isPassive()) {
                skill.useSkill();
            }
        }
    }

    @Override
    public Player getOwner() {
        return (Player) super.getOwner();
    }

    public boolean isDueling(Player player) {
        return DuelService.getInstance().isDueling(player.getObjectId(), getOwner().getObjectId());
    }
    
    @Override
    public void onRestore(HealType healType, int value) {
        super.onRestore(healType, value);
        switch (healType) {
            case DP:
                getOwner().getLifeStats().increaseDp(value);
                break;
        }
    }

    // TODO [AT] rename or remove
    public boolean isInShutdownProgress() {
        return isInShutdownProgress;
    }

    // TODO [AT] rename or remove
    public void setInShutdownProgress(boolean isInShutdownProgress) {
        this.isInShutdownProgress = isInShutdownProgress;
    }

    @Override
    public void onAfterSpawn() {
        EventNotifier.GLOBAL.fire(PlayerSpawn.class, getOwner());

        super.onAfterSpawn();
    }
    @Override
    public void onDialogSelect(int dialogId, Player player, int questId, int extendedRewardIndex) {
        switch (dialogId) {
            case 2:
                player.sendPck(new SM_PRIVATE_STORE(getOwner().getStore(), player));
                break;
        }
    }

    public void upgradePlayer() {
        Player player = getOwner();
        byte level = player.getLevel();

        PlayerStatsTemplate statsTemplate = DataManager.PLAYER_STATS_DATA.getTemplate(player);
        player.setPlayerStatsTemplate(statsTemplate);

        player.getLifeStats().synchronizeWithMaxStats();
        player.getLifeStats().updateCurrentStats();

        PacketSendUtility.broadcastPacket(player, new SM_LEVEL_UPDATE(player.getObjectId(), 0, level), true);

        // Guides Html on level up
        if (HTMLConfig.ENABLE_GUIDES) {
            HTMLService.sendGuideHtml(player);
        }

        // Temporal
        ClassChangeService.showClassChangeDialog(player);

        QuestEngine.getInstance().onLvlUp(new QuestEnv(null, player, 0, 0));
        updateNearbyQuests();

        // add new skills
        SkillLearnService.addNewSkills(player);
        // TODO M4 improve here performance
        updatePassiveStats();

        // add recipe for morph
        if (level == 10) {
            CraftSkillUpdateService.getInstance().setMorphRecipe(player);
        }

        if (player.isInTeam()) {
            TeamEffectUpdater.getInstance().startTask(player);
        }
        if (player.isLegionMember()) {
            LegionService.getInstance().updateMemberInfo(player);
        }
        player.getNpcFactions().onLevelUp();

        ThreadPoolManager.getInstance().schedule(()->{
            CustomQuestsService.getInstance().giveNewQuests(player);
        }, CQUESTS_UPDATE_DELAY_MS);

    }

    /**
     * After entering game player char is "blinking" which means that it's in under some protection, after making an action char stops blinking. - Starts
     * protection active - Schedules task to end protection
     */
    public void startProtectionActiveTask() {
        if (!getOwner().isProtectionActive()) {
            getOwner().setVisualState(CreatureVisualState.BLINKING);
            PacketSendUtility.broadcastPacket(getOwner(), new SM_PLAYER_STATE(getOwner()), true);
            Future<?> task = ThreadPoolManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    stopProtectionActiveTask();
                }

            },  60000);
            addTask(TaskId.PROTECTION_ACTIVE, task);
        }
    }

    /**
     * Stops protection active task after first move or use skill
     */
    public void stopProtectionActiveTask() {
        cancelTask(TaskId.PROTECTION_ACTIVE);
        Player player = getOwner();
        if (player != null && player.isSpawned()) {
            player.unsetVisualState(CreatureVisualState.BLINKING);
            PacketSendUtility.broadcastPacket(player, new SM_PLAYER_STATE(player), true);
            notifyAIOnMove();
        }
    }

    /**
     * When player arrives at destination point of flying teleport
     */
    public void onFlyTeleportEnd() {
        Player player = getOwner();
        if (player.isInPlayerMode(PlayerMode.WINDSTREAM_STARTED)) {
            player.unsetPlayerMode(PlayerMode.WINDSTREAM_STARTED);
            player.getLifeStats().triggerFpReduce();
            player.unsetState(CreatureState.FLYING);
            player.setState(CreatureState.ACTIVE);
            player.setState(CreatureState.GLIDING);
            player.getGameStats().updateStatsAndSpeedVisually();
        } else {
            player.unsetState(CreatureState.FLIGHT_TELEPORT);
            player.setFlightTeleportId(0);

            if (SecurityConfig.ENABLE_FLYPATH_VALIDATOR) {
                long diff = (System.currentTimeMillis() - player.getFlyStartTime());
                FlyPathEntry path = player.getCurrentFlyPath();

                if (player.getWorldId() != path.getEndWorldId()) {
                    AuditLogger.info(player, "Player tried to use flyPath #" + path.getId() + " from not native start world " + player
                        .getWorldId()
                        + ". expected " + path.getEndWorldId());
                }

                if (diff < path.getTimeInMs()) {
                    AuditLogger.info(player, "Player " + player.getName() + " used flypath bug " + diff + " instead of " + path
                        .getTimeInMs());
                }
                player.setCurrentFlypath(null);
            }

            player.setFlightDistance(0);
            player.setState(CreatureState.ACTIVE);
            updateZone();
        }
    }

    public boolean addItems(int itemId, int count) {
        return ItemService.addQuestItems(getOwner(), Collections.singletonList(new QuestItems(itemId, count)));
    }

    public void startStance(int skillId) {
        stance = skillId;
    }

    public void stopStance() {
        getOwner().getEffectController().removeEffect(stance);
        getOwner().sendPck(new SM_PLAYER_STANCE(getOwner(), 0));
        stance = 0;
    }

    public int getStanceSkillId() {
        return stance;
    }

    public boolean isUnderStance() {
        return stance != 0;
    }

    public void updateSoulSickness(int skillId) {
        Player player = getOwner();

        if (HouseInfo.of(player).typeIs(MANSION, ESTATE, PALACE)) {
            return;
        }

        if (!player.havePermission(MembershipConfig.DISABLE_SOULSICKNESS)) {
            int deathCount = player.getCommonData().getDeathCount();
            if (deathCount < 10) {
                deathCount++;
                player.getCommonData().setDeathCount(deathCount);
            }
            if (skillId == 0) {
                skillId = 8291;
            }
            SkillEngine.getInstance().getSkill(player, skillId, deathCount, player).useSkill();
        }
    }

    /**
     * Player is considered in combat if he's been attacked or has attacked less or equal 10s before
     *
     * @return true if the player is actively in combat
     */
    public boolean isInCombat() {
        return (((System.currentTimeMillis() - lastAttackedMilis) <= 10000) || ((System.currentTimeMillis() - lastAttackMilis) <= 10000));
    }

    public boolean isNoDeathPenaltyInEffect() {
        Iterator<Effect> iterator = getOwner().getEffectController().iterator();
        while (iterator.hasNext()) {
            Effect effect = iterator.next();
            if (effect.isNoDeathPenalty()) {
                return true;
            }
        }
        return false;
    }

    public boolean isNoResurrectPenaltyInEffect() {
        Iterator<Effect> iterator = getOwner().getEffectController().iterator();
        while (iterator.hasNext()) {
            Effect effect = iterator.next();
            if (effect.isNoResurrectPenalty()) {
                return true;
            }
        }
        return false;
    }

    public boolean isHiPassInEffect() {
        Iterator<Effect> iterator = getOwner().getEffectController().iterator();
        while (iterator.hasNext()) {
            Effect effect = iterator.next();
            if (effect.isHiPass()) {
                return true;
            }
        }
        return false;
    }

    
    
}
