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

import com.ne.gs.controllers.attack.AttackUtil;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.ne.gs.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpellAttackEffect")
public class SpellAttackEffect extends AbstractOverTimeEffect {

    @Override
    public void startEffect(Effect effect) {
        int valueWithDelta = value + delta * effect.getSkillLevel();
        int critAddDmg = this.critAddDmg2 + this.critAddDmg1 * effect.getSkillLevel();
        int finalDamage = AttackUtil.calculateMagicalOverTimeSkillResult(effect, valueWithDelta, element, this.position, true, this.critProbMod2, critAddDmg);
        effect.setReservedInt(position, finalDamage);
        super.startEffect(effect);
    }

    @Override
    public void onPeriodicAction(Effect effect) {
        Creature effected = effect.getEffected();
        Creature effector = effect.getEffector();
        effected.getController().onAttack(effector, effect.getSkillId(), TYPE.DAMAGE, effect.getReservedInt(position), false, LOG.SPELLATK, effect.getAttackStatus());
        effected.getObserveController().notifyDotAttackedObservers(effector, effect);
    }
}
