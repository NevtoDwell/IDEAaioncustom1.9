/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.periodicaction;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * @author antness
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PeriodicActions", propOrder = "periodicActions")
public class PeriodicActions {

    @XmlElements({@XmlElement(name = "hpuse", type = HpUsePeriodicAction.class), @XmlElement(name = "mpuse", type = MpUsePeriodicAction.class)})
    protected List<PeriodicAction> periodicActions;
    @XmlAttribute(name = "checktime")
    protected int checktime;

    public List<PeriodicAction> getPeriodicActions() {
        return periodicActions;
    }

    public int getChecktime() {
        return checktime;
    }
}
