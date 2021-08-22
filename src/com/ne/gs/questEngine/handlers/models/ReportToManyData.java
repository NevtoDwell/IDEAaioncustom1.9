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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import javolution.util.FastMap;

import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.handlers.template.ReportToMany;

/**
 * @author Hilgert
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReportToManyData")
public class ReportToManyData extends XMLQuest {

    @XmlAttribute(name = "start_item_id")
    protected int startItemId;

    @XmlAttribute(name = "start_npc_ids")
    protected List<Integer> startNpcIds;

    @XmlAttribute(name = "end_npc_ids")
    protected List<Integer> endNpcIds;

    @XmlAttribute(name = "start_dialog_id")
    protected int startDialog;
    @XmlAttribute(name = "end_dialog_id")
    protected int endDialog;
    @XmlElement(name = "npc_infos", required = true)
    protected List<NpcInfos> npcInfos;

    @Override
    public void register(QuestEngine questEngine) {
        int maxVar = 0;
        FastMap<Integer, NpcInfos> NpcInfo = new FastMap<>();
        for (NpcInfos mi : npcInfos) {
            NpcInfo.put(mi.getNpcId(), mi);
            if (mi.getVar() > maxVar) {
                maxVar = mi.getVar();
            }
        }
        ReportToMany template = new ReportToMany(id, startItemId, startNpcIds, endNpcIds, NpcInfo, startDialog, endDialog, maxVar);

        questEngine.addQuestHandler(template);
    }
}
