/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.spawns.siegespawns;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

import com.ne.gs.model.siege.SiegeModType;
import com.ne.gs.model.siege.SiegeRace;
import com.ne.gs.model.templates.spawns.Spawn;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SiegeSpawn")
public class SiegeSpawn {

    @XmlElement(name = "siege_race")
    private List<SiegeRaceTemplate> siegeRaceTemplates;
    @XmlAttribute(name = "siege_id")
    private int siegeId;

    public int getSiegeId() {
        return siegeId;
    }

    public List<SiegeRaceTemplate> getSiegeRaceTemplates() {
        return siegeRaceTemplates;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "SiegeRaceTemplate")
    public static class SiegeRaceTemplate {

        @XmlElement(name = "siege_mod")
        private List<SiegeModTemplate> SiegeModTemplates;
        @XmlAttribute(name = "race")
        private SiegeRace race;

        public SiegeRace getSiegeRace() {
            return race;
        }

        public List<SiegeModTemplate> getSiegeModTemplates() {
            return SiegeModTemplates;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "SiegeModTemplate")
        public static class SiegeModTemplate {

            @XmlElement(name = "spawn")
            private List<Spawn> spawns;
            @XmlAttribute(name = "mod")
            private SiegeModType siegeMod;

            public List<Spawn> getSpawns() {
                return spawns;
            }

            public SiegeModType getSiegeModType() {
                return siegeMod;
            }
        }
    }
}
