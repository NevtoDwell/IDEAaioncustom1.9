/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.pet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author IlBuono
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "petfunction")
public class PetFunction {

    @XmlAttribute(name = "type")
    private PetFunctionType type;
    @XmlAttribute(name = "id")
    private int id;
    @XmlAttribute(name = "slots")
    private int slots;

    public PetFunctionType getPetFunctionType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public int getSlots() {
        return slots;
    }

    public static PetFunction CreateEmpty() {
        PetFunction result = new PetFunction();
        result.type = PetFunctionType.NONE;
        return result;
    }

}
