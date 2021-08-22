/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.chathandlers;

import com.ne.commons.scripting.scriptmanager.ScriptManager;

/**
 * @author hex1r0
 */
public interface ChatCommandRegistry {

    ChatCommand getCommandByAlias(String alias);

    ChatCommandAliasRegistry getAliasRegistry();

    void setAliasRegistry(ChatCommandAliasRegistry aliasRegistry);

    ScriptManager getSm();

    void setSm(ScriptManager sm);

    void load();
}
