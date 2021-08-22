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
import com.ne.gs.model.team.legion.LegionEmblemType;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_LEGION_SEND_EMBLEM;
import com.ne.gs.services.LegionService;

/**
 * @author Simple
 * @modified cura
 */
public class CM_LEGION_SEND_EMBLEM extends AionClientPacket {

    private int legionId;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        legionId = readD();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Legion legion = LegionService.getInstance().getLegion(legionId);

        if (legion != null) {
            LegionEmblem legionEmblem = legion.getLegionEmblem();
            if (legionEmblem.getEmblemType() == LegionEmblemType.DEFAULT) {
                sendPacket(new SM_LEGION_SEND_EMBLEM(legionId, legionEmblem.getEmblemId(), legionEmblem.getColor_r(), legionEmblem.getColor_g(),
                    legionEmblem.getColor_b(), legion.getLegionName(), legionEmblem.getEmblemType(), 0));
            } else {
                sendPacket(new SM_LEGION_SEND_EMBLEM(legionId, legionEmblem.getEmblemId(), legionEmblem.getColor_r(), legionEmblem.getColor_g(),
                    legionEmblem.getColor_b(), legion.getLegionName(), legionEmblem.getEmblemType(), legionEmblem.getCustomEmblemData().length));
            }
        }
    }
}
