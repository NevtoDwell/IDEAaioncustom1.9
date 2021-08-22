/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.restrictions;

import com.ne.commons.func.tuple.Tuple;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.gs.configs.main.GroupConfig;
import com.ne.gs.model.DescId;
import com.ne.gs.model.actions.PlayerMode;
import com.ne.gs.model.conds.CanInviteToAlliance;
import com.ne.gs.model.conds.CanInviteToGroup;
import com.ne.gs.model.conds.SkillUseCond;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.model.templates.zone.ZoneClassName;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.skillengine.effect.AbnormalState;
import com.ne.gs.skillengine.effect.EffectType;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.skillengine.model.TransformType;
import com.ne.gs.skillengine.properties.TargetRelationAttribute;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.world.InstanceType;
import com.ne.gs.world.zone.ZoneInstance;
import com.ne.gs.world.zone.ZoneName;

/**
 * @author lord_rex modified by Sippolo
 */
public class PlayerRestrictions extends AbstractRestrictions {

    @Override
    public boolean canAffectBySkill(Player player, VisibleObject target, Skill skill) {
        if (skill == null) {
            return false;
        }

        // dont allow to use skills in Fly Teleport state
        if (target instanceof Player && ((Player) target).isProtectionActive()) {
            return false;
        }

        if (player != target && target instanceof Player) {
        Player tPlayer = (Player) target;
            if (!tPlayer.getRace().equals(player.getRace())) {
            if (!InstanceType.isInTVT(tPlayer) && !tPlayer.isEnemyFrom(player) && !InstanceType.isInPeace(tPlayer)) {
                 return false;
            }
        } else if (tPlayer.getController().isDueling(player)) {
            if (skill.getSkillTemplate().getProperties().getTargetRelation() != TargetRelationAttribute.ENEMY) {
            PacketSendUtility.sendPck(player, SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID);
            return false;
	}
      }
    }
        
        if (player.isUsingFlyTeleport() || (target instanceof Player && ((Player) target).isUsingFlyTeleport())) {
            return false;
        }

        if (((Creature) target).getLifeStats().isAlreadyDead() && !skill.getSkillTemplate()
                                                                        .hasResurrectEffect() && !skill.checkNonTargetAOE()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID);
            return false;
        }

        // cant ressurect non players and non dead
        if (skill.getSkillTemplate().hasResurrectEffect()
            && (!(target instanceof Player) || (!((Creature) target).getLifeStats()
                                                                   .isAlreadyDead() && !((Creature) target).isInState(CreatureState.DEAD)))) {
            return false;
        }

        if (skill.getSkillTemplate().hasItemHealFpEffect() && !player.isInFlyingState()) { // player must be
            player.sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_RESTRICTION_FLY_ONLY);
            return false;
        }

        if (!skill.getSkillTemplate().hasEvadeEffect()) {
            if (player.getEffectController().isAbnormalState(AbnormalState.CANT_ATTACK_STATE)) {
                return false;
            }
        }

        // Fix for Summon Group Member, cannot be used while either caster or summoned is actively in combat
        if (skill.getSkillTemplate().hasRecallInstant()) {
            // skill properties should already filter only players
            if (player.getController().isInCombat() || ((Player) target).getController().isInCombat()) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_Recall_CANNOT_ACCEPT_EFFECT(target.getName()));
                return false;
            }
        }

        if (player.isInState(CreatureState.PRIVATE_SHOP)) { // You cannot use an item while running a Private Store.
            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_CANNOT_USE_ITEM_DURING_PATH_FLYING(DescId.of(2800123)));
            return false;
        }
        return true;
    }

    public static boolean checkFly(Player player, VisibleObject target) {
        if ((player.isUsingFlyTeleport()) || (player.isInPlayerMode(PlayerMode.WINDSTREAM_STARTED))) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_RESTRICTION_NO_FLY);
            return false;
        }

        if ((target != null) && ((target instanceof Player))) {
            Player playerTarget = (Player) target;
            if ((playerTarget.isUsingFlyTeleport()) || (playerTarget.isInPlayerMode(PlayerMode.WINDSTREAM_STARTED))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canUseSkill(Player player, Skill skill) {
        return player.getConditioner().check(SkillUseCond.class, Tuple2.of(player, skill));
    }

    @Override
    public boolean canInviteToGroup(Player player, Player target) {
        return player.getConditioner().check(CanInviteToGroup.class, Tuple.of(player, target));
    }

    @Override
    public boolean canInviteToAlliance(Player player, Player target) {
        return player.getConditioner().check(CanInviteToAlliance.class, Tuple.of(player, target));
    }

    @Override
    public boolean canAttack(Player player, VisibleObject target) {
        if (target == null) {
            return false;
        }

        if (!checkFly(player, target)) {
            return false;
        }

        if (!(target instanceof Creature)) {
            return false;
        }

        Creature creature = (Creature) target;

        if (creature.getLifeStats().isAlreadyDead()) {
            return false;
        }

        return player.isEnemy(creature);
    }

    @Override
    public boolean canUseWarehouse(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        // TODO retail message to requestor and player
        if (player.isTrading()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean canTrade(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        if (player.isTrading()) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_EXCHANGE_PARTNER_IS_EXCHANGING_WITH_OTHER);
            return false;
        }

        return true;
    }

    @Override
    public boolean canChat(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        return !player.isGagged();
    }

    @Override
    public boolean canUseItem(Player player, Item item) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        if (player.getLifeStats().isAlreadyDead()) {
            return false;
        }

        if (player.getEffectController().isAbnormalState(AbnormalState.CANT_ATTACK_STATE)) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_USE_ITEM_WHILE_IN_ABNORMAL_STATE);
            return false;
        }

        if (item.getItemTemplate().hasAreaRestriction()) {
            ZoneName restriction = item.getItemTemplate().getUseArea();
            if (restriction.equals(ZoneName.get("_ABYSS_CASTLE_AREA_"))) {
                boolean isInFortZone = false;
                for (ZoneInstance zone : player.getPosition().getMapRegion().getZones(player)) {
                    if (zone.getZoneTemplate().getZoneType().equals(ZoneClassName.FORT)) {
                        isInFortZone = true;
                        break;
                    }
                }
                if (!isInFortZone) {
                    AionServerPacket packet = new SM_SYSTEM_MESSAGE(1300143);
                    player.sendPck(packet);
                    return false;
                }
            } else if(restriction.equals(ZoneName.get("IDARENA_PVP_ITEMUSEAREA_ALL_LOSE"))
                    || restriction.equals(ZoneName.get("IDARENA_PVP_ITEMUSEAREA_ALL"))
                    ||  restriction.equals(ZoneName.get("IDARENA_PVP_ITEMUSEAREA_ALL_T"))){
                //Исключение для итемов арены.
                boolean isInPvpArena = false;
                if(player.getWorldId()==300350000 || player.getWorldId()== 300360000){
                    isInPvpArena = true;
                } if (!isInPvpArena){
                    player.sendPck(new SM_SYSTEM_MESSAGE(1300143));
                    return false;
                }

            }
            else if (!player.isInsideZone(restriction)) {
                // You cannot use that item here.
                player.sendPck(new SM_SYSTEM_MESSAGE(1300143));
                return false;
            }
        }
        return true;
    }
}
