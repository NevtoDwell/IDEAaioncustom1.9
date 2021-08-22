/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.effect;


import com.ne.commons.utils.Rnd;
import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.ObserverType;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.network.aion.serverpackets.SM_TARGET_IMMOBILIZE;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RootEffect")
public class RootEffect extends EffectTemplate {

    @XmlAttribute
    protected int resistchance = 100;

    @Override
    public void applyEffect(Effect effect) {
        //Проверка на уклонение https://free-redmine.saas-secure.com/issues/12158
        if(effect.getEffected().getEffectController().hasAbnormalEffect(3328)) {
            return;
        }
        effect.addToEffectedController();
    }

    @Override
    public void calculate(Effect effect) {
        super.calculate(effect, StatEnum.ROOT_RESISTANCE, null);
    }

    @Override
    public void startEffect(final Effect effect) {
        final Creature effected = effect.getEffected();
        //Creature effector = effect.getEffector();
        effected.getMoveController().abortMove();
        effected.getEffectController().setAbnormal(AbnormalState.ROOT.getId());
        effect.setAbnormal(AbnormalState.ROOT.getId());

        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {                              
                if(effected.isInState(CreatureState.GLIDING)){
                   PacketSendUtility.broadcastPacket((Player) effected, new SM_EMOTION(effected, EmotionType.STOP_GLIDE, 1, 0), true);
                }
            }
        }, 500);  
        
        PacketSendUtility.broadcastPacketAndReceive(effected, new SM_TARGET_IMMOBILIZE(effected));
            
        ActionObserver observer = new ActionObserver(ObserverType.ATTACKED) {
            @Override
            public void attacked(Creature creature) {
                
                   // if(!effector.getEffectController().isAbnormalSet(AbnormalState.BLIND)){
                     //   effected.getEffectController().removeEffect(effect.getSkillId());
                    //}
                if (Rnd.get(0, 100) > resistchance) {
                    effected.getEffectController().removeEffect(effect.getSkillId());
                }            
            }
        };
        effected.getObserveController().addObserver(observer);
        effect.setActionObserver(observer, position);
    }

    @Override
    public void endEffect(Effect effect) {
        effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.ROOT.getId());
        ActionObserver observer = effect.getActionObserver(position);
        if (observer != null) {
            effect.getEffected().getObserveController().removeObserver(observer);
        }
    }
}
