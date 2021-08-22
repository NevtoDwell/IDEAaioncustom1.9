/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.mail;

public abstract interface IMailFormatter {

    public abstract MailPartType getType();

    public abstract String getFormattedString(MailPartType paramMailPartType);

    public abstract String getParamValue(String paramString);
}
