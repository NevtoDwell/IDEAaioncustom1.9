/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers.models.xmlQuest.operations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.services.QuestService;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CollectItemQuestOperation", propOrder = {"_true", "_false"})
public class CollectItemQuestOperation extends QuestOperation {

    @XmlElement(name = "true", required = true)
    protected QuestOperations _true;
    @XmlElement(name = "false", required = true)
    protected QuestOperations _false;
    @XmlAttribute
    protected Boolean removeItems;

    /*
     * (non-Javadoc)
     * @see
     * com.ne.gs.questEngine.handlers.models.xmlQuest.operations.QuestOperation#doOperate(com.ne.gs
     * .questEngine.model.QuestEnv)
     */
    @Override
    public void doOperate(QuestEnv env) {
        if (QuestService.collectItemCheck(env, removeItems == null ? true : false)) {
            _true.operate(env);
        } else {
            _false.operate(env);
        }
    }

}
