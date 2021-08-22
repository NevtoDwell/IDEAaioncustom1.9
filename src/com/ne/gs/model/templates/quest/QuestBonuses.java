/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.quest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.ne.gs.model.templates.rewards.BonusType;

/**
 * @author Rolandas
 *
 */

/**
 * <p/>
 * Java class for QuestBonuses complex type.
 * <p/>
 * The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="QuestBonuses">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="type" use="required" type="{}BonusType" />
 *       &lt;attribute name="level" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="skill" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestBonuses")
public class QuestBonuses {

    @XmlAttribute(required = true)
    protected BonusType type;
    @XmlAttribute
    protected int level;
    @XmlAttribute
    protected int skill;

    /**
     * Gets the value of the type property.
     *
     * @return possible object is {@link BonusType }
     */
    public BonusType getType() {
        return type;
    }

    /**
     * Gets the value of the level property.
     *
     * @return possible object is {@link Integer }
     */
    public int getLevel() {
        return level;
    }

    /**
     * Gets the value of the skill property.
     *
     * @return possible object is {@link Integer }
     */
    public int getSkill() {
        return skill;
    }
}
