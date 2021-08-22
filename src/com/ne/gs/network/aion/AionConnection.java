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
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;
import com.google.common.base.Preconditions;
import com.mw.networking.PacketProcessor;
import com.ne.commons.utils.concurrent.ExecuteWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.network.AConnection;
import com.ne.commons.network.Dispatcher;
import com.ne.gs.configs.network.NetworkConfig;
import com.ne.gs.model.account.Account;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.Crypt;
import com.ne.gs.network.aion.serverpackets.SM_KEY;
import com.ne.gs.network.loginserver.LoginServer;
import com.ne.gs.network.loginserver.serverpackets.SM_MAC;
import com.ne.gs.services.custom.ChatServerLogService;
import com.ne.gs.services.player.PlayerLeaveWorldService;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * Object representing connection between GameServer and Aion Client.
 *
 * @author -Nemesiss-
 */
public class AionConnection extends AConnection {

    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(AionConnection.class);

    //private static final PacketProcessor<AionConnection> packetProcessor = new PacketProcessor<>(NetworkConfig.PACKET_PROCESSOR_MIN_THREADS,
        //NetworkConfig.PACKET_PROCESSOR_MAX_THREADS, NetworkConfig.PACKET_PROCESSOR_THREAD_SPAWN_THRESHOLD,
        //NetworkConfig.PACKET_PROCESSOR_THREAD_KILL_THRESHOLD, new ExecuteWrapper());

    private static final PacketProcessor<AionConnection> packetProcessor = new PacketProcessor<>(new ExecuteWrapper());

    /* Begin handle packets */
    static {
        packetProcessor.start(NetworkConfig.PACKET_PROCESSOR_MAX_THREADS);
    }

    /**
     * Possible states of AionConnection
     */
    public static enum State {
        /**
         * client just connect
         */
        CONNECTED(1 << 1),
        /**
         * client is authenticated
         */
        AUTHED(1 << 2),
        /**
         * client entered world.
         */
        IN_GAME(1 << 3);

        private final int _id;

        private State(int id) {
            _id = id;
        }

        public int getId() {
            return _id;
        }
    }

    /**
     * Server Packet "to send" Queue
     */
    private final Deque<AionServerPacket> _sendQueue = new ArrayDeque<>(128);

    /**
     * Current state of this connection
     */
    private volatile State state;

    /**
     * AionClient is authenticating by passing to GameServer id of account.
     */
    private Account account;

    /**
     * Crypt that will encrypt/decrypt packets.
     */
    private final Crypt crypt = new Crypt();

    /**
     * active Player that owner of this connection is playing [entered game]
     */
    private final AtomicReference<Player> activePlayer = new AtomicReference<>();
    private String lastPlayerName = "";

    private long lastPingTimeMS;

    private int nbInvalidPackets = 0;
    private final static int MAX_INVALID_PACKETS = 3;

    private String macAddress;

    /**
     * Ping checker - for detecting hanged up connections *
     */
    private final PingChecker pingChecker;

    /**
     * Constructor
     *
     * @param sc
     * @param d
     *
     * @throws IOException
     */
    public AionConnection(SocketChannel sc, Dispatcher d) throws IOException {
        super(sc, d, 8192 * 2, 8192 * 2);

        state = State.CONNECTED;

        String ip = getIP();
        log.debug("connection from: " + ip);

        pingChecker = new PingChecker();
        pingChecker.start();
    }

    @Override
    protected void initialized() {
        /** Send SM_KEY packet */
        sendPacket(new SM_KEY());
    }

    /**
     * Enable crypt key - generate random key that will be used to encrypt second server packet [first one is unencrypted] and decrypt client packets. This
     * method is called from SM_KEY server packet, that packet sends key to aion client.
     *
     * @return "false key" that should by used by aion client to encrypt/decrypt packets.
     */
    public final int enableCryptKey() {
        return crypt.enableKey();
    }

    /**
     * Called by Dispatcher. ByteBuffer data contains one packet that should be processed.
     *
     * @param data
     *
     * @return True if data was processed correctly, False if some error occurred and connection should be closed NOW.
     */
    @Override
    protected final boolean processData(ByteBuffer data) {
        try {
            if (!crypt.decrypt(data)) {
                nbInvalidPackets++;
                log.debug("[" + nbInvalidPackets + "/" + MAX_INVALID_PACKETS + "] Decrypt fail, client packet passed...");
                if (nbInvalidPackets >= MAX_INVALID_PACKETS) {
                    log.warn("Decrypt fail!");
                    return false;
                }
                return true;
            }
        } catch (Exception ex) {
            log.error("Exception caught during decrypt!" + ex.getMessage());
            return false;
        }

        if (data.remaining() < 5) {// op + static code + op == 5 bytes
            log.error("Received fake packet from: " + this);
            return false;
        }

        AionClientPacket pck = Packets.handleClientPacket(data, this);

        if (pck != null && pck.read()) {
            if (NetworkConfig.LOG_CLIENT_PCK) {
                log.info(pck.toString());
            }

            packetProcessor.executePacket(pck);
        }

        return true;
    }

    /**
     * This method will be called by Dispatcher, and will be repeated till return false.
     *
     * @param data
     *
     * @return True if data was written to buffer, False indicating that there are not any more data to write.
     */
    @Override
    protected final boolean writeData(ByteBuffer data) {
        synchronized (guard) {
            AionServerPacket pck = _sendQueue.pollFirst();
            if (pck == null) {
                return false;
            }

            if (NetworkConfig.LOG_SERVER_PCK) {
                log.info(pck.toString());
            }

            pck.write(this, data);
            return true;
        }
    }

    /**
     * This method is called by Dispatcher when connection is ready to be closed.
     *
     * @return time in ms after witch onDisconnect() method will be called. Always return 0.
     */
    @Override
    protected final long getDisconnectionDelay() {
        return 0;
    }

    @Override
    protected final void onDisconnect() {
        /**
         * Client starts authentication procedure
         */
        pingChecker.stop();
        if (getAccount() != null) {
            LoginServer.getInstance().aionClientDisconnected(getAccount().getId());
            LoginServer.getInstance().sendPacket(new SM_MAC(getAccount().getId(), macAddress));
        }
        Player player = getActivePlayer();
        if (player != null) {
            PlayerLeaveWorldService.tryLeaveWorld(player);
            
            if(player.isGM())
            	ChatServerLogService.getInstance().evtLoggedOut(player);
        }
    }

    @Override
    protected final void onServerClose() {
        // TODO mb some packet should be send to client before closing?
        close(/* packet, */true);
    }

    /**
     * Encrypt packet.
     *
     * @param buf
     */
    public final void encrypt(ByteBuffer buf) {
        crypt.encrypt(buf);
    }

    /**
     * Sends AionServerPacket to this client.
     *
     * @param pck
     *     AionServerPacket to be sent.
     */
    public final void sendPacket(AionServerPacket pck) {
        if (pck == null) {
            return;
        }

        Packets.handleServerPacket(pck);

        synchronized (guard) {
            /**
             * Connection is already closed or waiting for last (close packet) to be sent
             */
            if (isWriteDisabled()) {
                return;
            }

            _sendQueue.addLast(pck);
            enableWriteInterest();
        }
    }

    /**
     * Its guaranteed that closePacket will be sent before closing connection, but all past and future packets wont. Connection will be closed [by Dispatcher
     * Thread], and onDisconnect() method will be called to clear all other things. forced means that server shouldn't wait with removing this connection.
     *
     * @param closePacket
     *     Packet that will be send before closing.
     * @param forced
     *     have no effect in this implementation.
     */
    public final void close(AionServerPacket closePacket, boolean forced) {
        if (closePacket == null) {
            return;
        }

        Packets.handleServerPacket(closePacket);

        synchronized (guard) {
            if (isWriteDisabled()) {
                return;
            }

            pendingClose = true;
            isForcedClosing = forced;
            _sendQueue.clear();
            _sendQueue.addLast(closePacket);
            enableWriteInterest();
        }
    }

    /**
     * Current state of this connection
     *
     * @return state
     */
    public final State getState() {
        return state;
    }

    /**
     * Sets the state of this connection
     *
     * @param state
     *     state of this connection
     */
    public void setState(State state) {
        this.state = state;
    }

    /**
     * Returns account object associated with this connection
     *
     * @return account object associated with this connection
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Sets account object associated with this connection
     *
     * @param account
     *     account object associated with this connection
     */
    public void setAccount(Account account) {
        Preconditions.checkArgument(account != null, "Account can't be null");
        this.account = account;
    }

    /**
     * Sets Active player to new value. Update connection state to correct value.
     *
     * @param player
     *
     * @return True if active player was set to new value.
     */
    public boolean setActivePlayer(Player player) {
        if (player == null) {
            activePlayer.set(player);
            setState(State.AUTHED);
        } else if (activePlayer.compareAndSet(null, player)) {
            setState(State.IN_GAME);
            lastPlayerName = player.getName();
        } else {
            return false;
        }
        return true;
    }

    /**
     * Return active player or null.
     *
     * @return active player or null.
     */
    public Player getActivePlayer() {
        return activePlayer.get();
    }

    /**
     * @return the lastPingTimeMS
     */
    public long getLastPingTimeMS() {
        return lastPingTimeMS;
    }

    /**
     * @param lastPingTimeMS
     *     the lastPingTimeMS to set
     */
    public void setLastPingTimeMS(long lastPingTimeMS) {
        this.lastPingTimeMS = lastPingTimeMS;
    }

    public void closeNow() {
        this.close(false);
    }

    public void setMacAddress(String mac) {
        macAddress = mac;
    }

    public String getMacAddress() {
        return macAddress;
    }

    @Override
    public String toString() {
        Player player = activePlayer.get();
        if (player != null) {
            return "AionConnection [state=" + state + ", account=" + account + ", getObjectId()=" + player
                .getObjectId() + ", lastPlayerName=" + lastPlayerName
                + ", macAddress=" + macAddress + ", getIP()=" + getIP() + "]";
        }
        return "";
    }

    private class PingChecker implements Runnable {

        // we don't have to detect hanged connections immediately
        // its rather some very rare case so 10 minutes check should be enough
        private static final int checkTime = 10 * 60 * 1000;
        private ScheduledFuture<?> task;
        private boolean started;

        private void start() {
            Preconditions.checkState(!started, "PingChecker can be started only one time!");
            started = true;
            task = ThreadPoolManager.getInstance().scheduleAtFixedRate(this, checkTime, checkTime);
        }

        private void stop() {
            task.cancel(false);
        }

        @Override
        public void run() {
            if (System.currentTimeMillis() - getLastPingTimeMS() > checkTime) {
                log.info("Found hanged up client: " + AionConnection.this + " - closing now :)");
                closeNow();
            }
        }
    }
}