/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.customrifts;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

import com.ne.commons.utils.xml.ElementList;
import com.ne.gs.modules.common.Pos;
import com.ne.gs.modules.common.PickPolicy;

/**
 * @author hex1r0
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "RiftPosList")
public class RiftPosList extends ElementList<RiftPos> {
    @XmlAttribute(name = "policy")
    protected PickPolicy _pickPolicy = PickPolicy.ALL;

    @XmlElement(name = "pos")
    public List<RiftPos> getPositions() {
        return getElements();
    }

    public PickPolicy getPickPolicy() {
        return _pickPolicy;
    }

    public void setPickPolicy(PickPolicy pickPolicy) {
        _pickPolicy = pickPolicy;
    }

}
