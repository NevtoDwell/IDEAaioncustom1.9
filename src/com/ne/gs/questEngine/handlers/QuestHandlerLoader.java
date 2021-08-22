/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers;

import java.lang.reflect.Modifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.scripting.classlistener.ClassListener;
import com.ne.commons.utils.ClassUtils;
import com.ne.gs.questEngine.QuestEngine;

/**
 * @author MrPoke
 */
public class QuestHandlerLoader implements ClassListener {

    private static final Logger logger = LoggerFactory.getLogger(QuestHandlerLoader.class);

    public QuestHandlerLoader() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void postLoad(Class<?>[] classes) {
        for (Class<?> c : classes) {
            if (logger.isDebugEnabled()) {
                logger.debug("Load class " + c.getName());
            }

            if (!isValidClass(c)) {
                continue;
            }

            if (ClassUtils.isSubclass(c, QuestHandler.class)) {
                try {
                    Class<? extends QuestHandler> tmp = (Class<? extends QuestHandler>) c;
                    if (tmp != null) {
                        QuestEngine.getInstance().addQuestHandler(tmp.newInstance());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to load quest handler class: " + c.getName(), e);
                }
            }
        }
    }

    @Override
    public void preUnload(Class<?>[] classes) {
        if (logger.isDebugEnabled()) {
            for (Class<?> c : classes) {
                // debug messages
                logger.debug("Unload class " + c.getName());
            }
        }

        QuestEngine.getInstance().clear();
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
