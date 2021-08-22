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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

import com.ne.gs.questEngine.model.ConditionUnionType;
import com.ne.gs.questEngine.model.QuestEnv;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestConditions", propOrder = {"conditions"})
public class QuestConditions {

    @XmlElements({@XmlElement(name = "quest_status", type = QuestStatusCondition.class),
                  @XmlElement(name = "npc_id", type = NpcIdCondition.class),
                  @XmlElement(name = "pc_inventory", type = PcInventoryCondition.class),
                  @XmlElement(name = "quest_var", type = QuestVarCondition.class),
                  @XmlElement(name = "dialog_id", type = DialogIdCondition.class)})
    protected List<QuestCondition> conditions;
    @XmlAttribute(required = true)
    protected ConditionUnionType operate;

    public boolean checkConditionOfSet(QuestEnv env) {
        boolean inCondition = (operate == ConditionUnionType.AND);
        for (QuestCondition cond : conditions) {
            boolean bCond = cond.doCheck(env);
            switch (operate) {
                case AND:
                    if (!bCond) {
                        return false;
                    }
                    inCondition = inCondition && bCond;
                    break;
                case OR:
                    if (bCond) {
                        return true;
                    }
                    inCondition = inCondition || bCond;
                    break;
            }
        }
        return inCondition;
    }

}
