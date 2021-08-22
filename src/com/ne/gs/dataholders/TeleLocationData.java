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

import com.ne.gs.model.templates.teleport.TelelocationTemplate;

/**
 * @author orz
 */
@XmlRootElement(name = "teleport_location")
@XmlAccessorType(XmlAccessType.FIELD)
public class TeleLocationData {

    @XmlElement(name = "teleloc_template")
    private List<TelelocationTemplate> tlist;

    /**
     * A map containing all teleport location templates
     */
    private final TIntObjectHashMap<TelelocationTemplate> loctlistData = new TIntObjectHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (TelelocationTemplate loc : tlist) {
            loctlistData.put(loc.getLocId(), loc);
        }

        tlist = null;
    }

    public int size() {
        return loctlistData.size();
    }

    public TelelocationTemplate getTelelocationTemplate(int id) {
        return loctlistData.get(id);
    }
}
