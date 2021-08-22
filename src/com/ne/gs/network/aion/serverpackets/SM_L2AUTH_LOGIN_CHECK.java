/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author -Nemesiss-
 */
public class SM_L2AUTH_LOGIN_CHECK extends AionServerPacket {

    /**
     * True if client is authed.
     */
    private final boolean ok;
    private final String accountName;
    private static final byte[] unk = hex2Byte
        ("000000610010ABD7170100804628070100F0888F060100103527070100205C2707010010161D0D050030641D0D0500203D1D0D010050B21D0D0200408B1D0D010070001E0D010010B9FE1E010090E4512A01009011832B0100107BEA2A0100104EB9290100B050E31100003018E2110000304513130000406C13130000B0AE7A120000C0D57A12000010CAE111000010F712130000201E13130000E0F21413000090607A120000A0877A120000400E7C12000060FEE4110000608DE21100009002E311000080DBE2110000E0C5E3110000C077E3110000D09EE31100005066E211000070B4E2110000E036E611000030FAE6110000A00BE81100004021E7110000B032E81100000085E6110000103BE41100007025E5110000902F14130000C0A41413000060BA13130000D0CB14130000509313130000A05614130000800814130000B07D14130000F0191513000050D7E4110000C059E8110000A09AE5110000B0C1E51100003089E41100009073E511000000727B12000010ACE611000090E4E71100007096E7110000606FE711000080BDE7110000403FE2110000E0237B120000F04A7B12000030E77B120000D0FC7A12000010997B12000020C07B12000040B0E4110000F05DE6110000A029E3110000E0A7E811000070E113130000804CE51100000014E4110000F0ECE3110000F0CEE8110000C0E8E5110000909E8E060100A0C58E0601002094C323020030BBC3230100907F840C0500B0CD840C0500A0A6840C0100E042850C0200C0F4840C0100D01B850C01009022661E010040E2C3230100106DC3230100");

    /**
     * Constructs new <tt>SM_L2AUTH_LOGIN_CHECK </tt> packet
     */
    public SM_L2AUTH_LOGIN_CHECK(boolean ok, String accountName) {
        this.ok = ok;
        this.accountName = accountName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeImpl(AionConnection con) {
        writeD(ok ? 0x00 : 0x01);
        writeB(unk);
        writeS(accountName);
    }

    private static byte[] hex2Byte(String str) {
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(str.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
}
