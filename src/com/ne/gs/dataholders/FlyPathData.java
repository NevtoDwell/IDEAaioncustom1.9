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
import gnu.trove.map.hash.TShortObjectHashMap;

import com.ne.gs.model.templates.flypath.FlyPathEntry;

/**
 * @author KID
 */
@XmlRootElement(name = "flypath_template")
@XmlAccessorType(XmlAccessType.FIELD)
public class FlyPathData {

    @XmlElement(name = "flypath_location")
    private List<FlyPathEntry> list;

    private final TShortObjectHashMap<FlyPathEntry> loctlistData = new TShortObjectHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (FlyPathEntry loc : list) {
            loctlistData.put(loc.getId(), loc);
        }

        list = null;
    }

    public int size() {
        return loctlistData.size();
    }

    public FlyPathEntry getPathTemplate(byte i) {
        return loctlistData.get((short) i);
    }
}
