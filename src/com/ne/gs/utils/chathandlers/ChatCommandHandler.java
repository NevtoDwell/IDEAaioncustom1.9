/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.chathandlers;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import gnu.trove.set.hash.THashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.func.Filter;
import com.ne.gs.configs.main.LoggingConfig;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author hex1r0
 */
public abstract class ChatCommandHandler implements Filter<String> {

    private static final Logger _log = LoggerFactory.getLogger(ChatCommandHandler.class);
    private static final Logger _auditLog = LoggerFactory.getLogger("ADMINAUDIT_LOG");

    private static volatile Set<ChatCommandHandler> _handlers = Collections.emptySet();

    public static void addHandler(ChatCommandHandler handler) {
        // copy on write
        synchronized (ChatCommandHandler.class) {
            Set<ChatCommandHandler> h = new THashSet<>(_handlers.size() + 1);
            h.addAll(_handlers);
            h.add(handler);

            _handlers = Collections.unmodifiableSet(h);
        }
    }

    public static void clearHandlers() {
        // copy on write
        synchronized (ChatCommandHandler.class) {
            _handlers = Collections.emptySet();
        }
    }

    public static boolean perform(Player player, String message) {
        // copy on write
      
        Set<ChatCommandHandler> hs = _handlers;
        for (ChatCommandHandler h : hs) {
            if (h.accept(message)) {
                String result = h.run(player, message) ? "succesfully used" : "failed to use";
                if (LoggingConfig.LOG_GMAUDIT && (h instanceof AdminCommandHandler)) {
                    _auditLog.info(String.format("Player: %s %s command: %s", player.getName(), result, message));
                }
                return true;
            }
        }

        return false;
    }

    // ------------------------------------------------------------------------
    private static final String[] EMPTY_ARRAY = new String[0];

    private final ChatCommandRegistry _registry;
   

    protected ChatCommandHandler(ChatCommandRegistry registry) {
        _registry = registry;

    }

    public ChatCommandRegistry getRegistry() {
        return _registry;
    }

    
    //    public void setRegistry(ChatCommandRegistry r) {
    //        _registry = r;
    //    }
    //
    //    public void setSecurity(ChatCommandSecurity s) {
    //        _security = s;
    //    }

    protected boolean run(Player player, String message) {
        String alias = parseAlias(message);
        String[] params = parseParams(message);

        ChatCommand cmd = getRegistry().getCommandByAlias(alias);

        if (cmd == null) {
            _log.warn("there is no `" + message + "` chat command!");
            return false;
        }

        
        String className = cmd.getClass().getSimpleName();

        return cmd.run(player, alias, params);
    }

    protected String[] parseParams(String message) {
        String[] tokens = message.split(" ");
        if (tokens.length > 1) {
            return Arrays.copyOfRange(tokens, 1, tokens.length);
        }

        return EMPTY_ARRAY;
    }

    protected String parseAlias(String message) {
        int space = message.indexOf(" ");
        if (space == -1) {
            space = message.length();
        }

        return message.substring(getPrefix().length(), space);
    }

    @Override
    public boolean accept(String message) {
        String[] tokens = message.split("(?!\\W)(?<=\\W)");
        return tokens.length != 0 && tokens[0].equals(getPrefix());
    }

    protected abstract String getPrefix();
}
