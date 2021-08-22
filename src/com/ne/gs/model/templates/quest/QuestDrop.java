/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.quest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestDrop")
public class QuestDrop {

    @XmlAttribute(name = "npc_id")
    protected int npcId;
    @XmlAttribute(name = "item_id")
    protected int itemId;
    @XmlAttribute
    protected int chance;
    @XmlAttribute(name = "drop_each_member")
    protected int dropEachMember = 0;

    @XmlTransient
    protected int questId;

    /**
     * Gets the value of the npcId property.
     *
     * @return possible object is {@link Integer }
     */
    public int getNpcId() {
        return npcId;
    }

    /**
     * Gets the value of the itemId property.
     *
     * @return possible object is {@link Integer }
     */
    public int getItemId() {
        return itemId;
    }

    /**
     * Gets the value of the chance property.
     *
     * @return possible object is {@link Integer }
     */
    public int getChance() {
        return chance == 0 ?  100 : chance;
    }

    /**
     * Gets the value of the dropEachMember property.
     *
     * @return possible object is {@link Boolean }
     */
    public boolean isDropEachMember() {
        return dropEachMember != 0;
    }

    /**
     * @return the questId
     */
    public int getQuestId() {
        return questId;
    }

    /**
     * @param questId
     *     the questId to set
     */
    public void setQuestId(int questId) {
        this.questId = questId;
    }

}
