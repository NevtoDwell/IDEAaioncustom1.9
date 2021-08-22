/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.configs.main.SecurityConfig;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author cura
 */
public class SM_CHARACTER_SELECT extends AionServerPacket {

    private final int type; // 0: new passkey input window, 1: passkey input window, 2: message window
    private int messageType; // 0: newpasskey complete, 2: passkey edit complete, 3: passkey input
    private int wrongCount;

    public SM_CHARACTER_SELECT(int type) {
        this.type = type;
    }

    public SM_CHARACTER_SELECT(int type, int messageType, int wrongCount) {
        this.type = type;
        this.messageType = messageType;
        this.wrongCount = wrongCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeC(type);

        switch (type) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                writeH(messageType); // 0: newpasskey complete, 2: passkey edit complete, 3: passkey input
                writeC(wrongCount > 0 ? 1 : 0); // 0: right passkey, 1: wrong passkey
                writeD(wrongCount); // wrong passkey input count
                writeD(SecurityConfig.PASSKEY_WRONG_MAXCOUNT);
                // server default value: 5)
                break;
        }
    }
}
