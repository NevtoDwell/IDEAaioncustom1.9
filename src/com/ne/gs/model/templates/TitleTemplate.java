/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.List;

import com.ne.gs.model.Race;
import com.ne.gs.model.stats.calc.StatOwner;
import com.ne.gs.model.stats.calc.functions.StatFunction;
import com.ne.gs.model.templates.stats.ModifiersTemplate;

/**
 * @author xavier
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "title_templates")
public class TitleTemplate implements StatOwner {

    @XmlAttribute(name = "id", required = true)
    @XmlID
    private String id;

    @XmlElement(name = "modifiers", required = false)
    protected ModifiersTemplate modifiers;

    @XmlAttribute(name = "race", required = true)
    private Race race;

    private int titleId;

    @XmlAttribute(name = "nameId")
    private int nameId;

    @XmlAttribute(name = "desc")
    private String description;

    public int getTitleId() {
        return titleId;
    }

    public Race getRace() {
        return race;
    }

    public int getNameId() {
        return nameId;
    }

    public String getDesc() {
        return description;
    }

    public List<StatFunction> getModifiers() {
        if (modifiers == null)
            return Collections.emptyList();

        return modifiers.getModifiers();
    }

    void afterUnmarshal(Unmarshaller u, Object parent) {
        this.titleId = Integer.parseInt(id);
    }
}
