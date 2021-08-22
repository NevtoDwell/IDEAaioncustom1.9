/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers.models.xmlQuest.operations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.questEngine.model.QuestEnv;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KillOperation")
public class KillOperation extends QuestOperation {

    /*
     * (non-Javadoc)
     * @seecom.ne.gs.questEngine.handlers.models.xmlQuest.operations.QuestOperation#doOperate(com.aionemu.
     * gameserver.services.QuestService, com.ne.gs.questEngine.model.QuestEnv)
     */
    @Override
    public void doOperate(QuestEnv env) {
        if (env.getVisibleObject() instanceof Npc) {
            ((Npc) env.getVisibleObject()).getController().onDie(env.getPlayer());
        }

    }

}
