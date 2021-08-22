/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.portal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestReq")
public class QuestReq {

    @XmlAttribute(name = "quest_id")
    protected int questId;

    @XmlAttribute(name = "quest_step")
    protected int questStep;

    @XmlAttribute(name = "err_quest")
    protected int errQuest;

    public int getQuestId() {
        return questId;
    }

    public void setQuestId(int value) {
        questId = value;
    }

    public int getQuestStep() {
        return questStep;
    }

    public void setQuestStep(int value) {
        questStep = value;
    }

    public int getErrQuest() {
        return errQuest;
    }
}
