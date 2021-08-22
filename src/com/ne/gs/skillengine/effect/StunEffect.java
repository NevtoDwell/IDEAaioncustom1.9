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

import com.mw.TempConst;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.network.aion.serverpackets.SM_TARGET_IMMOBILIZE;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StunEffect")
public class StunEffect extends EffectTemplate {

    @Override
    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
    }

    @Override
    public void calculate(Effect effect) {
        //lonefoxx
	final Creature effected = effect.getEffected();

    	if (effected instanceof Player) {
            switch (effect.getSkillId()) {
                case 155:
                case 156:
                case 157:
    		case 181:
                    super.calculate(effect, null, null);
    		return;
            }	
    	}

        Skill castingSkill = effected.getCastingSkill();
    	if(castingSkill != null && castingSkill.getSkillId() == TempConst.SKILL_ID_REMOVE_SHOCK)
    	    return;

        super.calculate(effect, StatEnum.STUN_RESISTANCE, null);
    }

    @Override
    public void startEffect(Effect effect) {
        Creature effected = effect.getEffected();
        effected.getController().cancelCurrentSkill();
        effected.getMoveController().abortMove();
        effect.getEffected().getEffectController().setAbnormal(AbnormalState.STUN.getId());
        effect.setAbnormal(AbnormalState.STUN.getId());
        PacketSendUtility.broadcastPacketAndReceive(effect.getEffected(), new SM_TARGET_IMMOBILIZE(effect.getEffected()));
    }

    @Override
    public void endEffect(Effect effect) {
        effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.STUN.getId());
    }
}
