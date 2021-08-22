/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.dataholders;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ne.gs.model.templates.pet.PetFlavour;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"flavours"})
@XmlRootElement(name = "pet_feed")
public class PetFeedData {

    @XmlElement(name = "flavour")
    protected List<PetFlavour> flavours;

    @XmlTransient
    private final Map<Integer, PetFlavour> petFlavoursById = new HashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        if (flavours == null) {
            return;
        }
        for (PetFlavour flavour : flavours) {
            petFlavoursById.put(flavour.getId(), flavour);
        }
        flavours = null;
    }

    public PetFlavour getFlavourById(int flavourId) {
        return petFlavoursById.get(flavourId);
    }

    public int size() {
        return petFlavoursById.size();
    }

    public PetFlavour[] getPetFlavours() {
        return petFlavoursById.values().toArray(new PetFlavour[0]);
    }
}
