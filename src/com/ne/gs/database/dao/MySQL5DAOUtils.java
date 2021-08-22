/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.dao;

public final class MySQL5DAOUtils {

    public static final String MYSQL_DB_NAME = "MySQL";

    public static boolean supports(String db, int majorVersion, int minorVersion) {
        return ("MySQL".equals(db)) && (majorVersion == 5);
    }
}
