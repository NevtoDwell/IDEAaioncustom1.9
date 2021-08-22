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
import java.util.List;
import java.util.Map;
import gnu.trove.map.hash.THashMap;

import com.ne.gs.model.siege.ArtifactLocation;
import com.ne.gs.model.siege.FortressLocation;
import com.ne.gs.model.siege.OutpostLocation;
import com.ne.gs.model.siege.SiegeLocation;
import com.ne.gs.model.siege.SourceLocation;
import com.ne.gs.model.templates.siegelocation.SiegeLocationTemplate;

/**
 * @author Sarynth, antness
 */
@XmlRootElement(name = "siege_locations")
@XmlAccessorType(XmlAccessType.FIELD)
public class SiegeLocationData {

    @XmlElement(name = "siege_location")
    private List<SiegeLocationTemplate> siegeLocationTemplates;
    /**
     * Map that contains skillId - SkillTemplate key-value pair
     */
    @XmlTransient
    private final Map<Integer, ArtifactLocation> artifactLocations = new THashMap<>();
    @XmlTransient
    private final Map<Integer, FortressLocation> fortressLocations = new THashMap<>();
    @XmlTransient
    private final Map<Integer, OutpostLocation> outpostLocations = new THashMap<>();
    @XmlTransient
    private final Map<Integer, SourceLocation> sourceLocations = new THashMap<>();
    @XmlTransient
    private final Map<Integer, SiegeLocation> siegeLocations = new THashMap<>();


    void afterUnmarshal(Unmarshaller u, Object parent) {
        artifactLocations.clear();
        fortressLocations.clear();
        outpostLocations.clear();
        sourceLocations.clear();
        siegeLocations.clear();
        for (SiegeLocationTemplate template : siegeLocationTemplates) {
            switch (template.getType()) {
                case FORTRESS:
                    FortressLocation fortress = new FortressLocation(template);
                    fortressLocations.put(template.getId(), fortress);
                    siegeLocations.put(template.getId(), fortress);
                    artifactLocations.put(template.getId(), new ArtifactLocation(template));
                    break;
                case ARTIFACT:
                    ArtifactLocation artifact = new ArtifactLocation(template);
                    artifactLocations.put(template.getId(), artifact);
                    siegeLocations.put(template.getId(), artifact);
                    break;
                case BOSSRAID_LIGHT:
                case BOSSRAID_DARK:
                    OutpostLocation protector = new OutpostLocation(template);
                    outpostLocations.put(template.getId(), protector);
                    siegeLocations.put(template.getId(), protector);
                    break;
                case SOURCE:
                    SourceLocation source = new SourceLocation(template);
                    sourceLocations.put(template.getId(), source);
                    siegeLocations.put(template.getId(), source);
                    break;
                default:
                    break;
            }
        }
        siegeLocationTemplates = null;
    }

    public int size() {
        return siegeLocations.size();
    }

    public Map<Integer, ArtifactLocation> getArtifacts() {
        return artifactLocations;
    }

    public Map<Integer, FortressLocation> getFortress() {
        return fortressLocations;
    }

    public Map<Integer, OutpostLocation> getOutpost() {
        return outpostLocations;
    }

    public Map<Integer, SourceLocation> getSource() {
        return sourceLocations;
    }

    public Map<Integer, SiegeLocation> getSiegeLocations() {
        return siegeLocations;
    }
}
