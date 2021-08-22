/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.effect;

import com.ne.gs.controllers.attack.AttackStatus;
import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.AttackCalcObserver;
import com.ne.gs.controllers.observer.AttackStatusObserver;
import com.ne.gs.controllers.observer.ObserverType;
import com.ne.gs.model.EmotionType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.network.aion.serverpackets.SM_QUEST_ACTION;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SleepEffect")
public class SleepEffect extends EffectTemplate {

    @Override
    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
    }

    @Override
    public void calculate(Effect effect) {
        super.calculate(effect, StatEnum.SLEEP_RESISTANCE, null);
    }

    @Override
    public void startEffect(final Effect effect) {
        final Creature effected = effect.getEffected();
        effected.getController().cancelCurrentSkill();       
        effect.setAbnormal(AbnormalState.SLEEP.getId());  
        
        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {                              
                if(effected.isInState(CreatureState.GLIDING)){
                   PacketSendUtility.broadcastPacket((Player) effected, new SM_EMOTION(effected, EmotionType.STOP_GLIDE, 1, 0), true);
                }
            }
        }, 500);    
        
        effected.getEffectController().setAbnormal(AbnormalState.SLEEP.getId());
		if (effected.getEffectController().hasAbnormalEffect(572)) {

            AttackCalcObserver acObserver = new AttackStatusObserver(value, AttackStatus.DODGE) {
                @Override
                public boolean checkStatus(AttackStatus status) {
                    if (status == AttackStatus.DODGE || status == AttackStatus.RESIST) {
                        if (value <= 1) {
                            effect.endEffect();
                        } else {    
                            value--;
                        }

                        return true;
                    } else {
                        return false;
                    }
                }
            };
            effect.getEffected().getObserveController().addAttackCalcObserver(acObserver);
            effect.setAttackStatusObserver(acObserver, position);
        } else {
            effect.setCancelOnDmg(true);
        }
    }

    @Override
    public void endEffect(Effect effect) {
        effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.SLEEP.getId());
	AttackCalcObserver acObserver = effect.getAttackStatusObserver(position);
        if (acObserver != null) {
            effect.getEffected().getObserveController().removeAttackCalcObserver(acObserver);
        }
    }
}
