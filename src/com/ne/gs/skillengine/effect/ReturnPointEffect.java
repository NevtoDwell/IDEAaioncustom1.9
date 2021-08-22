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

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReturnPointEffect")
public class ReturnPointEffect extends EffectTemplate {

    @Override
    public void applyEffect(Effect effect) {
        ItemTemplate itemTemplate = effect.getItemTemplate();
        int worldId = itemTemplate.getReturnWorldId();
        String pointAlias = itemTemplate.getReturnAlias();
        TeleportService.useTeleportScroll((Player) effect.getEffector(), pointAlias, worldId);
    }

    @Override
    public void calculate(Effect effect) {
        ItemTemplate itemTemplate = effect.getItemTemplate();
        if (itemTemplate != null) {
            effect.addSucessEffect(this);
        }
    }

}
