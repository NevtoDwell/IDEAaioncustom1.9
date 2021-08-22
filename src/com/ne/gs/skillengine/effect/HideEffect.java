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

import com.ne.gs.configs.main.SecurityConfig;
import com.ne.gs.controllers.attack.AttackUtil;
import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.ObserverType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureVisualState;
import com.ne.gs.network.aion.serverpackets.SM_PLAYER_STATE;
import com.ne.gs.services.player.PlayerVisualStateService;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author Sweetkr
 * @author Cura
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HideEffect")
public class HideEffect extends BufEffect {

    @XmlAttribute
    protected CreatureVisualState state;
    @XmlAttribute(name = "bufcount")
    protected int buffCount;

    @XmlAttribute
    protected int type = 0;

    @Override
    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
    }

    @Override
    public void endEffect(Effect effect) {
        super.endEffect(effect);

        Creature effected = effect.getEffected();
        effected.getEffectController().unsetAbnormal(AbnormalState.HIDE.getId());

        effected.unsetVisualState(state);

        if ((effected instanceof Player)) {
            ActionObserver observer = effect.getActionObserver(position);
            effect.getEffected().getObserveController().removeObserver(observer);
        }

        PacketSendUtility.broadcastPacketAndReceive(effected, new SM_PLAYER_STATE(effected));
        if ((SecurityConfig.INVIS) && ((effected instanceof Player))) {
            PlayerVisualStateService.hideValidate((Player) effected);
        }
    }

    @Override
    public void startEffect(final Effect effect) {
        super.startEffect(effect);

        final Creature effected = effect.getEffected();
        effected.getEffectController().setAbnormal(AbnormalState.HIDE.getId());
        effect.setAbnormal(AbnormalState.HIDE.getId());

        effected.setVisualState(state);

        AttackUtil.cancelCastOn(effected);

        PacketSendUtility.broadcastPacketAndReceive(effected, new SM_PLAYER_STATE(effected));

        ThreadPoolManager.getInstance().schedule(new Runnable() {

            public void run() {
                AttackUtil.removeTargetFrom(effected, true);
            }
        }, 500L);

        if ((effected instanceof Player)) {
            if (SecurityConfig.INVIS) {
                PlayerVisualStateService.hideValidate((Player) effected);
            }

            // Remove Hide when use skill
            ActionObserver observer = new HideBreakObserver(effect, buffCount);

            effected.getObserveController().addObserver(observer);
            effect.setActionObserver(observer, position);

            if (type == 0) {
                effect.setCancelOnDmg(true);
            }

        } else if (type == 0) {
            effect.setCancelOnDmg(true);

            effected.getObserveController().attach(new ActionObserver(ObserverType.ATTACK) {

                public void attack(Creature creature) {
                    effect.endEffect();
                }
            });
            effected.getObserveController().attach(new ActionObserver(ObserverType.SKILLUSE) {

                public void skilluse(Skill skill) {
                    effect.endEffect();
                }
            });
        }
    }

    private static class HideBreakObserver extends ActionObserver{

        private final Effect _effect;

        private int _maxBuffs;

        public HideBreakObserver(Effect effect, int maxBuffs) {
            super(ObserverType.HIDE_CANCEL);

            _effect = effect;
            _maxBuffs = maxBuffs;
        }

        @Override
        public void skilluse(Skill skill) {

            _maxBuffs--;

            if (skill.isSelfBuff() && _maxBuffs > 0) {
                return;
            }

            _effect.endEffect();
        }

        @Override
        public void loot() {
            _effect.endEffect();
        }

        @Override
        public void gather() {
            _effect.endEffect();
        }

        @Override
        public void npcdialogrequested(Npc npc) {
            _effect.endEffect();
        }

        @Override
        public void attack(Creature creature) {
            _effect.endEffect();
        }

        @Override
        public void itemused(Item item) {
            _effect.endEffect();
        }
    }
}
