/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.instance_bonusatrr;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InstanceBonusAttr", propOrder = {"penaltyAttr"})
public class InstanceBonusAttr {

    @XmlElement(name = "penalty_attr")
    protected List<InstancePenaltyAttr> penaltyAttr;

    @XmlAttribute(name = "buff_id", required = true)
    protected int buffId;

    public List<InstancePenaltyAttr> getPenaltyAttr() {
        if (penaltyAttr == null) {
            penaltyAttr = new ArrayList<>();
        }
        return penaltyAttr;
    }

    public int getBuffId() {
        return buffId;
    }

    public void setBuffId(int value) {
        buffId = value;
    }
}
