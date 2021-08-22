/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.versionning.Version;

/**
 * @author hex1r0
 */
public final class CoreVersion {

    private static final Logger _log = LoggerFactory.getLogger(CoreVersion.class);
    public static final Version VERSION = Version.of(GameServer.class);

    public static String[] getBuildInfo() {
        return new String[]{
            String.format("%-6s [ %4s ] - %s - %s", VERSION.getVersion(), VERSION.getRevision(), VERSION.getJdk(), new Date(VERSION.getBuildTime()))
        };
    }

    public static void printBuildInfo() {
        for (String line : getBuildInfo()) {
            _log.info(line);
        }
    }

    private CoreVersion() {
    }
}
