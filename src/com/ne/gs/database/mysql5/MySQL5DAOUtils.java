/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.mysql5;

/**
 * @author SoulKeeper
 */
public final class MySQL5DAOUtils {

    /**
     * Constant for MySQL name ;)
     */
    public static final String MYSQL_DB_NAME = "MySQL";

    /**
     * Returns true only if GDB supports MySQL5
     *
     * @param db
     *     database name
     * @param majorVersion
     *     major version
     * @param minorVersion
     *     minor version, ignored
     *
     * @return supports or not
     */
    public static boolean supports(String db, int majorVersion, int minorVersion) {
        return MYSQL_DB_NAME.equals(db) && majorVersion == 5;
    }
}
