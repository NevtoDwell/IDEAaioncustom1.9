/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

import com.ne.commons.utils.xml.ElementList;
import com.ne.gs.modules.pvpevent.PvpLocTemplate;

/**
 * @author hex1r0
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "CustomLocList")
@XmlRootElement(name = "custom_locs")
public class CustomLocList extends ElementList<CustomLocTemplate> {

    @XmlElements({
        @XmlElement(name = "custom_loc", type = CustomLocTemplate.class),
        @XmlElement(name = "pvp_loc", type = PvpLocTemplate.class)
    })
    public List<CustomLocTemplate> getLocs() {
        return getElements();
    }

}
