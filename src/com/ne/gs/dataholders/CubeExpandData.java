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

import com.ne.gs.model.templates.CubeExpandTemplate;

/**
 * This is for the Cube Expanders.
 *
 * @author dragoon112
 */
@XmlRootElement(name = "cube_expander")
@XmlAccessorType(XmlAccessType.FIELD)
public class CubeExpandData {

    @XmlElement(name = "cube_npc")
    private List<CubeExpandTemplate> clist;
    private final TIntObjectHashMap<CubeExpandTemplate> npctlistData = new TIntObjectHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (CubeExpandTemplate npc : clist) {
            npctlistData.put(npc.getNpcId(), npc);
        }

        clist = null;
    }

    public int size() {
        return npctlistData.size();
    }

    public CubeExpandTemplate getCubeExpandListTemplate(int id) {
        return npctlistData.get(id);
    }
}
