/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.guide;

/**
 * @author xTz
 */
public class Guide {

    private final int guide_id;
    private final int player_id;
    private final String title;

    public Guide(int guide_id, int player_id, String title) {
        this.guide_id = guide_id;
        this.player_id = player_id;
        this.title = title;
    }

    public int getGuideId() {
        return guide_id;
    }

    public int getPlayerId() {
        return player_id;
    }

    public String getTitle() {
        return title;
    }
}
