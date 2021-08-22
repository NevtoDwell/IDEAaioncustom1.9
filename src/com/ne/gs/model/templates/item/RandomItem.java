/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.Rnd;

/**
 * @author vlog
 */
@XmlType(name = "RandomItem")
public class RandomItem {

    @XmlAttribute(name = "type")
    protected RandomType type;
    @XmlAttribute(name = "count")
    protected int count;

    @XmlAttribute(name = "rnd_min")
    public int rndMin;

    @XmlAttribute(name = "rnd_max")
    public int rndMax;

    public int getCount() {
        return count;
    }

    public RandomType getType() {
        return type;
    }

    public int getRndMin() {
        return rndMin;
    }

    public int getRndMax() {
        return rndMax;
    }

    public final int getResultCount() {
        if ((count == 0) && (rndMin == 0) && (rndMax == 0)) {
            return 1;
        }
        if ((rndMin > 0) || (rndMax > 0)) {
            if (rndMax < rndMin) {
                LoggerFactory.getLogger(RandomItem.class).warn("Wrong rnd result item definition {} {}", rndMin, rndMax);
                return 1;
            }

            return Rnd.get(rndMin, rndMax);
        }

        return count;
    }
}
