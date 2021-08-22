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

import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.handlers.models.xmlQuest.events.OnKillEvent;
import com.ne.gs.questEngine.handlers.models.xmlQuest.events.OnTalkEvent;
import com.ne.gs.questEngine.handlers.template.XmlQuest;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "XmlQuest", propOrder = {"onTalkEvent", "onKillEvent"})
public class XmlQuestData extends XMLQuest {

    @XmlElement(name = "on_talk_event")
    protected List<OnTalkEvent> onTalkEvent;
    @XmlElement(name = "on_kill_event")
    protected List<OnKillEvent> onKillEvent;
    @XmlAttribute(name = "start_npc_id")
    protected Integer startNpcId;
    @XmlAttribute(name = "end_npc_id")
    protected Integer endNpcId;

    /**
     * Gets the value of the onTalkEvent property.
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the onTalkEvent property.
     * <p/>
     * For example, to add a new item, do as follows:
     * <p/>
     * <pre>
     * getOnTalkEvent().add(newItem);
     * </pre>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link OnTalkEvent }
     */
    public List<OnTalkEvent> getOnTalkEvent() {
        if (onTalkEvent == null) {
            onTalkEvent = new ArrayList<>();
        }
        return this.onTalkEvent;
    }

    /**
     * Gets the value of the onKillEvent property.
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the onKillEvent property.
     * <p/>
     * For example, to add a new item, do as follows:
     * <p/>
     * <pre>
     * getOnKillEvent().add(newItem);
     * </pre>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link OnKillEvent }
     */
    public List<OnKillEvent> getOnKillEvent() {
        if (onKillEvent == null) {
            onKillEvent = new ArrayList<>();
        }
        return this.onKillEvent;
    }

    /**
     * Gets the value of the startNpcId property.
     *
     * @return possible object is {@link Integer }
     */
    public Integer getStartNpcId() {
        return startNpcId;
    }

    /**
     * Gets the value of the endNpcId property.
     *
     * @return possible object is {@link Integer }
     */
    public Integer getEndNpcId() {
        return endNpcId;
    }

    /*
     * (non-Javadoc)
     * @see com.ne.gs.questEngine.handlers.models.QuestScriptData#register(com.ne.gs.questEngine
     * .QuestEngine)
     */
    @Override
    public void register(QuestEngine questEngine) {
        questEngine.addQuestHandler(new XmlQuest(this));
    }
}
