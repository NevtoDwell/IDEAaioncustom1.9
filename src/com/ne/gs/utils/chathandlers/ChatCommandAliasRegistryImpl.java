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
import java.util.Collections;
import java.util.Set;
import gnu.trove.set.hash.THashSet;

import com.ne.commons.utils.collections.AbstractRegistry;

/**
 * @author hex1r0
 */
public class ChatCommandAliasRegistryImpl extends AbstractRegistry<String, Collection<String>> implements ChatCommandAliasRegistry {

    @Override
    public Collection<String> getCommandAliases(String className) {
        Collection<String> c = get(className);
        if (c == null) {
            return Collections.emptyList();
        }

        return c;
    }

    @Override
    public void addAlias(String className, String alias) {
        Collection<String> c = get(className);
        if (c == null) {
            c = new THashSet<>(1);
            register(className, c);
        }

        c.add(alias);
    }

    @Override
    public Set<String> getAllClasses() {
        return getEntries(false).keySet();
    }
}
