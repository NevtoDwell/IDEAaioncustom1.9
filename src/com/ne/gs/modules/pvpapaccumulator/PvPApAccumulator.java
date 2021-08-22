/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.pvpapaccumulator;

import java.sql.PreparedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.database.DB;
import com.ne.commons.utils.Actor;
import com.ne.commons.utils.ActorRef;
import com.ne.commons.utils.EventNotifier;
import com.ne.gs.configs.modules.PvPApAccumulatorConfig;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.services.PvpService;
import com.ne.gs.services.abyss.AbyssPointsService;

/**
 * @author hex1r0
 */
public final class PvPApAccumulator {
    private static final Logger _log = LoggerFactory.getLogger(PvPApAccumulator.class);

    private static final String QUERY =
        "INSERT INTO `pvp_ap_accumulator` (`player_id`, `ap`) VALUES(?, ?) " +
            "ON DUPLICATE KEY UPDATE `ap` = `ap` + ?";

    private static final ActorRef<?> _proc = ActorRef.of(new Actor());
    private static final AddAppListener _listener = new AddAppListener();

    public static void init() {
        if (PvPApAccumulatorConfig.PVP_AP_ACCUMULATOR_ENABLED) {
            _log.info("PvPApAccumulator: Enabled");
            EventNotifier.GLOBAL.attach(_listener);
        }
    }

    private static void save(int playerId, int ap) {
        PreparedStatement st = DB.prepareStatement(QUERY);
        try {
            st.setInt(1, playerId);
            st.setInt(2, ap);
            st.setInt(3, ap);
            st.executeUpdate();
        } catch (Throwable t) {
            _log.error("Unable to execute: " + QUERY, t);
        } finally {
            DB.close(st);
        }
    }

    private static class AddAppListener extends AbyssPointsService.ApAddCallback {
        @Override
        public void onApAdd(final Player player, VisibleObject vo, final int points, Class rewarder) {
            if (!PvPApAccumulatorConfig.PVP_AP_ACCUMULATOR_ENABLED) {
                return;
            }

            if (!PvpService.class.equals(rewarder)) {
                return;
            }

            // execute async in order to avoid player lag if database is busy
            _proc.tell(new Runnable() {
                @Override
                public void run() {
                    save(player.getObjectId(), points);
                }
            });
        }
    }
}
