/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.mail;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "MailPartType")
@XmlEnum
public enum MailPartType {
    CUSTOM,
    SENDER,
    TITLE,
    HEADER,
    BODY,
    TAIL;

    public String value() {
        return name();
    }

    public static MailPartType fromValue(String v) {
        return valueOf(v);
    }
}
