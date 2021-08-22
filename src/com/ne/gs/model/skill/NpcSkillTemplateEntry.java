/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.skill;

import com.ne.commons.utils.Rnd;
import com.ne.gs.model.templates.npcskill.NpcSkillTemplate;

class NpcSkillTemplateEntry extends NpcSkillEntry {

    private final NpcSkillTemplate template;

    public NpcSkillTemplateEntry(NpcSkillTemplate template) {
        super(template.getSkillid(), template.getSkillLevel());
        this.template = template;
    }

    @Override
    public boolean isReady(int hpPercentage, long fightingTimeInMSec) {
        if (hasCooldown() || !chanceReady()) {
            return false;
        }
        // TODO Need to confirm
        switch (template.getConjunctionType()) {
            case XOR:
                return hpReady(hpPercentage) && !timeReady(fightingTimeInMSec) || !hpReady(hpPercentage) && timeReady(fightingTimeInMSec);
            case OR:
                return hpReady(hpPercentage) || timeReady(fightingTimeInMSec);
            case AND:
                return hpReady(hpPercentage) && timeReady(fightingTimeInMSec);
        }
        return false;
    }

    @Override
    public boolean chanceReady() {
        return Rnd.chance(template.getProbability());
    }

    @Override
    public boolean hpReady(int hpPercentage) {
        if (template.getMaxhp() == 0 && template.getMinhp() == 0) {
            return true;
        }
        if (template.getMaxhp() >= hpPercentage && template.getMinhp() <= hpPercentage) {
            return true;
        }
        return false;
    }

    @Override
    public boolean timeReady(long fightingTimeInMSec) {
        if (template.getMaxTime() == 0 && template.getMinTime() == 0) {
            return true;
        }
        if (template.getMaxTime() >= fightingTimeInMSec && template.getMinTime() <= fightingTimeInMSec) {
            return true;
        }
        return false;
    }

    @Override
    public boolean hasCooldown() {
        return template.getCooldown() > System.currentTimeMillis() - lastTimeUsed;
    }
}
