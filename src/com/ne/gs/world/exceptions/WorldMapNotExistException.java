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
 * This Exception will be thrown when some object is referencing to World map that do not exist. This Exception indicating serious error.
 *
 * @author -Nemesiss-
 */
@SuppressWarnings("serial")
public class WorldMapNotExistException extends RuntimeException {

    /**
     * Constructs an <code>WorldMapNotExistException</code> with no detail message.
     */
    public WorldMapNotExistException() {
        super();
    }

    /**
     * Constructs an <code>WorldMapNotExistException</code> with the specified detail message.
     *
     * @param s
     *     the detail message.
     */
    public WorldMapNotExistException(String s) {
        super(s);
    }
}
