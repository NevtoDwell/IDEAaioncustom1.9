/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.chathandlers;

import java.util.Collection;
import java.util.Set;

/**
 * @author hex1r0
 */
public interface ChatCommandAliasRegistry {

    Collection<String> getCommandAliases(String className);

    void addAlias(String className, String alias);

    Set<String> getAllClasses();
}
