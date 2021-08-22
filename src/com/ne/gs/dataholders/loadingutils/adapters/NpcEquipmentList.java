/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.dataholders.loadingutils.adapters;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;

import com.ne.gs.model.templates.item.ItemTemplate;

/**
 * @author Luno
 */
public class NpcEquipmentList {

    @XmlElement(name = "item")
    @XmlIDREF
    public ItemTemplate[] items;

}
