/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.effect.modifier;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.skillengine.change.Func;
import com.ne.gs.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActionModifier")
public abstract class ActionModifier {

    @XmlAttribute
    protected int delta;
    @XmlAttribute(required = true)
    protected int value;

    @XmlAttribute
    protected Func mode = Func.ADD;

    /**
     * Applies modifier to original value
     *
     * @param effect
     * @return int
     */
    public abstract int analyze(Effect effect);

    /**
     * Performs check of condition
     *
     * @param effect
     *
     * @return true or false
     */
    public abstract boolean check(Effect effect);

    public Func getFunc() {
        return mode;
    }
}
