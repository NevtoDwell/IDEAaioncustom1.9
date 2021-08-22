/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.chathandlers;

import java.io.File;

import com.ne.gs.GameServerError;

/**
 * @author hex1r0
 */
public class AdminCommandRegistry extends AbstractChatCommandRegistry {

    @Override
    public void load() {
        try {
            getSm().load(new File("./data/scripts/system/adminhandlers.xml"));
        } catch (Exception e) {
            throw new GameServerError("Can't initialize admin chat handlers.", e);
        }
    }

    @Override
    public void postLoad(Class<?>[] classes) {
        super.postLoad(classes);

        _log.info("Loaded " + size() + " admin commands.");
    }
}
