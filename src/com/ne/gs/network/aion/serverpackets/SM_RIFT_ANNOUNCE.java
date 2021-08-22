/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.controllers.RiftController;
import com.ne.gs.model.Race;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author Sweetkr
 */
public class SM_RIFT_ANNOUNCE extends AionServerPacket {

    private final int actionId;
    private Race race;
    private RiftController rift;
    private int objectId;
    private int gelkmaros;
    private int inggison;
    private int cl;
    private int cr;
    private int tl;
    private int tr;

    public SM_RIFT_ANNOUNCE(Race race) {

        this.race = race;
        actionId = 0;
    }

    public SM_RIFT_ANNOUNCE(boolean gelkmaros, boolean inggison) {
        this.gelkmaros = (gelkmaros ? 1 : 0);
        this.inggison = (inggison ? 1 : 0);
        actionId = 1;
    }

    public SM_RIFT_ANNOUNCE(RiftController rift, boolean isMaster) {

        this.rift = rift;
        if (isMaster) {
            actionId = 2;
        } else {
            actionId = 3;
        }
    }

    /**
     * Rift despawn
     *
     * @param objectId
     */
    public SM_RIFT_ANNOUNCE(int objectId) {

        this.objectId = objectId;
        actionId = 4;
    }

    public SM_RIFT_ANNOUNCE(boolean cl, boolean cr, boolean tl, boolean tr) {
        this.cl = (cl ? 1 : 0);
        this.cr = (cr ? 1 : 0);
        this.tl = (tl ? 1 : 0);
        this.tr = (tr ? 1 : 0);
        actionId = 5;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        switch (actionId) {
            case 0: // announce
                writeH(0x09);
                writeC(actionId);
                switch (race) {
                    case ASMODIANS:
                        writeD(1);
                        writeD(0);
                        break;
                    case ELYOS:
                        writeD(1);
                        writeD(0);
                }
                break;
            case 1:
                writeH(9);
                writeC(actionId);
                writeD(gelkmaros);
                writeD(inggison);
                break;
            case 2:
                writeH(0x21);
                writeC(actionId);
                writeD(rift.getOwner().getObjectId());
                writeD(rift.getMaxEntries() - rift.getUsedEntries());
                writeD(rift.getRemainTime());
                writeD(rift.getMinLevel());
                writeD(rift.getMaxLevel());
                writeF(rift.getOwner().getX());
                writeF(rift.getOwner().getY());
                writeF(rift.getOwner().getZ());
                break;
            case 3:
                writeH(0x0D);
                writeC(actionId);
                writeD(rift.getOwner().getObjectId());
                writeD(rift.getUsedEntries());
                writeD(rift.getRemainTime());
                break;
            case 4:
                writeH(0x05);
                writeC(actionId);
                writeD(objectId);
                break;
            case 5:
                writeH(0x05);
                writeC(actionId);
                writeC(cl);
                writeC(cr);
                writeC(tl);
                writeC(tr);
                break;
        }
    }
}
