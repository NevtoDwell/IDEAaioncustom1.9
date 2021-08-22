/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.siegeservice;

public class SiegeException extends RuntimeException {

    private static final long serialVersionUID = 8834569185793190327L;

    public SiegeException() {
    }

    public SiegeException(String message) {
        super(message);
    }

    public SiegeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SiegeException(Throwable cause) {
        super(cause);
    }
}
