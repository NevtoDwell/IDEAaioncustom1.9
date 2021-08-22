/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import com.ne.gs.model.templates.expand.Expand;

/**
 * @author Simple
 */
@XmlRootElement(name = "warehouse_npc")
@XmlAccessorType(XmlAccessType.FIELD)
public class WarehouseExpandTemplate {

    @XmlElement(name = "expand", required = true)
    protected List<Expand> warehouseExpands;

    /**
     * NPC ID
     */
    @XmlAttribute(name = "id", required = true)
    protected int id;

    /**
     * NPC name
     */
    @XmlAttribute(name = "name", required = true)
    protected String name = "";

    public int getNpcId() {
        return id;
    }

    /**
     * Gets the value of the material property.
     */
    public List<Expand> getWarehouseExpand() {
        return warehouseExpands;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Returns true if list contains level
     *
     * @return true or false
     */
    public boolean contains(int level) {
        for (Expand expand : warehouseExpands) {
            if (expand.getLevel() == level) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if list contains level
     *
     * @return expand
     */
    public Expand get(int level) {
        for (Expand expand : warehouseExpands) {
            if (expand.getLevel() == level) {
                return expand;
            }
        }
        return null;
    }
}
