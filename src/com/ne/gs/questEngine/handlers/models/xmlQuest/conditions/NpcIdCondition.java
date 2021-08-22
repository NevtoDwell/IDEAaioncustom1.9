/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers.models.xmlQuest.conditions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.questEngine.model.QuestEnv;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NpcIdCondition")
public class NpcIdCondition extends QuestCondition {

    @XmlAttribute(required = true)
    protected int values;

    /*
     * (non-Javadoc)
     * @see
     * com.ne.gs.questEngine.handlers.template.xmlQuest.condition.QuestCondition#doCheck(com.ne.gs
     * .questEngine.model.QuestEnv)
     */
    @Override
    public boolean doCheck(QuestEnv env) {
        int id = 0;
        VisibleObject visibleObject = env.getVisibleObject();
        if (visibleObject != null && visibleObject instanceof Npc) {
            id = ((Npc) visibleObject).getNpcId();
        }
        switch (getOp()) {
            case EQUAL:
                return id == values;
            case GREATER:
                return id > values;
            case GREATER_EQUAL:
                return id >= values;
            case LESSER:
                return id < values;
            case LESSER_EQUAL:
                return id <= values;
            case NOT_EQUAL:
                return id != values;
            default:
                return false;
        }
    }
}
