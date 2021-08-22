/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.loginserver;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.ne.commons.network.AConnection;
import com.ne.commons.network.ConnectionFactory;
import com.ne.commons.network.Dispatcher;

/**
 * ConnectionFactory implementation that will be creating AionConnections
 *
 * @author -Nemesiss-
 */
public class LoginConnectionFactoryImpl implements ConnectionFactory {

    /**
     * Create a new {@link com.ne.commons.network.AConnection AConnection} instance.<br>
     *
     * @param socket
     *     that new {@link com.ne.commons.network.AConnection AConnection} instance will represent.<br>
     * @param dispatcher
     *     to witch new connection will be registered.<br>
     *
     * @return a new instance of {@link com.ne.commons.network.AConnection AConnection}<br>
     *
     * @throws IOException
     * @see com.ne.commons.network.AConnection
     * @see com.ne.commons.network.Dispatcher
     */

	/*
     * (non-Javadoc)
	 * @see com.ne.commons.network.ConnectionFactory#create(java.nio.channels.SocketChannel, com.ne.commons.network.Dispatcher)
	 */
    @Override
    public AConnection create(SocketChannel socket, Dispatcher dispatcher) throws IOException {
        return new LoginServerConnection(socket, dispatcher);
    }
}
