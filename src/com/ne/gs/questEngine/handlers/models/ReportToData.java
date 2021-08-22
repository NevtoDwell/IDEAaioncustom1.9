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
import java.util.List;

import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.handlers.template.ReportTo;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReportToData")
public class ReportToData extends XMLQuest {

    @XmlAttribute(name = "start_npc_ids")
    protected List<Integer> startNpcIds;

    @XmlAttribute(name = "end_npc_ids")
    protected List<Integer> endNpcIds;

    @XmlAttribute(name = "item_id", required = true)
    protected int itemId;

    @Override
    public void register(QuestEngine questEngine) {
        ReportTo template = new ReportTo(id, startNpcIds, endNpcIds, itemId);
        questEngine.addQuestHandler(template);
    }
}
