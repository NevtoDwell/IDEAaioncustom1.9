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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ne.gs.model.templates.shield.ShieldTemplate;

/**
 * @author Wakizashi
 */
@XmlRootElement(name = "shields")
@XmlAccessorType(XmlAccessType.FIELD)
public class ShieldData {

    @XmlElement(name = "shield")
    private List<ShieldTemplate> shieldTemplates;

    public int size() {
        if (shieldTemplates == null) {
            shieldTemplates = new ArrayList<>();
            return 0;
        }
        return shieldTemplates.size();
    }

    public List<ShieldTemplate> getShieldTemplates() {
        if (shieldTemplates == null) {
            return new ArrayList<>();
        }
        return shieldTemplates;
    }

    public void addAll(Collection<ShieldTemplate> templates) {
        if (shieldTemplates == null) {
            shieldTemplates = new ArrayList<>();
        }
        shieldTemplates.addAll(templates);
    }
}
