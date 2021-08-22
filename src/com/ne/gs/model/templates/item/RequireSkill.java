/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * <p/>
 * Java class for RequireSkill complex type.
 * <p/>
 * The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="RequireSkill">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="skillId" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="skilllvl" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequireSkill", propOrder = {"skillId"})
public class RequireSkill {

    @XmlElement(type = Integer.class)
    protected List<Integer> skillId;
    @XmlAttribute
    protected Integer skilllvl;

    /**
     * Gets the value of the skillId property.
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the skillId property.
     * <p/>
     * For example, to add a new item, do as follows:
     * <p/>
     * <pre>
     * getSkillId().add(newItem);
     * </pre>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link Integer }
     */
    public List<Integer> getSkillId() {
        if (skillId == null) {
            skillId = new ArrayList<>();
        }
        return this.skillId;
    }

    /**
     * Gets the value of the skilllvl property.
     *
     * @return possible object is {@link Integer }
     */
    public Integer getSkilllvl() {
        return skilllvl;
    }

}
