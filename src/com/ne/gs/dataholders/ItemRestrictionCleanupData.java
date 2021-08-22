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
import java.util.List;

import com.ne.gs.model.templates.restriction.ItemCleanupTemplate;

/**
 * @author KID
 */
@XmlRootElement(name = "item_restriction_cleanups")
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemRestrictionCleanupData {

    @XmlElement(name = "cleanup")
    private List<ItemCleanupTemplate> bplist;

    public int size() {
        return bplist.size();
    }

    public List<ItemCleanupTemplate> getList() {
        return this.bplist;
    }
}
