/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers;


/**
 * @author Rolandas
 */
public enum HandlerResult {
    UNKNOWN,
    // allow other handlers to process
    SUCCESS,
    FAILED;

    public static HandlerResult fromBoolean(Boolean value) {
        if (value == null) {
            return HandlerResult.UNKNOWN;
        } else if (value) {
            return HandlerResult.SUCCESS;
        }
        return HandlerResult.FAILED;
    }
}
