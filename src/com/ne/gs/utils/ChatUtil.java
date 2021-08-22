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

    public static final String HEART = "\ue020";
    public static final String VIP = "\ue023";
    public static final String PREMIUM = "\ue024";

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
        String[] tags = new String[]{
            AdminConfig.ADMIN_TAG_1,
            AdminConfig.ADMIN_TAG_2,
            AdminConfig.ADMIN_TAG_3,
            AdminConfig.ADMIN_TAG_4,
            AdminConfig.ADMIN_TAG_5
        };

        for (String tag : tags) {
            String t = tag.replaceAll("%s", "");
            if (name.contains(t))
                return name.replaceAll(tag.replaceAll("%s", ""), "");

        }

        return name;
    }

    public static String decorateName(Player player) {
        // orphaned players - later find/remove them
        AionConnection con = player.getClientConnection();
        if (con != null) {
            if (AdminConfig.ADMIN_TAG_ENABLE && player.isGM()) {
                return String.format(player.getCustomTag(false), player.getName());
            }

            String prefix = "";
            if (MembershipConfig.ENABLE_NAMEDECOR) {
                if (con.getAccount().getMembership() == 1) {
                    prefix = PREMIUM;
                } else if (con.getAccount().getMembership() == 2) {
                    prefix = VIP;
                }
            }

            if (WeddingsConfig.ENABLE_NAMEDECOR) {
                if (player.isMarried()) {
                    PlayerCommonData partner = PlayerCommonData.get(player.getPartnerId());
                    if (partner != null) {
                        return String.format("%s%s %s %s", prefix, player.getName(), HEART, partner.getName());
                    }
                }
            } else {
                return prefix + player.getName();
            }
        }

        return player.getName();
    }

    public static String undecorateName(String input) {
        if (MembershipConfig.ENABLE_NAMEDECOR) {
            input = input.replace(VIP, "");
            input = input.replace(PREMIUM, "");
        }

        if (WeddingsConfig.ENABLE_NAMEDECOR && input.contains(HEART)) {
            input = input.split(HEART)[0].trim();
        }

        return getRealAdminName(input);
    }
}
