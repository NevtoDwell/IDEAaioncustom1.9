/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.customrates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

import com.ne.commons.utils.xml.ElementList;

/**
 * @author Jenelli
 * @date 26.04.13
 * @time 22:49
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "RateLoadList")
@XmlRootElement(name = "rate_config_loads")
public class RateLoadList extends ElementList<RateLoad> {
    @XmlElement(name = "rate_config")
    public List<RateLoad> getRates() {
        return getElements();
    }
}
