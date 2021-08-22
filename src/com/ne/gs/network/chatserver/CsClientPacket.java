/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.chatserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.network.packet.BaseClientPacket;

/**
 * @author ATracer
 */
public abstract class CsClientPacket extends BaseClientPacket<ChatServerConnection> implements Cloneable {

    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(CsClientPacket.class);

    /**
     * Constructs new client packet with specified opcode. If using this constructor, user must later manually set buffer and connection.
     *
     * @param opcode
     *     packet id
     */
    protected CsClientPacket(int opcode) {
        super(opcode);
    }

    /**
     * run runImpl catching and logging Throwable.
     */
    @Override
    public final void run() {
        try {
            runImpl();
        } catch (Throwable e) {
            log.warn("error handling ls (" + getConnection().getIP() + ") message " + this, e);
        }
    }

    /**
     * Send new LsServerPacket to connection that is owner of this packet. This method is equivalent to: getConnection().sendPacket(msg);
     *
     * @param msg
     */
    protected void sendPacket(CsServerPacket msg) {
        getConnection().sendPacket(msg);
    }

    /**
     * Clones this packet object.
     *
     * @return CsClientPacket
     */
    public CsClientPacket clonePacket() {
        try {
            return (CsClientPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
