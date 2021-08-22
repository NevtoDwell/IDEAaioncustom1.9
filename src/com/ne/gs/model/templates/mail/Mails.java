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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ne.gs.model.Race;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"sysMailTemplates"})
@XmlRootElement(name = "mails")
public class Mails {

    @XmlElement(name = "mail")
    private List<SysMail> sysMailTemplates;

    @XmlTransient
    private final Map<String, SysMail> sysMailByName = new HashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (SysMail template : sysMailTemplates) {
            String sysMailName = template.getName().toLowerCase();
            sysMailByName.put(sysMailName, template);
        }
        sysMailTemplates.clear();
        sysMailTemplates = null;
    }

    public MailTemplate getMailTemplate(String name, String eventName, Race playerRace) {
        SysMail template = sysMailByName.get(name.toLowerCase());
        if (template == null) {
            return null;
        }
        return template.getTemplate(eventName, playerRace);
    }

    public int size() {
        return sysMailByName.values().size();
    }
}
