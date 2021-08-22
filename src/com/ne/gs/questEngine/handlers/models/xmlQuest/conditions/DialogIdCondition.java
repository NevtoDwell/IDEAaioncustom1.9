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

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DialogIdCondition")
public class DialogIdCondition extends QuestCondition {

    @XmlAttribute(required = true)
    protected int value;

    /**
     * Gets the value of the value property.
     */
    public int getValue() {
        return value;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.ne.gs.questEngine.handlers.template.xmlQuest.condition.QuestCondition#doCheck(com.ne.gs
     * .questEngine.model.QuestEnv)
     */
    @Override
    public boolean doCheck(QuestEnv env) {
        int data = env.getDialogId();
        switch (getOp()) {
            case EQUAL:
                return data == value;
            case NOT_EQUAL:
                return data != value;
            default:
                return false;
        }
    }
}
