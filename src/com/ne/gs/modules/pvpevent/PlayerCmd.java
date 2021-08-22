/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */

package com.ne.gs.modules.pvpevent;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author hex1r0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlayerCmd")
public class PlayerCmd {

    @XmlAttribute(name = "name", required = true)
    private String _name = "";

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

}
