/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.dataholders.loadingutils;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rolandas
 */
public class XmlValidationHandler implements ValidationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(XmlValidationHandler.class);

    @Override
    public boolean handleEvent(ValidationEvent event) {
        if (event.getSeverity() == ValidationEvent.FATAL_ERROR || event.getSeverity() == ValidationEvent.ERROR) {
            ValidationEventLocator locator = event.getLocator();
            String message = event.getMessage();
            int line = locator.getLineNumber();
            int column = locator.getColumnNumber();
            log.error("Error at [line=" + line + ", column=" + column + "]: " + message);
            throw new Error(event.getLinkedException());
        }
        return true;
    }

}
