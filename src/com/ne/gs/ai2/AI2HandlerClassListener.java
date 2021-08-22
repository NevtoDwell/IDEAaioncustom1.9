/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2;

import java.lang.reflect.Modifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.scripting.classlistener.ClassListener;
import com.ne.commons.utils.ClassUtils;

/**
 * @author ATracer
 */
public class AI2HandlerClassListener implements ClassListener {

    private static final Logger log = LoggerFactory.getLogger(AI2HandlerClassListener.class);

    @SuppressWarnings("unchecked")
    @Override
    public void postLoad(Class<?>[] classes) {
        for (Class<?> c : classes) {
            if (log.isDebugEnabled()) {
                log.debug("Load class " + c.getName());
            }

            if (!isValidClass(c)) {
                continue;
            }

            if (ClassUtils.isSubclass(c, AbstractAI.class)) {
                Class<? extends AbstractAI> tmp = (Class<? extends AbstractAI>) c;
                if (tmp != null) {
                    AI2Engine.getInstance().registerAI(tmp);
                }
            }
        }
    }

    @Override
    public void preUnload(Class<?>[] classes) {
        if (log.isDebugEnabled()) {
            for (Class<?> c : classes) {
                log.debug("Unload class " + c.getName());
            }
        }
    }

    public boolean isValidClass(Class<?> clazz) {
        int modifiers = clazz.getModifiers();

        if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)) {
            return false;
        }

        if (!Modifier.isPublic(modifiers)) {
            return false;
        }

        return true;
    }
}
