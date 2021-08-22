/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.chathandlers;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Set;
import com.google.common.collect.Sets;
import gnu.trove.set.hash.THashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.scripting.classlistener.AggregatedClassListener;
import com.ne.commons.scripting.classlistener.ClassListener;
import com.ne.commons.scripting.classlistener.OnClassLoadUnloadListener;
import com.ne.commons.scripting.classlistener.ScheduledTaskClassListener;
import com.ne.commons.scripting.scriptmanager.ScriptManager;
import com.ne.commons.utils.ClassUtils;
import com.ne.commons.utils.collections.AbstractRegistry;

/**
 * @author hex1r0
 */
public abstract class AbstractChatCommandRegistry extends AbstractRegistry<String, ChatCommand> implements ChatCommandRegistry, ClassListener {

    protected static final Logger _log = LoggerFactory.getLogger(AbstractChatCommandRegistry.class);

    private ScriptManager _sm;
    private ChatCommandAliasRegistry _aliasRegistry;

    @Override
    public ChatCommand getCommandByAlias(String alias) {
        return get(alias);
    }

    @Override
    public ChatCommandAliasRegistry getAliasRegistry() {
        return _aliasRegistry;
    }

    @Override
    public void setAliasRegistry(ChatCommandAliasRegistry aliasRegistry) {
        _aliasRegistry = aliasRegistry;
    }

    @Override
    public ScriptManager getSm() {
        return _sm;
    }

    @Override
    public void setSm(ScriptManager sm) {
        _sm = sm;

        AggregatedClassListener acl = new AggregatedClassListener();
        acl.addClassListener(new OnClassLoadUnloadListener());
        acl.addClassListener(new ScheduledTaskClassListener());
        acl.addClassListener(this);

        _sm.setGlobalClassListener(acl);
    }

    @Override
    public void postLoad(Class<?>[] classes) {
        Set<String> configClassNames = _aliasRegistry.getAllClasses();
        Set<String> scriptClassNames = new THashSet<>();

        for (Class<?> c : classes) {
            if (!isValidClass(c)) {
                continue;
            }

            @SuppressWarnings("unchecked") Class<? extends ChatCommand> clazz = (Class<? extends ChatCommand>) c;
            if (clazz != null) {
                try {
                    ChatCommand cmd = clazz.newInstance();
                    String className = clazz.getSimpleName();
                    Collection<String> aliases = _aliasRegistry.getCommandAliases(className);

                    if (aliases.isEmpty()) {
                        _log.warn("Alias for " + className + " is not defined");
                        continue;
                    }

                    scriptClassNames.add(className);

                    registerAll(cmd, aliases);
                } catch (Exception e) {
                    _log.warn("", e);
                }
            }
        }

        // validate math between scripts and config
        for (String className : Sets.difference(configClassNames, scriptClassNames)) {
            _log.warn("Command script `" + className + "` does not exist!");
        }

        for (String className : Sets.difference(scriptClassNames, configClassNames)) {
            _log.warn("Command `" + className + "` is not defined in config!");
        }
    }

    private static boolean isValidClass(Class<?> clazz) {
        int modifiers = clazz.getModifiers();

        if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)) {
            return false;
        }

        if (!Modifier.isPublic(modifiers)) {
            return false;
        }

        if (!ClassUtils.isSubclass(clazz, ChatCommand.class)) {
            return false;
        }

        return true;
    }

    @Override
    public void preUnload(Class<?>[] classes) {
    }
}
