/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
//fugnyA
package com.ne.gs.ai2.handler;

import java.util.Collections;

import com.ne.gs.ai2.NpcAI2;
import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.controllers.attack.AttackResult;
import com.ne.gs.controllers.attack.AttackStatus;
import com.ne.gs.model.TribeClass;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.npc.NpcTemplateType;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.knownlist.Visitor;
import mw.engines.geo.GeoHelper;


/**
 * @author ATracer
 */
public final class AggroEventHandler {

    /**
     * @param npcAI
     */
    public static void onAggro(NpcAI2 npcAI, Creature myTarget) {
        Npc owner = npcAI.getOwner();
        // TODO move out?
        if (myTarget.getAdminNeutral() == 1 || myTarget.getAdminNeutral() == 3 || myTarget.getAdminEnmity() == 1 || myTarget.getAdminEnmity() == 3) {
            return;
        }

        PacketSendUtility.broadcastPacket(owner,
            new SM_ATTACK(owner, myTarget, 0, 633, 0, Collections.singletonList(new AttackResult(0, AttackStatus.NORMALHIT))));

        ThreadPoolManager.getInstance().schedule(new AggroNotifier(owner, myTarget, true), 500);
    }

    public static boolean onCreatureNeedsSupport(NpcAI2 npcAI, Creature notMyTarget) {
        Npc owner = npcAI.getOwner();
        if (notMyTarget.isSupportFrom(owner) && MathUtil.isInRange(owner, notMyTarget, owner.getAggroRange())
           ) {
            VisibleObject myTarget = notMyTarget.getTarget();
            if (myTarget != null && myTarget instanceof Creature) {
                Creature targetCreature = (Creature) myTarget;

                PacketSendUtility.broadcastPacket(owner,
                    new SM_ATTACK(owner, targetCreature, 0, 633, 0, Collections.singletonList(new AttackResult(0, AttackStatus.NORMALHIT))));
                ThreadPoolManager.getInstance().schedule(new AggroNotifier(owner, targetCreature, false), 500);
                return true;
            }
        }
        return false;
    }

    public static boolean onGuardAgainstAttacker(NpcAI2 npcAI, Creature attacker) {
        Npc owner = npcAI.getOwner();
        TribeClass tribe = owner.getTribe();
        if (!tribe.isGuard() && owner.getObjectTemplate().getNpcTemplateType() != NpcTemplateType.GUARD) {
            return false;
        }
        VisibleObject target = attacker.getTarget();
        if (target != null && target instanceof Player) {
            Player playerTarget = (Player) target;
            if (!owner.isEnemy(playerTarget) && owner.isEnemy(attacker) && MathUtil.isInRange(owner, playerTarget, owner.getAggroRange())
                && GeoHelper.canSee(owner, attacker)) {
                owner.getAggroList().startHate(attacker);
                return true;
            }
        }
        return false;
    }

    private static final class AggroNotifier implements Runnable {

        private Npc aggressive;
        private Creature target;
        private final boolean broadcast;

        AggroNotifier(Npc aggressive, Creature target, boolean broadcast) {
            this.aggressive = aggressive;
            this.target = target;
            this.broadcast = broadcast;
        }

        @Override
        public void run() {
            aggressive.getAggroList().addHate(target, 1);
            if (broadcast) {
                aggressive.getKnownList().doOnAllNpcs(new Visitor<Npc>() {

                    @Override
                    public void visit(Npc object) {
                        object.getAi2().onCreatureEvent(AIEventType.CREATURE_NEEDS_SUPPORT, aggressive);
                    }
                });
            }
            aggressive = null;
            target = null;
        }

    }

}
