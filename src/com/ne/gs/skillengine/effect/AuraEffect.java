/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ne.commons.utils.concurrent.RunnableWrapper;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.network.aion.serverpackets.SM_MANTRA_EFFECT;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.SkillTemplate;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.utils.audit.AuditLogger;

/**
 * @author ATracer modified by kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuraEffect")
public class AuraEffect extends EffectTemplate {
    @XmlAttribute
    protected int distance;
    @XmlAttribute(name = "skill_id")
    protected int skillId;

    @Override
    public void applyEffect(Effect effect) {
        final Player effector = (Player) effect.getEffector();
        if (effector.getEffectController().isNoshowPresentBySkillId(effect.getSkillId())) {
            AuditLogger.info(effector, "Player might be abusing CM_CASTSPELL mantra effect Player kicked skill id: " + effect.getSkillId());

            // @hex1r0 VERY DIRTY attempt to avoid deadlocks
            /*ThreadPoolManager.getInstance().execute(new Runnable() {
                @Override
                public void run() {*/
                   effector.getClientConnection().closeNow();
                /*}
            });*/

            return;
        }
        effect.addToEffectedController();
    }

    @Override
    public void onPeriodicAction(Effect effect) {
        Player effector = (Player) effect.getEffector();
        if (!effector.isOnline()) {
            return;
        }
        int actualRange;
        if ((effector.isInGroup2()) || (effector.isInAlliance2())) {
            Collection<Player> onlynePlayers = effector.isInGroup2() ? effector.getPlayerGroup2().getOnlineMembers() : effector.getPlayerAllianceGroup2().getOnlineMembers();

            actualRange = (int) (distance * effector.getGameStats().getStat(StatEnum.BOOST_MANTRA_RANGE, 100).getCurrent() / 100.0F);
            for (Player player : onlynePlayers) {
                if (MathUtil.isIn3dRange(effector, player, actualRange)) {
                    applyAuraTo(player, effect);
                }
            }
        } else {
            applyAuraTo(effector, effect);
        }

        PacketSendUtility.broadcastPacket(effector, new SM_MANTRA_EFFECT(effector, skillId));
    }

    private void applyAuraTo(Player effected, Effect effect) {
        SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skillId);
        Effect e = new Effect(effected, effected, template, template.getLvl(), 0);
        e.initialize();
        e.applyEffect();
    }

    @Override
    public void startEffect(Effect effect) {
        effect.setPeriodicTask(ThreadPoolManager.getInstance().scheduleAtFixedRate(new AuraTask(effect), 0L, 6500L), position);
    }

    public void endEffect(Effect effect) {
    }

    private class AuraTask implements Runnable {

        private final Effect effect;

        public AuraTask(Effect effect) {
            this.effect = effect;
        }

        public void run() {
            onPeriodicAction(effect);

            Thread.yield(); // FIXME sync WTF??
        }
    }
}
