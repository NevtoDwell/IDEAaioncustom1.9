/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.team.legion.Legion;
import com.ne.gs.model.team.legion.LegionEmblem;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.services.LegionService;

/**
 * @author cura
 */
public class CM_LEGION_SEND_EMBLEM_INFO extends AionClientPacket {

    private int legionId;

    @Override
    protected void readImpl() {
        legionId = readD();
    }

    @Override
    protected void runImpl() {
        getConnection().getActivePlayer();

        Legion legion = LegionService.getInstance().getLegion(legionId);
        if (legion != null) {
            LegionEmblem legionEmblem = legion.getLegionEmblem();
            if (legionEmblem.getCustomEmblemData() == null) {
                return;
            }
            LegionService.getInstance().sendEmblemData(getConnection().getActivePlayer(), legionEmblem, legionId, legion.getLegionName());
        }
    }
}
