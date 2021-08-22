/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.npc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TalkInfo")
public class TalkInfo {

    @XmlAttribute(name = "distance")
    private int talkDistance = 2;

    @XmlAttribute(name = "delay")
    private int talkDelay;

    @XmlAttribute(name = "is_dialog")
    private boolean hasDialog;

    public int getDistance() {
        return talkDistance;
    }

    public int getDelay() {
        return talkDelay;
    }

    public boolean isDialogNpc() {
        return hasDialog;
    }
}
