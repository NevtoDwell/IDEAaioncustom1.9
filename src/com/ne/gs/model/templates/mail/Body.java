/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.mail;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Body")
@XmlSeeAlso({MailPart.class})
public class Body extends MailPart {

    @XmlAttribute(name = "type")
    protected MailPartType type;

    @Override
    public MailPartType getType() {
        if (type == null) {
            return MailPartType.BODY;
        }
        return type;
    }

    @Override
    public String getParamValue(String name) {
        return "";
    }
}
