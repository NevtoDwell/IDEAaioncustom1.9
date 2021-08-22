/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.properties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

import com.mw.TempConst;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.skillengine.model.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Properties")
public class Properties {

    private static final Logger Log = LoggerFactory.getLogger(Properties.class);

    @XmlAttribute(name = "first_target", required = true)
    protected FirstTargetAttribute firstTarget;

    @XmlAttribute(name = "first_target_range", required = true)
    protected int firstTargetRange;

    @XmlAttribute(name = "awr")
    protected boolean addWeaponRange;

    @XmlAttribute(name = "target_relation", required = true)
    protected TargetRelationAttribute targetRelation;

    @XmlAttribute(name = "target_type", required = true)
    protected TargetRangeAttribute targetType;

    @XmlAttribute(name = "target_distance")
    protected int targetDistance;

    @XmlAttribute(name = "target_maxcount")
    protected int targetMaxCount;

    @XmlAttribute(name = "target_status")
    private List<String> targetStatus;

    @XmlAttribute(name = "revision_distance")
    protected int revisionDistance;

    @XmlAttribute(name = "effective_width")
    private int effectiveWidth;

    @XmlAttribute(name = "effective_angle")
    private int effectiveAngle;

    @XmlAttribute(name = "direction")
    protected int direction;

    @XmlAttribute(name = "target_species")
    protected TargetSpeciesAttribute targetSpecies = TargetSpeciesAttribute.ALL;

    /**
     * @param skill
     */
    public boolean validate(Skill skill) {
        if (firstTarget != null) {
            if (!FirstTargetProperty.set(skill, this)) {

                if(skill.getSkillId() == TempConst.SKILL_ID_REMOVE_SHOCK)
                    Log.info("REMOVE_SHOCK FirstTargetProperty wasn't passed");

                return false;
            }
        }
        if (firstTargetRange != 0 || addWeaponRange) {
            if (!FirstTargetRangeProperty.set(skill, this, CastState.CAST_START)) {

                if(skill.getSkillId() == TempConst.SKILL_ID_REMOVE_SHOCK)
                    Log.info("REMOVE_SHOCK FirstTargetRangeProperty wasn't passed");

                return false;
            }
        }
        if (targetType != null) {
            if (!TargetRangeProperty.set(skill, this)) {

                if(skill.getSkillId() == TempConst.SKILL_ID_REMOVE_SHOCK)
                    Log.info("REMOVE_SHOCK TargetRangeProperty wasn't passed");

                return false;
            }
        }
        if (targetRelation != null) {
            if (!TargetRelationProperty.set(skill, this)) {

                if(skill.getSkillId() == TempConst.SKILL_ID_REMOVE_SHOCK)
                    Log.info("REMOVE_SHOCK TargetRelationProperty wasn't passed");

                return false;
            }
        }
        if (targetStatus != null) {
            //TODO sometimes abnormals check fails!
            if (!TargetStatusProperty.set(skill, this) && skill.getSkillId() != TempConst.SKILL_ID_REMOVE_SHOCK) {
                return false;
            }
        }
        if (targetSpecies != TargetSpeciesAttribute.ALL) {
            if (!TargetSpeciesProperty.set(skill, this)) {

                if(skill.getSkillId() == TempConst.SKILL_ID_REMOVE_SHOCK)
                    Log.info("REMOVE_SHOCK TargetSpeciesProperty wasn't passed");

                return false;
            }
        }
        if (targetType != null) {
            if (!MaxCountProperty.set(skill, this)) {

                if(skill.getSkillId() == TempConst.SKILL_ID_REMOVE_SHOCK)
                    Log.info("REMOVE_SHOCK MaxCountProperty wasn't passed");

                return false;
            }
        }
        return true;
    }

    /**
     * @param skill
     */
    public boolean endCastValidate(Skill skill) {
        Creature firstTarget = skill.getFirstTarget();
        skill.getEffectedList().clear();
        skill.getEffectedList().add(firstTarget);

        if (firstTargetRange != 0) {
            if (!FirstTargetRangeProperty.set(skill, this, CastState.CAST_END)) {
                return false;
            }
        }
        if (targetType != null) {
            if (!TargetRangeProperty.set(skill, this)) {
                return false;
            }
        }
        if (targetRelation != null) {
            if (!TargetRelationProperty.set(skill, this)) {
                return false;
            }
        }
        if (targetType != null) {
            if (!MaxCountProperty.set(skill, this)) {
                return false;
            }
        }
        if (targetStatus != null) {
            if (!TargetStatusProperty.set(skill, this)) {
                return false;
            }
        }
        if (targetSpecies != TargetSpeciesAttribute.ALL) {
            if (!TargetSpeciesProperty.set(skill, this)) {
                return false;
            }
        }
        return true;
    }

    public FirstTargetAttribute getFirstTarget() {
        return firstTarget;
    }

    public int getFirstTargetRange() {
        return firstTargetRange;
    }

    public boolean isAddWeaponRange() {
        return addWeaponRange;
    }

    public TargetRelationAttribute getTargetRelation() {
        return targetRelation;
    }

    public TargetRangeAttribute getTargetType() {
        return targetType;
    }

    public int getTargetDistance() {
        return targetDistance;
    }

    public int getTargetMaxCount() {
        return targetMaxCount;
    }

    public List<String> getTargetStatus() {
        return targetStatus;
    }

    public int getRevisionDistance() {
        return revisionDistance;
    }

    public int getEffectiveWidth() {
        return effectiveWidth;
    }

    public int getEffectiveAngle() {
        return effectiveAngle;
    }

    public boolean isBackDirection() {
        return direction == 1;
    }

    public TargetSpeciesAttribute getTargetSpecies() {
        return targetSpecies;
    }

    public enum CastState {
        CAST_START(true),
        CAST_END(false);

        private final boolean isCastStart;

        CastState(boolean isCastStart) {
            this.isCastStart = isCastStart;
        }

        public boolean isCastStart() {
            return isCastStart;
        }
    }
}
