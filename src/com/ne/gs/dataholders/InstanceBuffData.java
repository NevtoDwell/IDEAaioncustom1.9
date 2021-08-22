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

import com.ne.gs.model.templates.instance_bonusatrr.InstanceBonusAttr;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"instanceBonusattr"})
@XmlRootElement(name = "instance_bonusattrs")
public class InstanceBuffData {

    @XmlElement(name = "instance_bonusattr")
    protected List<InstanceBonusAttr> instanceBonusattr;

    @XmlTransient
    private final TIntObjectHashMap<InstanceBonusAttr> templates = new TIntObjectHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (InstanceBonusAttr template : instanceBonusattr) {
            templates.put(template.getBuffId(), template);
        }
        instanceBonusattr = null;
    }

    public int size() {
        return templates.size();
    }

    public InstanceBonusAttr getInstanceBonusattr(int buffId) {
        return templates.get(buffId);
    }
}
