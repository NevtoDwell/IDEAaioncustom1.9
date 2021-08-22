/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.ne.gs.services.custom.CustomQuestsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.ai2.poll.AIQuestion;
import com.ne.gs.configs.main.GroupConfig;
import com.ne.gs.controllers.attack.AggroInfo;
import com.ne.gs.controllers.attack.AttackStatus;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.AionObject;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.RewardType;
import com.ne.gs.model.gameobjects.siege.SiegeNpc;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.team2.TemporaryPlayerTeam;
import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.common.service.PlayerTeamDistributionService;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.services.DialogService;
import com.ne.gs.services.RespawnService;
import com.ne.gs.services.SiegeService;
import com.ne.gs.services.SiegeService.SiegeBoss;
import com.ne.gs.services.abyss.AbyssPointsService;
import com.ne.gs.services.drop.DropRegistrationService;
import com.ne.gs.services.drop.DropService;
import com.ne.gs.skillengine.model.SkillTemplate;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.stats.StatFunctions;
import com.ne.gs.world.zone.ZoneInstance;

/**
 * This class is for controlling Npc's
 *
 * @author -Nemesiss-, ATracer (2009-09-29), Sarynth modified by Wakizashi
 */

public class NpcController extends CreatureController<Npc> {

    private static final Logger log = LoggerFactory.getLogger(NpcController.class);

    @Override
    public void notSee(VisibleObject object, boolean isOutOfRange) {
        super.notSee(object, isOutOfRange);
        if (object instanceof Creature) {
            getOwner().getAi2().onCreatureEvent(AIEventType.CREATURE_NOT_SEE, (Creature) object);
            getOwner().getAggroList().remove((Creature) object);
        }
        // TODO not see player ai event
    }

    @Override
    public void see(VisibleObject object) {
        super.see(object);
        Npc owner = getOwner();
        if (object instanceof Creature) {
            owner.getAi2().onCreatureEvent(AIEventType.CREATURE_SEE, (Creature) object);
        }
        if (object instanceof Player) {
            // TODO see player ai event
            if (owner.getLifeStats().isAlreadyDead()) {
                DropService.getInstance().see((Player) object, owner);
            }
        } else if (object instanceof Summon) {
            // TODO see summon ai event
        }
    }

    @Override
    public void onBeforeSpawn() {
        super.onBeforeSpawn();
        Npc owner = getOwner();

        // set state from npc templates
        if (owner.getObjectTemplate().getState() != 0) {
            owner.setState(owner.getObjectTemplate().getState());
        } else {
            owner.setState(CreatureState.NPC_IDLE);
        }

        owner.getLifeStats().setCurrentHpPercent(100);
        owner.getAi2().onGeneralEvent(AIEventType.RESPAWNED);

        if (owner.getSpawn().canFly()) {
            owner.setState(CreatureState.FLYING);
        }
        if (owner.getSpawn().getState() != 0) {
            owner.setState(owner.getSpawn().getState());
        }
    }

    @Override
    public void onAfterSpawn() {
        super.onAfterSpawn();
        getOwner().getAi2().onGeneralEvent(AIEventType.SPAWNED);
    }

    @Override
    public void onDespawn() {
        Npc owner = getOwner();
        DropService.getInstance().unregisterDrop(getOwner());
        owner.getAi2().onGeneralEvent(AIEventType.DESPAWNED);
        if (owner.getNpcId() == SiegeBoss.GOVERNOR_SUNAYAKA_218553) {
            SiegeService.getInstance().despawnTiamarantaBoss();
        }
        super.onDespawn();
    }

    @Override
    public void onDie(Creature lastAttacker) {

        Npc owner = getOwner();
        if (owner.getSpawn().hasPool()) {
            owner.getSpawn().setUse(false);
        }
        PacketSendUtility.broadcastPacket(owner,
            new SM_EMOTION(owner, EmotionType.DIE, 0, owner.equals(lastAttacker) ? 0 : lastAttacker.getObjectId()));
        try {
            if (owner.getAi2().poll(AIQuestion.SHOULD_REWARD)) {
                doReward();
            }

            owner.getPosition().getWorldMapInstance().getInstanceHandler().onDie(owner);
            owner.getAi2().onGeneralEvent(AIEventType.DIED);
        } finally {
            if (owner.getAi2().poll(AIQuestion.SHOULD_DECAY)) {
                addTask(TaskId.DECAY, RespawnService.scheduleDecayTask(owner));
            }
            if (owner.getAi2().poll(AIQuestion.SHOULD_RESPAWN)
                && !SiegeService.getInstance().isSiegeNpcInActiveSiege(owner)) {
                Future<?> task = scheduleRespawn();
                if (task != null) {
                    addTask(TaskId.RESPAWN, task);
                }
            } else if (!hasScheduledTask(TaskId.DECAY)) {
                onDelete();
            }
        }
        super.onDie(lastAttacker);
    }

    @Override
    public void doReward() {
        if (getOwner() instanceof SiegeNpc) {
            rewardSiegeNpc();
        }

        AionObject winner = getOwner().getAggroList().getMostDamage();

        if (winner == null) {
            return;
        }

        if (getOwner().getAggroList().hasNpcInDamageList()) {
            if (!(winner instanceof TemporaryPlayerTeam) && !((Player) winner).isInGroup2()) {
                Player active = (Player) ((Player) winner).getActingCreature();
                QuestEngine.getInstance().onKill(new QuestEnv(getOwner(), active, 0, 0));
            }
            return;
        }

        if (winner instanceof TemporaryPlayerTeam) {
            PlayerTeamDistributionService.doReward((TemporaryPlayerTeam) winner, getOwner());
        } else if (((Player) winner).isInGroup2()) {
            PlayerTeamDistributionService.doReward(((Player) winner).getPlayerGroup2(), getOwner());
        } else {
            super.doReward();

            Player player = (Player) ((Creature) winner).getActingCreature();

            long expReward = StatFunctions.calculateSoloExperienceReward(player, getOwner());
            player.getCommonData().addExp(expReward, RewardType.HUNTING, getOwner().getObjectTemplate().getNameId());


            int dpReward = StatFunctions.calculateSoloDPReward(player, getOwner());
            player.getLifeStats().increaseDp(dpReward);

            if (getOwner().isRewardAP()) {
                if (!player.isInState(CreatureState.DUELING)) {
                    AbyssPointsService.addAp(player, getOwner(), StatFunctions.calculatePvEApGained(player, getOwner()), NpcController.class);
                }
            }

            QuestEngine.getInstance().onKill(new QuestEnv(getOwner(), player, 0, 0));
            CustomQuestsService.getInstance().onNpcKill(player, getOwner());
            DropRegistrationService.getInstance().registerDrop(getOwner(), player, player.getLevel(), null);
            // notify instance script
        }
    }

    @Override
    public Npc getOwner() {
        return (Npc) super.getOwner();
    }

    @Override
    public void onDialogRequest(Player player) {
        // notify npc dialog request observer
        if (!getOwner().getObjectTemplate().canInteract()) {
            return;
        }
        player.getObserveController().notifyRequestDialogObservers(getOwner());

        getOwner().getAi2().onCreatureEvent(AIEventType.DIALOG_START, player);
    }

    @Override
    public void onDialogSelect(int dialogId, Player player, int questId, int extendedRewardIndex) {
        if (!MathUtil.isInRange(getOwner(), player, getOwner().getObjectTemplate().getTalkDistance() + 2)) {
            return;
        }
        if (!getOwner().getAi2().onDialogSelect(player, dialogId, questId, extendedRewardIndex)) {
            DialogService.onDialogSelect(dialogId, player, getOwner(), questId, extendedRewardIndex);
        }
    }

    @Override
    public void onAttack(Creature creature, int skillId, TYPE type, int damage, boolean notifyAttack, LOG log, AttackStatus attackStatus) {
        if (getOwner().getLifeStats().isAlreadyDead()) {
            return;
        }
        Creature actingCreature;

        // summon should gain its own aggro
        if (creature instanceof Summon) {
            actingCreature = creature;
        } else {
            actingCreature = creature.getActingCreature();
        }

        super.onAttack(actingCreature, skillId, type, damage, notifyAttack, log, attackStatus);

        Npc npc = getOwner();

        if (actingCreature instanceof Player) {
            QuestEngine.getInstance().onAttack(new QuestEnv(npc, (Player) actingCreature, 0, 0));
        }

        PacketSendUtility.broadcastPacket(npc, new SM_ATTACK_STATUS(npc, type, skillId, damage, log));
    }

    @Override
    public void onStopMove() {
        getOwner().getMoveController().setInMove(false);
        super.onStopMove();
    }

    @Override
    public void onStartMove() {
        getOwner().getMoveController().setInMove(true);
        super.onStartMove();
    }

    @Override
    public void onEnterZone(ZoneInstance zoneInstance) {
        if (zoneInstance.getAreaTemplate().getZoneName() == null) {
            log.error("No name found for a Zone in the map " + zoneInstance.getAreaTemplate().getWorldId());
        }
    }

    private void rewardSiegeNpc() {
        float totalDamage = getOwner().getAggroList().getTotalDamage(); // has to be float
        for (AggroInfo aggro : getOwner().getAggroList().getFinalDamageList(true)) {
            float percentage = aggro.getDamage() / totalDamage;
            if (aggro.getAttacker() instanceof Player) {
                Player player = (Player) aggro.getAttacker();
                if (check(player)) {
                    reward(player, percentage, 1);
                }
            } else if (aggro.getAttacker() instanceof PlayerGroup) {
                rewardMembers(((PlayerGroup) aggro.getAttacker()).getMembers(), percentage);
            } else if ((aggro.getAttacker() instanceof PlayerAlliance)) {
                rewardMembers(((PlayerAlliance) aggro.getAttacker()).getMembers(), percentage);
            }
        }
    }

    private boolean check(Player p) {
        return MathUtil.isIn3dRange(p, getOwner(), GroupConfig.GROUP_MAX_DISTANCE) && !p.getLifeStats().isAlreadyDead();
    }

    private void reward(Player p, float percentage, int size) {
        int baseApReward = StatFunctions.calculatePvEApGained(p, getOwner());
        int apRewardPerMember = Math.round(baseApReward * percentage / size);
        if (apRewardPerMember > 0) {
            if (p.isInState(CreatureState.DUELING))
                return;

            AbyssPointsService.addAp(p, getOwner(), apRewardPerMember, NpcController.class);
        }
    }

    private void rewardMembers(Collection<Player> members, float percentage) {
        List<Player> pls = ImmutableList.copyOf(Iterables.filter(members, new Predicate<Player>() {
            @Override
            public boolean apply(Player p) {
                return check(p);
            }
        }));

        for (Player member : pls) {
            reward(member, percentage, pls.size());
        }
    }

    public Future<?> scheduleRespawn() {
        if (!getOwner().getSpawn().isNoRespawn()) {
            return RespawnService.scheduleRespawnTask(getOwner());
        }
        return null;
    }

    public final float getAttackDistanceToTarget() {
        return getOwner().getGameStats().getAttackRange().getCurrent() / 1000f;
    }

    @Override
    public boolean useSkill(int skillId, int skillLevel) {
        SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(skillId);
        if (skillTemplate != null) {
            if (!getOwner().isSkillDisabled(skillTemplate)) {
                getOwner().getGameStats().renewLastSkillTime();
                return super.useSkill(skillId, skillLevel);
            }
        } else {
            log.warn("Invalid skillId provided", new Exception(Integer.toString(skillId)));
        }
        return false;
    }

}
