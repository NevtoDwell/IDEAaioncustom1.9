/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

import com.ne.commons.utils.xml.ElementList;

/**
 * @author hex1r0
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "PosList")
public class PosList extends ElementList<Pos> {
    @XmlAttribute(name = "policy")
    protected PickPolicy _pickPolicy = PickPolicy.ALL;

    @XmlElement(name = "pos")
    public List<Pos> getPositions() {
        return getElements();
    }

    public PickPolicy getPickPolicy() {
        return _pickPolicy;
    }

    public void setPickPolicy(PickPolicy pickPolicy) {
        _pickPolicy = pickPolicy;
    }

}
