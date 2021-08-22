/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.instance;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.scripting.classlistener.AggregatedClassListener;
import com.ne.commons.scripting.classlistener.OnClassLoadUnloadListener;
import com.ne.commons.scripting.classlistener.ScheduledTaskClassListener;
import com.ne.commons.scripting.scriptmanager.ScriptManager;
import com.ne.gs.GameServerError;
import com.ne.gs.instance.handlers.EventID;
import com.ne.gs.instance.handlers.GeneralInstanceHandler;
import com.ne.gs.instance.handlers.InstanceHandler;
import com.ne.gs.instance.handlers.InstanceID;
import com.ne.gs.model.GameEngine;
import com.ne.gs.world.WorldMapInstance;

/**
 * @author ATracer
 */
public class InstanceEngine implements GameEngine {

    private static final Logger log = LoggerFactory.getLogger(InstanceEngine.class);
    private static ScriptManager scriptManager = new ScriptManager();
    public static final File INSTANCE_DESCRIPTOR_FILE = new File("./data/scripts/system/instancehandlers.xml");
    public static final InstanceHandler DUMMY_INSTANCE_HANDLER = new GeneralInstanceHandler();
    
    private final Map<Integer, Class<? extends InstanceHandler>> eventHandlers = new HashMap<>();
    private final Map<Integer, Class<? extends InstanceHandler>> handlers = new HashMap<>();

    @Override
    public void load(CountDownLatch progressLatch) {
        log.info("Instance engine load started");
        scriptManager = new ScriptManager();

        AggregatedClassListener acl = new AggregatedClassListener();
        acl.addClassListener(new OnClassLoadUnloadListener());
        acl.addClassListener(new ScheduledTaskClassListener());
        acl.addClassListener(new InstanceHandlerClassListener());
        scriptManager.setGlobalClassListener(acl);

        try {
            scriptManager.load(INSTANCE_DESCRIPTOR_FILE);
            log.info("Loaded " + handlers.size() + " instance handlers.");
        } catch (Exception e) {
            throw new GameServerError("Can't initialize instance handlers.", e);
        } finally {
            if (progressLatch != null) {
                progressLatch.countDown();
            }
        }
    }

    @Override
    public void shutdown() {
        log.info("Instance engine shutdown started");
        scriptManager.shutdown();
        scriptManager = null;
        eventHandlers.clear();
        handlers.clear();
        log.info("Instance engine shutdown complete");
    }

    public InstanceHandler getNewInstanceHandler(int worldId) {
        Class<? extends InstanceHandler> instanceClass = handlers.get(worldId);
        InstanceHandler instanceHandler = null;
        if (instanceClass != null) {
            try {
                instanceHandler = instanceClass.newInstance();
            } catch (Exception ex) {
                log.warn("Can't instantiate instance handler " + worldId, ex);
            }
        }
        if (instanceHandler == null) {
            instanceHandler = DUMMY_INSTANCE_HANDLER;
        }
        return instanceHandler;
    }

    // new
    public InstanceHandler getNewEventInstanceHandler(int handlerId) {
        Class<? extends InstanceHandler> instanceClass = this.eventHandlers.get(handlerId);
        InstanceHandler instanceHandler = null;
        if (instanceClass != null) {
            try {
                instanceHandler = instanceClass.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                log.warn("Can't instantiate event instance handler " + handlerId, ex);
            }
        }
        if (instanceHandler == null) {
            instanceHandler = DUMMY_INSTANCE_HANDLER;
        }
        return instanceHandler;
    }
    
    /**
     * @param handler
     */
    final void addInstanceHandlerClass(Class<? extends InstanceHandler> handler) {
        EventID eventAnnotation = handler.getAnnotation(EventID.class);
        if (eventAnnotation != null) {
            eventHandlers.put(eventAnnotation.eventId(), handler);
            return;
        }
        InstanceID idAnnotation = handler.getAnnotation(InstanceID.class);
        if (idAnnotation != null) {
            handlers.put(idAnnotation.value(), handler);
        }
    }

    /**
     * @param instance
     */
    public void onInstanceCreate(WorldMapInstance instance) {
        instance.getInstanceHandler().onInstanceCreate(instance);
    }

    public static InstanceEngine getInstance() {
        return SingletonHolder.instance;
    }

    @SuppressWarnings("synthetic-access")
    private static final class SingletonHolder {

        protected static final InstanceEngine instance = new InstanceEngine();
    }
}
