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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.questEngine.model.ConditionOperation;
import com.ne.gs.questEngine.model.QuestEnv;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestCondition")
@XmlSeeAlso({NpcIdCondition.class, DialogIdCondition.class, PcInventoryCondition.class, QuestVarCondition.class,
             QuestStatusCondition.class})
public abstract class QuestCondition {

    @XmlAttribute(required = true)
    protected ConditionOperation op;

    /**
     * Gets the value of the op property.
     *
     * @return possible object is {@link ConditionOperation }
     */
    public ConditionOperation getOp() {
        return op;
    }

    public abstract boolean doCheck(QuestEnv env);

}
