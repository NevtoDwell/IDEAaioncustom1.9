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
 * This Exception will be thrown when some object is referencing to Instance that do not exist now.
 *
 * @author -Nemesiss-
 */
@SuppressWarnings("serial")
public class InstanceNotExistException extends RuntimeException {

    /**
     * Constructs an <code>InstanceNotExistException</code> with no detail message.
     */
    public InstanceNotExistException() {
        super();
    }

    /**
     * Constructs an <code>InstanceNotExistException</code> with the specified detail message.
     *
     * @param s
     *     the detail message.
     */
    public InstanceNotExistException(String s) {
        super(s);
    }
}
