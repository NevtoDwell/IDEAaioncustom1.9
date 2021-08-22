/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.summons;

import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.controllers.SummonController;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.summons.SummonMode;
import com.ne.gs.model.summons.UnsummonType;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.network.aion.serverpackets.SM_SUMMON_OWNER_REMOVE;
import com.ne.gs.network.aion.serverpackets.SM_SUMMON_PANEL;
import com.ne.gs.network.aion.serverpackets.SM_SUMMON_PANEL_REMOVE;
import com.ne.gs.network.aion.serverpackets.SM_SUMMON_UPDATE;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.skillengine.SkillEngine;
import com.ne.gs.skillengine.effect.EffectType;
import com.ne.gs.spawnengine.VisibleObjectSpawner;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;

public final class SummonsService {

    public static void createSummon(Player master, int npcId, int skillId, int skillLevel, int time) {
        if (master.getSummon() != null) {
            AionServerPacket packet = new SM_SYSTEM_MESSAGE(1300072);
            master.sendPck(packet);
            return;
        }
        Summon summon = VisibleObjectSpawner.spawnSummon(master, npcId, skillId, skillLevel, time);
        if (summon.getAi2().getName().equals("siege_weapon")) {
            summon.getAi2().onGeneralEvent(AIEventType.SPAWNED);
        }
        master.setSummon(summon);
        master.sendPck(new SM_SUMMON_PANEL(summon));
        PacketSendUtility.broadcastPacket(summon, new SM_EMOTION(summon, EmotionType.START_EMOTE2));
        PacketSendUtility.broadcastPacket(summon, new SM_SUMMON_UPDATE(summon));
    }

    public static void release(Summon summon, UnsummonType unsummonType, boolean isAttacked) {
        if (summon.getMode() == SummonMode.RELEASE) {
            return;
        }
        summon.getController().cancelCurrentSkill();
        summon.setMode(SummonMode.RELEASE);
        Player master = summon.getMaster();
        switch (unsummonType) {
            case COMMAND:
                PacketSendUtility.sendPck(master, SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_UNSUMMON_FOLLOWER(summon.getNameId()));
                PacketSendUtility.sendPck(master, new SM_SUMMON_UPDATE(summon));
                break;
            case DISTANCE:
                PacketSendUtility.sendPck(master, SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_UNSUMMON_BY_TOO_DISTANCE);
                PacketSendUtility.sendPck(master, new SM_SUMMON_UPDATE(summon));
                break;
            case UNSPECIFIED:
            case LOGOUT:
                break;
        }

        summon.getObserveController().notifySummonReleaseObservers();
        summon.setReleaseTask(ThreadPoolManager.getInstance().schedule(new ReleaseSummonTask(summon, unsummonType, isAttacked), 3100L));
    }

    public static void restMode(Summon summon) {
        summon.getController().cancelCurrentSkill();
        summon.setMode(SummonMode.REST);
        Player master = summon.getMaster();
        master.sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_REST_MODE(summon.getNameId()));
        master.sendPck(new SM_SUMMON_UPDATE(summon));
        summon.getLifeStats().triggerRestoreTask();
    }

    public static void setUnkMode(Summon summon) {
        summon.setMode(SummonMode.UNK);
        Player master = summon.getMaster();
        master.sendPck(new SM_SUMMON_UPDATE(summon));
    }

    public static void guardMode(Summon summon) {
        summon.getController().cancelCurrentSkill();
        summon.setMode(SummonMode.GUARD);
        Player master = summon.getMaster();
        master.sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_GUARD_MODE(summon.getNameId()));
        master.sendPck(new SM_SUMMON_UPDATE(summon));
        summon.getLifeStats().triggerRestoreTask();
    }

    public static void attackMode(Summon summon) {
        summon.setMode(SummonMode.ATTACK);
        Player master = summon.getMaster();
        master.sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_ATTACK_MODE(summon.getNameId()));
        master.sendPck(new SM_SUMMON_UPDATE(summon));
        summon.getLifeStats().cancelRestoreTask();
    }

    public static void doMode(SummonMode summonMode, Summon summon) {
        doMode(summonMode, summon, 0, null);
    }

    public static void doMode(SummonMode summonMode, Summon summon, UnsummonType unsummonType) {
        doMode(summonMode, summon, 0, unsummonType);
    }

    public static void doMode(SummonMode summonMode, Summon summon, int targetObjId, UnsummonType unsummonType) {
        if (summon.getLifeStats().isAlreadyDead()) {
            return;
        }
        if (unsummonType != null && unsummonType.equals(UnsummonType.COMMAND) && !summonMode.equals(SummonMode.RELEASE)) {
            summon.cancelReleaseTask();
        }
        SummonController summonController = summon.getController();
        if (summonController == null) {
            return;
        }
        if (summon.getMaster() == null) {
            summon.getController().onDelete();
            return;
        }
        switch (summonMode) {
            case REST:
                summonController.restMode();
                break;
            case ATTACK:
                summonController.attackMode(targetObjId);
                break;
            case GUARD:
                summonController.guardMode();
                break;
            case RELEASE:
                if (unsummonType != null) {
                    summonController.release(unsummonType);
                }
                break;
        }
    }

    public static class ReleaseSummonTask implements Runnable {

        private final Summon owner;
        private final UnsummonType unsummonType;
        private final Player master;
        private final VisibleObject target;
        private final boolean isAttacked;

        public ReleaseSummonTask(Summon owner, UnsummonType unsummonType, boolean isAttacked) {
            this.owner = owner;
            this.unsummonType = unsummonType;
            master = owner.getMaster();
            target = master.getTarget();
            this.isAttacked = isAttacked;
        }

        @Override
        public void run() {
            owner.getController().onDie(master);
            owner.getController().delete();
            owner.setMaster(null);
            master.setSummon(null);
            
            if(!master.getEffectController().hasAbnormalEffect(417)){
            master.getEffectController().removeEffectByEffectType(EffectType.PROTECT); //fix//фикс бессмертия заклинателя под заменой
            }

            switch (unsummonType) {
                case COMMAND:
                case DISTANCE:
                case UNSPECIFIED:
                    master.sendPck(SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_UNSUMMONED(owner.getNameId()));
                    master.sendPck(new SM_SUMMON_OWNER_REMOVE(owner.getObjectId()));

                    master.sendPck(new SM_SUMMON_PANEL_REMOVE());
                    if (target instanceof Creature) {
                        final Creature lastAttacker = (Creature) target;
                        if (!master.getLifeStats().isAlreadyDead() && !lastAttacker.getLifeStats().isAlreadyDead() && isAttacked) {
                            ThreadPoolManager.getInstance().schedule(new Runnable() {

                                @Override
                                public void run() {
                                    lastAttacker.getAggroList().addHate(master, 1);
                                }
                            }, 1000L);
                        }
                    }
                    break;
            }
        }
    }
}
