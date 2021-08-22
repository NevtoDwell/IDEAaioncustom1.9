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
import com.ne.gs.ai2.AIState;
import com.ne.gs.ai2.NpcAI2;
import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.ObserverType;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.network.aion.serverpackets.SM_TARGET_IMMOBILIZE;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.PositionUtil;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import mw.engines.geo.GeoEngine;
import mw.engines.geo.collision.CollidableType;
import mw.engines.geo.math.Vector3f;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.concurrent.ScheduledFuture;


/**
 * @author Sarynth
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FearEffect")
public class FearEffect extends EffectTemplate {

    @XmlAttribute
    protected int resistchance = 100;

    @Override
    public void applyEffect(Effect effect) {
        Creature effected = effect.getEffected();
        effected.getController().cancelCurrentSkill();     
        effect.addToEffectedController();
        // Снимает инвиз
        effected.getEffectController().removeHideEffects();    
    }

    @Override
    public void calculate(Effect effect) {

        super.calculate(effect, StatEnum.FEAR_RESISTANCE, null);
    }

    @Override
    public void startEffect(final Effect effect) {
        Creature effector = effect.getEffector();
        final Creature effected = effect.getEffected();
		
      //fix снятия стойки
      if (effected instanceof Player) {
          Player playerEffector = (Player) effected;
          if (playerEffector.getController().isUnderStance()) {
              playerEffector.getController().stopStance();
          }
      }
        
        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {                              
                if(effected.isInState(CreatureState.GLIDING)){
                   PacketSendUtility.broadcastPacket((Player) effected, new SM_EMOTION(effected, EmotionType.STOP_GLIDE, 1, 0), true);
                }
            }
        }, 500);  
      
        effect.setAbnormal(AbnormalState.FEAR.getId());
        effected.getEffectController().setAbnormal(AbnormalState.FEAR.getId());

        PacketSendUtility.broadcastPacketAndReceive(effected, new SM_TARGET_IMMOBILIZE(effected));
        effected.getController().stopMoving();

        if (effected instanceof Npc) {

            ((NpcAI2) effected.getAi2()).setStateIfNot(AIState.FEAR);

            if (!MathUtil.isNearCoordinates(effected, effector, 40))
                return;
            byte moveAwayHeading = PositionUtil.getMoveAwayHeading(effector, effected);
            double radian = Math.toRadians(MathUtil.convertHeadingToDegree(moveAwayHeading));
            float maxDistance = 1000;
            float x1 = (float) (Math.cos(radian) * maxDistance);
            float y1 = (float) (Math.sin(radian) * maxDistance);

            Vector3f closestCollision = GeoEngine.getAvailablePoint(effected, x1, y1, obstacle_intentions);

            ((Npc) effected).getMoveController().moveToPoint(closestCollision.getX(), closestCollision.getY(),
                    closestCollision.getZ());

        }
        else {
            ScheduledFuture<?> fearTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new FearTask(effector, effected), 0L, 1000L);
            effect.setPeriodicTask(fearTask, position);
        }

        //resistchance of fear effect to damage, if value is lower than 100, fear can be interrupted bz damage
        //example skillId: 540 Terrible howl
        if (resistchance < 100) {
            ActionObserver observer = new ActionObserver(ObserverType.ATTACKED) {
                @Override
                public void attacked(Creature creature) {
                    if (Rnd.get(0, 100) > resistchance)
                        effected.getEffectController().removeEffect(effect.getSkillId());
                }
            };
            effected.getObserveController().addObserver(observer);
            effect.setActionObserver(observer, position);
        }
    }

    @Override
    public void endEffect(Effect effect) {

        effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.FEAR.getId());

        // for now we support only players
        //if (GeoDataConfig.FEAR_ENABLE) {
            //effect.getEffected().getMoveController().abortMove();// TODO impl stopMoving?
        //}
        if (effect.getEffected() instanceof Npc) {
            ((NpcAI2) effect.getEffected().getAi2()).setStateIfNot(AIState.FEAR);
            effect.getEffected().getMoveController().abortMove();
            effect.getEffected().getAi2().onCreatureEvent(AIEventType.ATTACK, effect.getEffector());
        }
        //not need here
        //PacketSendUtility.broadcastPacketAndReceive(effect.getEffected(), new SM_TARGET_IMMOBILIZE(effect.getEffected()));

        if (resistchance < 100) {
            ActionObserver observer = effect.getActionObserver(position);
            if (observer != null)
                effect.getEffected().getObserveController().removeObserver(observer);
        }
    }

    static final byte obstacle_intentions = (byte) (CollidableType.PHYSICAL.getId() | CollidableType.DOOR.getId());

    class FearTask implements Runnable {


        private Creature effector;
        private Creature effected;

        FearTask(Creature effector, Creature effected) {
            this.effector = effector;
            this.effected = effected;
        }

        @Override
        public void run() {
            if (effected.getEffectController().isUnderFear()) {
                if (!MathUtil.isNearCoordinates(effected, effector, 40))
                    return;
                byte moveAwayHeading = PositionUtil.getMoveAwayHeading(effector, effected);
                double radian = Math.toRadians(MathUtil.convertHeadingToDegree(moveAwayHeading));
                float maxDistance = effected.getGameStats().getMovementSpeedFloat() * 2;
                float x1 = (float) (Math.cos(radian) * maxDistance);
                float y1 = (float) (Math.sin(radian) * maxDistance);

                Vector3f closestCollision = GeoEngine.getAvailablePoint(effected, x1, y1, obstacle_intentions);

                if (effected instanceof Npc) {
                    //((Npc) effected).getMoveController().resetMove();
                    ((Npc) effected).getMoveController().moveToPoint(closestCollision.getX(), closestCollision.getY(),
                            closestCollision.getZ());
                } else {
                    effected.getMoveController().setNewDirection(closestCollision.getX(), closestCollision.getY(),
                            closestCollision.getZ(), moveAwayHeading);
                    effected.getMoveController().startMovingToDestination();
                }
            }
        }
    }
}
