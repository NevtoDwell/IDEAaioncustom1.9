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
import java.util.List;
import gnu.trove.map.hash.TIntObjectHashMap;

import com.ne.gs.model.autogroup.AutoGroup;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "autoGroup"
})
@XmlRootElement(name = "auto_groups")
public class AutoGroupData {

    @XmlElement(name = "auto_group")
    protected List<AutoGroup> autoGroup;
    @XmlTransient
    private final TIntObjectHashMap<AutoGroup> autoGroupByInstanceId = new TIntObjectHashMap<>();
    @XmlTransient
    private final TIntObjectHashMap<AutoGroup> autoGroupByNpcId = new TIntObjectHashMap<>();

    void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        for (AutoGroup ag : autoGroup) {
            autoGroupByInstanceId.put(ag.getId(), ag);

            if (!ag.getNpcIds().isEmpty()) {
                for (int npcId : ag.getNpcIds()) {
                    autoGroupByNpcId.put(npcId, ag);
                }
            }
        }
        autoGroup = null;
    }

    public AutoGroup getTemplateByInstaceMaskId(byte maskId) {
        return autoGroupByInstanceId.get(maskId);
    }

    public int size() {
        return autoGroupByInstanceId.size();
    }
}
