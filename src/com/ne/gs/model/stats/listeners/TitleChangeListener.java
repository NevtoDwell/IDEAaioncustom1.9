/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.stats.listeners;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.stats.container.CreatureGameStats;
import com.ne.gs.model.templates.TitleTemplate;

/**
 * @author xavier
 */
public final class TitleChangeListener {

    public static void onTitleChange(CreatureGameStats<?> cgs, int titleId, boolean isSet) {
        TitleTemplate tt = DataManager.TITLE_DATA.getTitleTemplate(titleId);
        if (tt == null) {
            return;
        }
        if (!isSet) {
            cgs.endEffect(tt);
        } else {
            cgs.addEffect(tt, tt.getModifiers());
        }
    }
}
