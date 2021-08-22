/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.dataholders;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ne.gs.model.templates.road.RoadTemplate;

/**
 * @author SheppeR
 */
@XmlRootElement(name = "roads")
@XmlAccessorType(XmlAccessType.FIELD)
public class RoadData {

    @XmlElement(name = "road")
    private List<RoadTemplate> roadTemplates;

    public int size() {
        if (roadTemplates == null) {
            roadTemplates = new ArrayList<>();
            return 0;
        }
        return roadTemplates.size();
    }

    public List<RoadTemplate> getRoadTemplates() {
        if (roadTemplates == null) {
            return new ArrayList<>();
        }
        return roadTemplates;
    }

    public void addAll(Collection<RoadTemplate> templates) {
        if (roadTemplates == null) {
            roadTemplates = new ArrayList<>();
        }
        roadTemplates.addAll(templates);
    }
}
