/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.handlers.template.ItemOrders;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemOrdersData")
public class ItemOrdersData extends XMLQuest {

    @XmlAttribute(name = "start_item_id", required = true)
    protected int startItemId;

    @XmlAttribute(name = "talk_npc_id1")
    protected int talkNpc1;

    @XmlAttribute(name = "talk_npc_id2")
    protected int talkNpc2;

    @XmlAttribute(name = "end_npc_id", required = true)
    protected int endNpcId;

    @Override
    public void register(QuestEngine questEngine) {
        ItemOrders template = new ItemOrders(id, startItemId, talkNpc1, talkNpc2, endNpcId);
        questEngine.addQuestHandler(template);
    }
}
