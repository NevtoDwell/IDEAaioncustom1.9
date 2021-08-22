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
import java.util.ArrayList;
import java.util.List;
import gnu.trove.map.hash.THashMap;

import com.ne.gs.skillengine.model.MotionTime;


/**
 * @author kecimis
 */
@XmlRootElement(name = "motion_times")
@XmlAccessorType(XmlAccessType.FIELD)
public class MotionData {

    @XmlElement(name = "motion_time")
    protected List<MotionTime> motionTimes;

    @XmlTransient
    private final THashMap<String, MotionTime> motionTimesMap = new THashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (MotionTime motion : motionTimes) {
            motionTimesMap.put(motion.getName(), motion);
        }
    }

    /**
     * @return the motionTimeList
     */
    public List<MotionTime> getMotionTimes() {
        if (motionTimes == null) {
            motionTimes = new ArrayList<>();
        }

        return motionTimes;
    }

    public MotionTime getMotionTime(String name) {
        return motionTimesMap.get(name);
    }

    public int size() {
       if (motionTimes == null) {
            motionTimes = new ArrayList<>();
        }

        return motionTimes.size();
    }
}
