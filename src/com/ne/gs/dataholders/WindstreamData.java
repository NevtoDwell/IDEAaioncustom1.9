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
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import gnu.trove.map.hash.TIntObjectHashMap;

import com.ne.gs.model.templates.windstreams.WindstreamTemplate;

/**
 * @author LokiReborn
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "windstreams")
public class WindstreamData {

    @XmlElement(name = "windstream")
    private List<WindstreamTemplate> wts;

    private TIntObjectHashMap<WindstreamTemplate> windstreams;

    void afterUnmarshal(Unmarshaller u, Object parent) {
        windstreams = new TIntObjectHashMap<>();
        for (WindstreamTemplate wt : wts) {
            windstreams.put(wt.getMapid(), wt);
        }

        wts = null;
    }

    public WindstreamTemplate getStreamTemplate(int mapId) {
        return windstreams.get(mapId);
    }

    /**
     * @return items.size()
     */
    public int size() {
        return windstreams.size();
    }
}

