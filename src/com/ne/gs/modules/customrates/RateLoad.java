/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.customrates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.modules.common.Time;

/**
 * @author Jenelli
 * @date 26.04.13
 * @time 22:34
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RateLoad")
public class RateLoad {
    @XmlAttribute(name = "id", required = true)
    protected Integer _id;

    @XmlElement(name = "file", required = true)
    protected String _file;

    @XmlElement(name = "time")
    protected Time _time;

    public Integer getId() {
        return _id;
    }

    public void setId(Integer id) {
        _id = id;
    }

    public String getFile() {
        return _file;
    }

    public  Time getTime() {
        return _time;
    }
}
