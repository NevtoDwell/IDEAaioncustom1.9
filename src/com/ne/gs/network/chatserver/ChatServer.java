/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.chatserver;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.network.Dispatcher;
import com.ne.commons.network.NioServer;
import com.ne.gs.configs.network.NetworkConfig;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.chatserver.serverpackets.SM_CS_PLAYER_AUTH;
import com.ne.gs.network.chatserver.serverpackets.SM_CS_PLAYER_LOGOUT;
import com.ne.gs.network.factories.CsPacketHandlerFactory;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
public class ChatServer {

    private static final Logger log = LoggerFactory.getLogger(ChatServer.class);

    private ChatServerConnection chatServer;
    private NioServer nioServer;

    private final boolean serverShutdown = false;

    public static ChatServer getInstance() {
        return SingletonHolder.instance;
    }

    private ChatServer() {
    }

    public void setNioServer(NioServer nioServer) {
        this.nioServer = nioServer;
    }

    /**
     * @return
     */
    public ChatServerConnection connect() {
        SocketChannel sc;
        for (; ; ) {
            chatServer = null;
            log.info("Connecting to ChatServer: " + NetworkConfig.CHAT_ADDRESS);
            try {
                sc = SocketChannel.open(NetworkConfig.CHAT_ADDRESS);
                sc.configureBlocking(false);
                Dispatcher d = nioServer.getReadWriteDispatcher();
                CsPacketHandlerFactory csPacketHandlerFactory = new CsPacketHandlerFactory();
                chatServer = new ChatServerConnection(sc, d, csPacketHandlerFactory.getPacketHandler());

                // register
                d.register(sc, SelectionKey.OP_READ, chatServer);

                // initialized
                chatServer.initialized();

                return chatServer;
            } catch (Exception e) {
                log.info("Cant connect to ChatServer: " + e.getMessage());
            }
            try {
                /**
                 * 10s sleep
                 */
                Thread.sleep(10 * 1000);
            } catch (Exception e) {
            }
        }
    }

    /**
     * This method is called when we lost connection to ChatServer.
     */
    public void chatServerDown() {
        log.warn("Connection with ChatServer lost...");

        chatServer = null;

        if (!serverShutdown) {
            ThreadPoolManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    connect();
                }
            }, 5000);
        }
    }

    /**
     * @param player
     */
    public void sendPlayerLoginRequst(Player player) {
        if (chatServer != null) {
            chatServer.sendPacket(new SM_CS_PLAYER_AUTH(player.getObjectId(), player.getAcountName(), player.getName()));
        }
    }

    /**
     * @param player
     */
    public void sendPlayerLogout(Player player) {
        if (chatServer != null) {
            chatServer.sendPacket(new SM_CS_PLAYER_LOGOUT(player.getObjectId()));
        }
    }

    @SuppressWarnings("synthetic-access")
    private static final class SingletonHolder {

        protected static final ChatServer instance = new ChatServer();
    }
}
