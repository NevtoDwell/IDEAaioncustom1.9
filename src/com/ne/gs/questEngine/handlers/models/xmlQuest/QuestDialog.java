/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers.models.xmlQuest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.questEngine.handlers.models.xmlQuest.conditions.QuestConditions;
import com.ne.gs.questEngine.handlers.models.xmlQuest.operations.QuestOperations;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestDialog", propOrder = {"conditions", "operations"})
public class QuestDialog {

    protected QuestConditions conditions;
    protected QuestOperations operations;
    @XmlAttribute(required = true)
    protected int id;

    public boolean operate(QuestEnv env, QuestState qs) {
        if (env.getDialogId() != id) {
            return false;
        }
        if (conditions == null || conditions.checkConditionOfSet(env)) {
            if (operations != null) {
                return operations.operate(env);
            }
        }
        return false;
    }
}
