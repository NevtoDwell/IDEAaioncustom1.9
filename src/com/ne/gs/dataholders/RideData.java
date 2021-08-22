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
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import gnu.trove.map.hash.TIntObjectHashMap;

import com.ne.gs.model.templates.ride.RideInfo;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"rides"})
@XmlRootElement(name = "rides")
public class RideData {

    @XmlElement(name = "ride_info")
    private List<RideInfo> rides;

    @XmlTransient
    private TIntObjectHashMap<RideInfo> rideInfos;

    void afterUnmarshal(Unmarshaller u, Object parent) {
        rideInfos = new TIntObjectHashMap<>();

        for (RideInfo info : rides) {
            rideInfos.put(info.getNpcId(), info);
        }
        rides = null;
    }

    public RideInfo getRideInfo(int npcId) {
        return rideInfos.get(npcId);
    }

    public int size() {
        return rideInfos.size();
    }
}
