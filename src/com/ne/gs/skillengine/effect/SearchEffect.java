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
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureSeeState;
import com.ne.gs.network.aion.serverpackets.SM_PLAYER_STATE;
import com.ne.gs.services.player.PlayerVisualStateService;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author Sweetkr
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SearchEffect")
public class SearchEffect extends EffectTemplate {

    // TODO! value should be enum already (@XmlEnum) - having int here is just stupid

    @XmlAttribute
    protected CreatureSeeState state;

    @Override
    public void applyEffect(Effect effect) {
        effect.addToEffectedController();
    }

    @Override
    public void endEffect(Effect effect) {
        Creature effected = effect.getEffected();

        effected.unsetSeeState(state);

        if ((SecurityConfig.INVIS) && ((effected instanceof Player))) {
            PlayerVisualStateService.seeValidate((Player) effected);
        }
        PacketSendUtility.broadcastPacketAndReceive(effected, new SM_PLAYER_STATE(effected));
    }

    @Override
    public void startEffect(Effect effect) {
        Creature effected = effect.getEffected();

        effected.setSeeState(state);

        if ((SecurityConfig.INVIS) && ((effected instanceof Player))) {
            PlayerVisualStateService.seeValidate((Player) effected);
        }
        PacketSendUtility.broadcastPacketAndReceive(effected, new SM_PLAYER_STATE(effected));
    }
}
