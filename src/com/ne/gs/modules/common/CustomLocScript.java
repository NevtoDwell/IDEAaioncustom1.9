/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.common;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import com.ne.gs.configs.main.LoggingConfig;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.Sys;
import com.ne.commons.utils.Actor;
import com.ne.commons.utils.ActorRef;
import com.ne.gs.instance.handlers.GeneralInstanceHandler;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author hex1r0
 */
public class CustomLocScript<T extends CustomLocTemplate> extends GeneralInstanceHandler {

    /*private static final Logger _killLog = LoggerFactory.getLogger("KILL_LOG");*/
    
    protected static final Logger _log = LoggerFactory.getLogger(CustomLocScript.class);

    protected final ActorRef<?> _processor = ActorRef.of(new Actor());
    protected final T _template;
    protected final Date _expiresAt;


    public CustomLocScript(T template, Date expiresAt) {
        _template = template;
        _expiresAt = expiresAt;
    }

    public long remainingTimeMs() {
        return Math.max(0, _expiresAt.getTime() - Sys.millis());
    }

    public T getTemplate() {
        return _template;
    }

    public Date getExpiresAt() {
        return _expiresAt;
    }

    public ActorRef<?> getProcessor() {
        return _processor;
    }

    public ScheduledFuture<?> schedule(final Runnable task, long delayMs) {
        return ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                getProcessor().tell(task);
            }
        }, delayMs);
    }

    /**
     * Triggered when location is ready for action (to accept players, etc)
     */
    public void onReady() { }

    /**
     * Triggered when script receives message from outside, teleporters, etc
     */
    public void onRecv(String messageId, Object[] args) { }

    @Override
    public boolean onDie(Player player, Creature lastAttacker) {

        /*if (LoggingConfig.LOG_KILL && lastAttacker instanceof Player) {
            _killLog.info("[KILL-{}] Player [{}] killed [{}]",
                    getClass().getSimpleName(),
                    lastAttacker.getName(),
                    player.getName());
        }*/

        return super.onDie(player, lastAttacker);
    }

    public boolean canEnter(Player player) {
        return true;
    }
}
