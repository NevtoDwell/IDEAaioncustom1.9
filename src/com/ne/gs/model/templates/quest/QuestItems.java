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
import javax.xml.bind.annotation.XmlType;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestItems")
public class QuestItems {

    @XmlAttribute(name = "item_id")
    protected int itemId;
    @XmlAttribute
    protected int count;

    /**
     * Constructor used by unmarshaller
     */
    public QuestItems() {
        this.count = 1;
    }

    public QuestItems(int itemId, int count) {
        super();
        this.itemId = itemId;
        this.count = count;
    }

    /**
     * Gets the value of the itemId property.
     *
     * @return possible object is {@link int }
     */
    public int getItemId() {
        return itemId;
    }

    /**
     * Gets the value of the count property.
     *
     * @return possible object is {@link int }
     */
    public int getCount() {
        return count;
    }

}
