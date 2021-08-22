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
import java.util.List;
import gnu.trove.map.hash.TIntObjectHashMap;

import com.ne.gs.model.templates.InstanceCooltime;

/**
 * @author VladimirZ
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "instance_cooltimes")
public class InstanceCooltimeData {

    @XmlElement(name = "instance_cooltime", required = true)
    protected List<InstanceCooltime> instanceCooltime;

    private final TIntObjectHashMap<InstanceCooltime> instanceCooltimes = new TIntObjectHashMap<>();

    /**
     * @param u
     * @param parent
     */
    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (InstanceCooltime tmp : instanceCooltime) {
            instanceCooltimes.put(tmp.getWorldId(), tmp);
        }
        instanceCooltime = null;
    }

    /**
     * @param worldId
     *
     * @return
     */
    public InstanceCooltime getInstanceCooltimeByWorldId(int worldId) {
        return instanceCooltimes.get(worldId);
    }

    /**
     * @param worldId
     *
     * @return entrance cooltime or 0 if no information in xml
     */
    public int getInstanceEntranceCooltime(int worldId) {
        InstanceCooltime coolTime = instanceCooltimes.get(worldId);
        return coolTime != null ? coolTime.getEntCoolTime() : 0;
    }

    public Integer size() {
        return instanceCooltimes.size();
    }
}
