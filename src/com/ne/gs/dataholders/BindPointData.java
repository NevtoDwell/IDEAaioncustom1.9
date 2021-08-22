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

import com.ne.gs.model.templates.BindPointTemplate;

/**
 * @author avol
 */
@XmlRootElement(name = "bind_points")
@XmlAccessorType(XmlAccessType.FIELD)
public class BindPointData {

    @XmlElement(name = "bind_point")
    private List<BindPointTemplate> bplist;

    /**
     * A map containing all bind point location templates
     */
    private final TIntObjectHashMap<BindPointTemplate> bindplistData = new TIntObjectHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (BindPointTemplate bind : bplist) {
            bindplistData.put(bind.getNpcId(), bind);
        }

        bplist = null;
    }

    public int size() {
        return bindplistData.size();
    }

    public BindPointTemplate getBindPointTemplate(int npcId) {
        return bindplistData.get(npcId);
    }
}
