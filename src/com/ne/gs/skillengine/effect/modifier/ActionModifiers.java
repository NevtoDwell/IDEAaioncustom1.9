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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActionModifiers")
public class ActionModifiers {

    @XmlElements({
        @XmlElement(name = "frontdamage", type = FrontDamageModifier.class),
        @XmlElement(name = "backdamage", type = BackDamageModifier.class),
        @XmlElement(name = "abnormaldamage", type = AbnormalDamageModifier.class),
        @XmlElement(name = "targetrace", type = TargetRaceDamageModifier.class)})
    protected List<ActionModifier> actionModifiers;

    public List<ActionModifier> getActionModifiers() {
        if (actionModifiers == null) {
            actionModifiers = new ArrayList<>();
        }
        return this.actionModifiers;
    }
}
