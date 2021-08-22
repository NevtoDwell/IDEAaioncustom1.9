/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.dataholders;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import com.ne.gs.model.Race;
import com.ne.gs.model.templates.portal.InstanceExit;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"instanceExit"})
@XmlRootElement(name = "instance_exits")
public class InstanceExitData {

    @XmlElement(name = "instance_exit")
    protected List<InstanceExit> instanceExit;

    @XmlTransient
    protected List<InstanceExit> instanceExits = new ArrayList<>();

    void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        for (InstanceExit exit : instanceExit) {
            instanceExits.add(exit);
        }
        instanceExit = null;
    }

    public InstanceExit getInstanceExit(int worldId, Race race) {
        for (InstanceExit exit : instanceExits) {
            if (exit.getInstanceId() == worldId && (race.equals(exit.getRace()) || exit.getRace().equals(Race.PC_ALL))) {
                return exit;
            }
        }
        return null;
    }

    public int size() {
        return instanceExits.size();
    }
}
