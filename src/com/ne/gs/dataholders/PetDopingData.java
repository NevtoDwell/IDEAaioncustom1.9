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
import java.util.List;
import gnu.trove.map.hash.TShortObjectHashMap;

import com.ne.gs.model.templates.pet.PetDopingEntry;

@XmlRootElement(name = "dopings")
@XmlAccessorType(XmlAccessType.FIELD)
public class PetDopingData {

    @XmlElement(name = "doping")
    private List<PetDopingEntry> list;

    @XmlTransient
    private final TShortObjectHashMap<PetDopingEntry> dopingsById = new TShortObjectHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (PetDopingEntry dope : list) {
            dopingsById.put(dope.getId(), dope);
        }
        list = null;
    }

    public int size() {
        return dopingsById.size();
    }

    public PetDopingEntry getDopingTemplate(short id) {
        return dopingsById.get(id);
    }
}
