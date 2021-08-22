/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.world.exceptions;

/**
 * This exception will be thrown when object will be spawned more than one time (without despawning)
 *
 * @author -Nemesiss-
 */
@SuppressWarnings("serial")
public class AlreadySpawnedException extends RuntimeException {

    /**
     * Constructs an <code>AlreadySpawnedException</code> with no detail message.
     */
    public AlreadySpawnedException() {
        super();
    }

    /**
     * Constructs an <code>AlreadySpawnedException</code> with the specified detail message.
     *
     * @param s
     *     the detail message.
     */
    public AlreadySpawnedException(String s) {
        super(s);
    }

    /**
     * Creates new error
     *
     * @param message
     *     exception description
     * @param cause
     *     reason of this exception
     */
    public AlreadySpawnedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates new error
     *
     * @param cause
     *     reason of this exception
     */
    public AlreadySpawnedException(Throwable cause) {
        super(cause);
    }
}
