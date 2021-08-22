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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.questEngine.model.QuestEnv;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GiveItemOperation")
public class GiveItemOperation extends QuestOperation {

    @XmlAttribute(name = "item_id", required = true)
    protected int itemId;
    @XmlAttribute(required = true)
    protected int count;

    /*
     * (non-Javadoc)
     * @see
     * com.ne.gs.questEngine.handlers.models.xmlQuest.operations.QuestOperation#doOperate(com.ne.gs
     * .questEngine.model.QuestEnv)
     */
    @Override
    public void doOperate(QuestEnv env) {
        Player player = env.getPlayer();
        player.getController().addItems(itemId, count);
    }

}
