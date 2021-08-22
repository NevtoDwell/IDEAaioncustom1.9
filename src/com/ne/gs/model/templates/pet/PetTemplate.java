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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

import com.ne.gs.model.templates.stats.PetStatsTemplate;

/**
 * @author IlBuono
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "pet")
public class PetTemplate {

    @XmlAttribute(name = "id", required = true)
    private int id;
    @XmlAttribute(name = "name", required = true)
    private String name;
    @XmlAttribute(name = "nameid", required = true)
    private int nameId;
    @XmlAttribute(name = "condition_reward")
    private int conditionReward;
    @XmlElement(name = "petfunction")
    private List<PetFunction> petFunctions;
    @XmlElement(name = "petstats")
    private PetStatsTemplate petStats;

    @XmlTransient
    Boolean hasPlayerFuncs = null;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getNameId() {
        return nameId;
    }

    public List<PetFunction> getPetFunctions() {
        if (hasPlayerFuncs == null) {
            hasPlayerFuncs = false;
            if (petFunctions == null) {
                List<PetFunction> result = new ArrayList<>();
                result.add(PetFunction.CreateEmpty());
                petFunctions = result;
            } else {
                for (PetFunction func : petFunctions) {
                    if (func.getPetFunctionType().isPlayerFunction()) {
                        hasPlayerFuncs = true;
                        break;
                    }
                }
                if (!hasPlayerFuncs.booleanValue()) {
                    petFunctions.add(PetFunction.CreateEmpty());
                }
            }
        }
        return petFunctions;
    }

    public PetFunction getWarehouseFunction() {
        if (petFunctions == null) {
            return null;
        }
        for (PetFunction pf : petFunctions) {
            if (pf.getPetFunctionType() == PetFunctionType.WAREHOUSE) {
                return pf;
            }
        }
        return null;
    }

    /**
     * Used to write to SM_PET packet, so checks only needed ones
     */
    public boolean ContainsFunction(PetFunctionType type) {
        if (type.getId() < 0) {
            return false;
        }

        for (PetFunction t : getPetFunctions()) {
            if (t.getPetFunctionType() == type) {
                return true;
            }
        }
        return false;
    }

    public PetFunction getPetFunction(PetFunctionType type) {
        for (PetFunction t : getPetFunctions()) {
            if (t.getPetFunctionType() == type) {
                return t;
            }
        }
        return null;
    }

    public PetStatsTemplate getPetStats() {
        return petStats;
    }

    public final int getConditionReward() {
        return conditionReward;
    }

}
