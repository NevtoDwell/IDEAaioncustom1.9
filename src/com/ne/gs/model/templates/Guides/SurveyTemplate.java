/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.Guides;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SurveyTemplate")
public class SurveyTemplate {

    @XmlAttribute(name = "itemId")
    private int itemId;
    @XmlAttribute(name = "count")
    private long count;

    /**
     * @return the count
     */
    public long getCount() {
        return this.count;
    }

    /**
     * @return the itemId
     */
    public int getItemId() {
        return this.itemId;
    }
}
