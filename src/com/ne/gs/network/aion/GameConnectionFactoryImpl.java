/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.network.AConnection;
import com.ne.commons.network.ConnectionFactory;
import com.ne.commons.network.Dispatcher;
import com.ne.gs.configs.network.NetworkConfig;
import com.ne.gs.network.sequrity.FloodManager;
import com.ne.gs.network.sequrity.FloodManager.Result;

/**
 * ConnectionFactory implementation that will be creating AionConnections
 *
 * @author -Nemesiss-
 */
public class GameConnectionFactoryImpl implements ConnectionFactory {

    private final Logger log = LoggerFactory.getLogger(GameConnectionFactoryImpl.class);
    private FloodManager floodAcceptor;

    /**
     * Create a new {@link com.ne.commons.network.AConnection AConnection} instance.<br>
     *
     * @return a new instance of {@link com.ne.commons.network.AConnection AConnection}<br>
     *
     * @throws IOException
     * @see com.ne.commons.network.AConnection
     * @see com.ne.commons.network.Dispatcher
     */

    public GameConnectionFactoryImpl() {
        if (NetworkConfig.ENABLE_FLOOD_CONNECTIONS) {
            floodAcceptor = new FloodManager(NetworkConfig.Flood_Tick, new FloodManager.FloodFilter(NetworkConfig.Flood_SWARN, NetworkConfig.Flood_SReject,
                NetworkConfig.Flood_STick), // short period
                new FloodManager.FloodFilter(NetworkConfig.Flood_LWARN, NetworkConfig.Flood_LReject, NetworkConfig.Flood_LTick)); // long period
        }
    }

    /*
     * (non-Javadoc)
     * @see com.ne.commons.network.ConnectionFactory#create(java.nio.channels.SocketChannel, com.ne.commons.network.Dispatcher)
     */
    @Override
    public AConnection create(SocketChannel socket, Dispatcher dispatcher) throws IOException {
        if (NetworkConfig.ENABLE_FLOOD_CONNECTIONS) {
            String host = socket.socket().getInetAddress().getHostAddress();
            Result isFlooding = floodAcceptor.isFlooding(host, true);
            switch (isFlooding) {
                case REJECTED: {
                    log.warn("Rejected connection from " + host);
                    socket.close();
                    return null;
                }
                case WARNED: {
                    log.warn("Connection over warn limit from " + host);
                    break;
                }
            }
        }

        return new AionConnection(socket, dispatcher);
    }
}
