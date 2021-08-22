/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.dataholders.loadingutils.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.ne.gs.model.items.NpcEquippedGear;

/**
 * @author Luno
 */
public class NpcEquippedGearAdapter extends XmlAdapter<NpcEquipmentList, NpcEquippedGear> {

    /*
     * (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public NpcEquipmentList marshal(NpcEquippedGear v) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public NpcEquippedGear unmarshal(NpcEquipmentList v) throws Exception {
        return new NpcEquippedGear(v);
    }

}
