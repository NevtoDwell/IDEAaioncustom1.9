/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.GameServer;
import com.ne.gs.configs.main.GSConfig;
import com.ne.gs.configs.main.MembershipConfig;
import com.ne.gs.configs.network.IPConfig;
import com.ne.gs.configs.network.NetworkConfig;
import com.ne.gs.model.Race;
import com.ne.gs.network.NetworkController;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.services.ChatService;
import com.ne.gs.services.EventService;

/**
 * @author -Nemesiss- CC fix
 */
public class SM_VERSION_CHECK extends AionServerPacket {

    private final int version;
    private int characterLimitCount;

    /**
     * Related to the character creation mode
     */
    private final int characterFactionsMode;
    private final int characterCreateMode;

    public SM_VERSION_CHECK(int version) {
        this.version = version;
        if (MembershipConfig.CHARACTER_ADDITIONAL_ENABLE != 10 && MembershipConfig.CHARACTER_ADDITIONAL_COUNT > GSConfig.CHARACTER_LIMIT_COUNT) {
            characterLimitCount = MembershipConfig.CHARACTER_ADDITIONAL_COUNT;
        } else {
            characterLimitCount = GSConfig.CHARACTER_LIMIT_COUNT;
        }

        characterLimitCount *= NetworkController.getInstance().getServerCount();

        if (GSConfig.CHARACTER_CREATION_MODE < 0 || GSConfig.CHARACTER_CREATION_MODE > 2) {
            characterFactionsMode = 0;
        } else {
            characterFactionsMode = GSConfig.CHARACTER_CREATION_MODE;
        }

        if (GSConfig.CHARACTER_FACTION_LIMITATION_MODE < 0 || GSConfig.CHARACTER_FACTION_LIMITATION_MODE > 3) {
            characterCreateMode = 0;
        } else {
            characterCreateMode = GSConfig.CHARACTER_FACTION_LIMITATION_MODE * 0x04;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        if (version < 0xC2) {
            writeC(0x02);
            return;
        }
        writeC(0x00);
        writeC(NetworkConfig.GAMESERVER_ID);
        writeD(0x0001D608);
        writeD(0x0001D5A4);
        writeD(0x00000000);// spacing
        writeD(0x0001D5A4);
        writeD(0x004E9E4A);
        writeC(0x00);// unk
        writeC(GSConfig.SERVER_COUNTRY_CODE);// country code;
        writeC(0x00);// unk

        int serverMode = (characterLimitCount * 0x10) | characterFactionsMode;

        if (GSConfig.ENABLE_RATIO_LIMITATION) {
            if (GameServer.getCountFor(Race.ELYOS) + GameServer.getCountFor(Race.ASMODIANS) > GSConfig.RATIO_HIGH_PLAYER_COUNT_DISABLING) {
                writeC(serverMode | 0x0C);
            } else if (GameServer.getRatiosFor(Race.ELYOS) > GSConfig.RATIO_MIN_VALUE) {
                writeC(serverMode | 0x04);
            } else if (GameServer.getRatiosFor(Race.ASMODIANS) > GSConfig.RATIO_MIN_VALUE) {
                writeC(serverMode | 0x08);
            } else {
                writeC(serverMode);
            }
        } else {
            writeC(serverMode | characterCreateMode);
        }

        writeD((int) (System.currentTimeMillis() / 1000));
        writeH(0x015E);
        writeH(0x0A01);
        writeH(0x0A01);
        writeH(0x150B);
        writeH(2);
        writeC(GSConfig.CHARACTER_REENTRY_TIME);
        writeC(EventService.getInstance().getEventType().getId());
        writeD(0x00); // 2.5
        writeD(0x5460);
        writeC(4);
        writeH(1);// its loop size

        writeC(0);

        writeB(IPConfig.getDefaultAddress());
        writeH(ChatService.getPort());
    }
}
