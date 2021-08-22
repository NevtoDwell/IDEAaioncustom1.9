/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.siege;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.DescId;
import com.ne.gs.model.templates.siegelocation.ArtifactActivation;
import com.ne.gs.model.templates.siegelocation.SiegeLocationTemplate;
import com.ne.gs.services.SiegeService;
import com.ne.gs.skillengine.model.SkillTemplate;

/**
 * @author Source
 */
public class ArtifactLocation extends SiegeLocation {

    private ArtifactStatus status;

    public ArtifactLocation() {
        status = ArtifactStatus.IDLE;
    }

    public ArtifactLocation(SiegeLocationTemplate template) {
        super(template);
        // Artifacts Always Vulnerable
        setVulnerable(true);
    }

    @Override
    public int getNextState() {
        return STATE_VULNERABLE;
    }

    public long getLastActivation() {
        return lastArtifactActivation;
    }

    public void setLastActivation(long paramLong) {
        lastArtifactActivation = paramLong;
    }

    public int getCoolDown() {
        long i = template.getActivation().getCd();
        long l = System.currentTimeMillis() - lastArtifactActivation;
        if (l > i) {
            return 0;
        } else {
            return (int) ((i - l) / 1000);
        }
    }

    /**
     * Returns DescriptionId that describes name of this artifact.<br>
     *
     * @return DescriptionId with name
     */
    public DescId getNameAsDescriptionId() {
        // Get Skill id, item, count and target defined for each artifact.
        ArtifactActivation activation = getTemplate().getActivation();
        int skillId = activation.getSkillId();
        SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(skillId);
        return DescId.of(skillTemplate.getNameId());
    }

    public boolean isStandAlone() {
        return !SiegeService.getInstance().getFortresses().containsKey(getLocationId());
    }

    public FortressLocation getOwningFortress() {
        return SiegeService.getInstance().getFortress(getLocationId());
    }

    /**
     * @return the status
     */
    public ArtifactStatus getStatus() {
        return status != null ? status : ArtifactStatus.IDLE;
    }

    /**
     * @param status
     *     the status to set
     */
    public void setStatus(ArtifactStatus status) {
        this.status = status;
    }
}
