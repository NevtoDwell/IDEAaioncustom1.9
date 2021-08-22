/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.network.packet.BaseClientPacket;

/**
 * Base class for every Aion -> LS Client Packet
 *
 * @author -Nemesiss-
 */
public abstract class AionClientPacket extends BaseClientPacket<AionConnection> implements Cloneable {

    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(AionClientPacket.class);

    private int _stateMask;

    public AionClientPacket() {
        super(0);
    }

    public int getStateMask() {
        return _stateMask;
    }

    public void setStateMask(int stateMask) {
        _stateMask = stateMask;
    }

    /**
     * run runImpl catching and logging Throwable.
     */
    @Override
    public final void run() {

        try {
            // run only if packet is still valid (connection state didn't changed)
            if (isValid()) {
            	String name = getConnection().getIP();
            	log.info("Handling "+name+ " client (" + this.getPacketName() + ") message :" + this);
                runImpl();
            }
        } catch (Throwable e) {
            String name = getConnection().getAccount().getName();
            if (name == null) {
                name = getConnection().getIP();
            }

            log.error("Error handling client (" + name + ") message :" + this, e);
        }
    }

    /**
     * Send new AionServerPacket to connection that is owner of this packet. This method is equvalent to: getConnection().sendPacket(msg);
     *
     * @param msg
     */
    protected void sendPacket(AionServerPacket msg) {
        getConnection().sendPacket(msg);
    }

    /**
     * Clones this packet object.
     *
     * @return AionClientPacket
     */
    public AionClientPacket clonePacket() {
        try {
            return (AionClientPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    protected final String readS(int size) {
        String string = readS();
        if (string != null) {
            readB(size - (string.length() * 2 + 2));
        } else {
            readB(size);
        }
        return string;
    }

    /**
     * Check if packet is still valid for its connection.
     *
     * @return true if packet is still valid and should be processed.
     */
    public final boolean isValid() {
        int state = getConnection().getState().getId();
        boolean valid = (_stateMask & state) == state;

        if (!valid) {
            log.info(this + " wont be processed cuz its valid state don't match current connection state: " + getConnection().getState());
        }

        return valid;
    }
}
