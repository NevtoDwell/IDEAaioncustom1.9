/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;

/**
 * @author dragoon112
 */
public class CM_REMOVE_ALTERED_STATE extends AionClientPacket {

    private int skillid;

    /*
     * (non-Javadoc)
     * @see com.ne.commons.network.packet.BaseClientPacket#readImpl()
     */
    @Override
    protected void readImpl() {
        skillid = readH();

    }

    /*
     * (non-Javadoc)
     * @see com.ne.commons.network.packet.BaseClientPacket#runImpl()
     */
    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        player.getEffectController().removeEffect(skillid);
    }

}
