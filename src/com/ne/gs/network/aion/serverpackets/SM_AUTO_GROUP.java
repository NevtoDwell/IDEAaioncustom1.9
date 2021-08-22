/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.autogroup.AutoGroupsType;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author SheppeR, Guapo, nrg
 */
public class SM_AUTO_GROUP extends AionServerPacket {

    private static final Logger log = LoggerFactory.getLogger(SM_AUTO_GROUP.class);
    private int windowId;
    private final int instanceMaskId;
    private int mapId;
    private int messageId;
    private int titleId;
    private int waitTime;
    private boolean close;
    String name = StringUtils.EMPTY;

    public SM_AUTO_GROUP(int instanceMaskId) {
        this.instanceMaskId = instanceMaskId;
    }

    public SM_AUTO_GROUP(int instanceMaskId, int windowId) {
        this.instanceMaskId = instanceMaskId;
        this.windowId = windowId;
    }

    public SM_AUTO_GROUP(int instanceMaskId, int windowId, boolean close) {
        this.instanceMaskId = instanceMaskId;
        this.windowId = windowId;
        this.close = close;
    }

    public SM_AUTO_GROUP(int instanceMaskId, int windowId, int waitTime, String name) {
        this.instanceMaskId = instanceMaskId;
        this.windowId = windowId;
        this.waitTime = waitTime;
        this.name = name;
    }

    @Override
    protected void writeImpl(AionConnection con) {

        AutoGroupsType agt = AutoGroupsType.getAutoGroupByInstanceMaskId((byte) instanceMaskId);
        if (agt == null) {
            log.warn("Auto Groups Type no found for Instance MaskId: " + instanceMaskId);
            return;
        }
        messageId = agt.getNameId();
        titleId = agt.getTittleId();
        mapId = agt.getInstanceMapId();

        writeD(instanceMaskId);
        writeC(windowId);
        writeD(mapId);
        switch (windowId) {
            case 0: // request entry
                writeD(messageId);
                writeD(titleId);
                writeD(0);
                break;
            case 1: // waiting window
                writeD(0);
                writeD(0);
                writeD(waitTime);
                break;
            case 2: // cancel looking
                writeD(0);
                writeD(0);
                writeD(0);
                break;
            case 3: // pass window
                writeD(0);
                writeD(0);
                writeD(waitTime);
                break;
            case 4: // enter window
                writeD(0);
                writeD(0);
                writeD(0);
                break;
            case 5: // after you click enter
                writeD(0);
                writeD(0);
                writeD(0);
                break;
            case 6: // entry icon
                writeD(messageId);
                writeD(titleId);
                writeD(close ? 0 : 1);
                break;
            case 7: // failed window
                writeD(messageId);
                writeD(titleId);
                writeD(0);
                break;
            case 8: // on login
                writeD(0);
                writeD(0);
                writeD(waitTime);
                break;
        }
        writeC(0);
        writeS(name);
    }
}
