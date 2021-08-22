/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.network.aion.AionClientPacket;

public class CM_UNK extends AionClientPacket {

    int size;
    int unk0;
    byte unk1;
    byte unk2;
    byte unk3;
    byte unk4;
    int someId;
    int sequence;
    byte[] data;

    @Override
    protected void readImpl() {
        size = readD();
        unk1 = (byte) readC();
        unk2 = (byte) readC();
        unk3 = (byte) readC();
        unk4 = (byte) readC();
        size = readD();
        unk0 = readD();
        unk0 = readD();
        someId = readD();
        unk0 = readD();
        sequence = readD();
        unk0 = readD();
        data = readB(size - 36);
    }

    @Override
    protected void runImpl() {
    }
}
