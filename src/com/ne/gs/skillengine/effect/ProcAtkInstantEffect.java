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

import com.ne.gs.model.DescId;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.skillengine.action.DamageType;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author Wakizashi
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcAtkInstantEffect")
public class ProcAtkInstantEffect extends DamageEffect {

    @Override
    public void applyEffect(Effect effect) {
        if (effect.getEffector() instanceof Player) {
            ((Player) effect.getEffector()).sendPck(new SM_SYSTEM_MESSAGE(1301062, DescId.of(effect.getSkillTemplate().getNameId())));
        }

        effect.getEffected().getController().onAttack(effect.getEffector(), effect.getSkillId(), TYPE.DAMAGE, effect.getReserved1(), false, LOG.PROCATKINSTANT, effect.getAttackStatus());
    }

    @Override
    public void calculate(Effect effect) {
        super.calculate(effect, DamageType.MAGICAL);
    }
}
