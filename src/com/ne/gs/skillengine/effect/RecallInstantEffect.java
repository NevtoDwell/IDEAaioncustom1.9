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
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.RequestResponseHandler;
import com.ne.gs.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author Bio, Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecallInstantEffect")
public class RecallInstantEffect extends EffectTemplate {

    @Override
    public void applyEffect(Effect effect) {
        final Creature effector = effect.getEffector();
        Player effected = (Player) effect.getEffected();

        final int worldId = effect.getWorldId();
        final int instanceId = effect.getInstanceId();
        final float locationX = effect.getSkill().getX();
        final float locationY = effect.getSkill().getY();
        final float locationZ = effect.getSkill().getZ();
        final int locationH = effect.getSkill().getH();

        /**
         * TODO need to confirm if cannot be summoned while on abnormal effects stunned, sleeping, feared, etc.
         */
        RequestResponseHandler rrh = new RequestResponseHandler(effector) {

            @Override
            public void denyRequest(Creature effector, Player effected) {

                ((Player) effector).sendPck(SM_SYSTEM_MESSAGE.STR_MSG_Recall_Rejected_EFFECT(effected.getName()));
                effected.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_Recall_Rejected_EFFECT(effector.getName()));
            }

            @Override
            public void acceptRequest(Creature effector, Player effected) {
                TeleportService.teleportTo(effected, worldId, instanceId, locationX, locationY, locationZ, locationH);
            }
        };

        effected.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_SUMMON_PARTY_DO_YOU_ACCEPT_REQUEST, rrh);
        effected.sendPck(new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_SUMMON_PARTY_DO_YOU_ACCEPT_REQUEST, 0, 0, effector.getName(),
            "Summon Group Member", 30));
    }

    @Override
    public void calculate(Effect effect) {
        Creature effector = effect.getEffector();

        if (!(effect.getEffected() instanceof Player)) {
            return;
        }
        Player effected = (Player) effect.getEffected();

        if (effected.getController().isInCombat()) {
            return;
        }

        if (effector.getWorldId() == effected.getWorldId() && !effector.isInInstance() && !effector.isEnemy(effected)) {
            effect.getSkill().setTargetPosition(effector.getX(), effector.getY(), effector.getZ(), effector.getHeading());
            effect.addSucessEffect(this);
        }
    }
}
