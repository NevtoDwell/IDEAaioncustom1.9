/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.idfactory;

/**
 * This error is thrown by id factory
 *
 * @author SoulKeeper
 */
@SuppressWarnings("serial")
public class IDFactoryError extends Error {

    public IDFactoryError() {

    }

    public IDFactoryError(String message) {
        super(message);
    }

    public IDFactoryError(String message, Throwable cause) {
        super(message, cause);
    }

    public IDFactoryError(Throwable cause) {
        super(cause);
    }
}
