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
import java.util.ArrayList;
import java.util.List;

import com.ne.gs.model.templates.quest.QuestItems;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.handlers.template.WorkOrders;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WorkOrdersData", propOrder = {"giveComponent"})
public class WorkOrdersData extends XMLQuest {

    @XmlElement(name = "give_component", required = true)
    protected List<QuestItems> giveComponent;
    @XmlAttribute(name = "start_npc_ids", required = true)
    protected List<Integer> startNpcIds;
    @XmlAttribute(name = "recipe_id", required = true)
    protected int recipeId;

    /**
     * Gets the value of the giveComponent property.
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the giveComponent property.
     * <p/>
     * For example, to add a new item, do as follows:
     * <p/>
     * <pre>
     * getGiveComponent().add(newItem);
     * </pre>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link QuestItems }
     */
    public List<QuestItems> getGiveComponent() {
        if (giveComponent == null) {
            giveComponent = new ArrayList<>();
        }
        return this.giveComponent;
    }

    /**
     * Gets the value of the startNpcId property.
     */
    public List<Integer> getStartNpcIds() {
        return startNpcIds;
    }

    /**
     * Gets the value of the recipeId property.
     */
    public int getRecipeId() {
        return recipeId;
    }

    /*
     * (non-Javadoc)
     * @see com.ne.gs.questEngine.handlers.models.QuestScriptData#register()
     */
    @Override
    public void register(QuestEngine questEngine) {
        questEngine.addQuestHandler(new WorkOrders(this));
    }
}
