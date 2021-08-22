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

import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestVarCondition")
public class QuestVarCondition extends QuestCondition {

    @XmlAttribute(required = true)
    protected int value;
    @XmlAttribute(name = "var_id", required = true)
    protected int varId;

    /*
     * (non-Javadoc)
     * @see
     * com.ne.gs.questEngine.handlers.template.xmlQuest.condition.QuestCondition#doCheck(com.ne.gs
     * .questEngine.model.QuestEnv)
     */
    @Override
    public boolean doCheck(QuestEnv env) {
        QuestState qs = env.getPlayer().getQuestStateList().getQuestState(env.getQuestId());
        if (qs == null) {
            return false;
        }
        int var = qs.getQuestVars().getVarById(varId);
        switch (getOp()) {
            case EQUAL:
                return var == value;
            case GREATER:
                return var > value;
            case GREATER_EQUAL:
                return var >= value;
            case LESSER:
                return var < value;
            case LESSER_EQUAL:
                return var <= value;
            case NOT_EQUAL:
                return var != value;
            default:
                return false;
        }
    }

}
