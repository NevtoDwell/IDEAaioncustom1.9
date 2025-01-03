/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.mail;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ne.gs.model.Race;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SysMail", propOrder = {"templates"})
public class SysMail {

    @XmlElement(name = "template", required = true)
    private List<MailTemplate> templates;

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlTransient
    private final Map<String, List<MailTemplate>> mailCaseTemplates = new HashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (MailTemplate template : templates) {
            String caseName = template.getName().toLowerCase();
            List<MailTemplate> sysTemplates = mailCaseTemplates.get(caseName);
            if (sysTemplates == null) {
                sysTemplates = new ArrayList<>();
                mailCaseTemplates.put(caseName, sysTemplates);
            }
            sysTemplates.add(template);
        }
        templates.clear();
        templates = null;
    }

    public MailTemplate getTemplate(String eventName, Race playerRace) {
        List<MailTemplate> sysTemplates = mailCaseTemplates.get(eventName.toLowerCase());
        if (sysTemplates == null) {
            return null;
        }
        for (MailTemplate template : sysTemplates) {
            if (template.getRace() == playerRace || template.getRace() == Race.PC_ALL) {
                return template;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
}
