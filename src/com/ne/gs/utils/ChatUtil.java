/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils;

import com.ne.gs.configs.administration.AdminConfig;
import com.ne.gs.configs.main.MembershipConfig;
import com.ne.gs.configs.main.WeddingsConfig;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.world.WorldPosition;

/**
 * @author antness
 */
public final class ChatUtil {

    public static String position(String label, WorldPosition pos) {
        return position(label, pos.getMapId(), pos.getX(), pos.getY(), pos.getZ());
    }

    public static String position(String label, long worldId, float x, float y, float z) {
        // TODO: need rework for abyss map
        return String.format("[pos:%s;%d %f %f %f -1]", label, worldId, x, y, z);
    }

    public static String item(long itemId) {
        return String.format("[item: %d]", itemId);
    }

    public static String recipe(long recipeId) {
        return String.format("[recipe: %d]", recipeId);
    }

    public static String quest(int questId) {
        return String.format("[quest: %d]", questId);
    }

    public static String getRealAdminName(String name) {
        return name;
    }

}
