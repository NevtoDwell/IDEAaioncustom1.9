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
import javax.xml.bind.annotation.XmlType;
import java.util.List;

import com.ne.gs.model.drop.GlobalDrop;

/**
 * @author Kolobrodik
 */
@XmlRootElement(name = "global_drop")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "globalDropData", propOrder = {"globalDrop"})
public class GlobalDropData {

    @XmlElement(name = "drop_data")
    protected List<GlobalDrop> globalDrop;

    public List<GlobalDrop> getGlobalDrop() {
        return globalDrop;
    }

    public int size() {
        return globalDrop.size();
    }
}
