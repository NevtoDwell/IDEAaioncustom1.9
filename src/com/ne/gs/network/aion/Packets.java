/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.Util;
import com.ne.commons.annotations.NotNull;
import com.ne.commons.annotations.Nullable;
import com.ne.commons.utils.collections.CopyOnWriteMap;
import com.ne.gs.configs.network.NetworkConfig;
import com.ne.gs.network.aion.clientpackets.ClientPackets;
import com.ne.gs.network.aion.serverpackets.ServerPackets;

import static com.ne.gs.network.aion.AionConnection.State;

/**
 * @author hex1r0
 */
public final class Packets {

    private Packets() {
    }

    private static final Logger _log = LoggerFactory.getLogger(Packets.class);

    // TODO rework, use lighter method
    private static final Map<Integer, AionClientPacket> _clientPackets = CopyOnWriteMap.of();

    public static void regCP(@NotNull Class<? extends AionClientPacket> type,
                             int opcode, @NotNull State... states) {
        int stateMask = 0;
        for (State state : states) {
            stateMask |= state.getId();
        }

        regCP(type, opcode, stateMask);
    }

    public static void regCP(@NotNull Class<? extends AionClientPacket> type,
                             int opcode, int stateMask) {
        String name = type.getSimpleName();
        try {
            AionClientPacket pck = type.newInstance();
            AionClientPacket old = _clientPackets.put(opcode, pck);
            pck.setOpcode(opcode);
            pck.setStateMask(stateMask);

            if (old != null) {
                _log.warn(String.format("%s replaced %s", name, old.getClass().getSimpleName()));
            }
        } catch (Exception e) {
            _log.warn(e.getMessage(), e);
        }
    }

    @Nullable
    public static AionClientPacket handleClientPacket(ByteBuffer buf, AionConnection con) {
        State state = con.getState();
        int opcode = buf.getShort() & 0xffff;  // Second opcode
        buf.position(buf.position() + 3);

        AionClientPacket proto = _clientPackets.get(opcode);

        if (proto == null) {
            if (NetworkConfig.DISPLAY_UNKNOWNPACKETS) {
                _log.warn(String.format("Unknown packet recived from Aion client: 0x%04X, state=%s %n%s", opcode, state
                    .toString(), Util.toHex(buf)));
            }
            return null;
        }

        AionClientPacket pck = proto.clonePacket();
        pck.setBuffer(buf);
        pck.setConnection(con);

        return pck;
    }

    // TODO rework, use lighter method
    private static final Map<Class<? extends AionServerPacket>, Integer> _serverPackets = CopyOnWriteMap.of();

    public static void regSP(Class<? extends AionServerPacket> type, Integer opcode) {
        Integer old = _serverPackets.put(type, opcode);
        if (old != null) {
            _log.warn(String.format("%d replaced %d", opcode, old));
        }
    }

    public static void handleServerPacket(@NotNull AionServerPacket pck) {
        if (pck.getOpcode() == Integer.MIN_VALUE) {
            Integer opcode = _serverPackets.get(pck.getClass());
            if (opcode == null) {
                throw new IllegalArgumentException(
                    "There is no opcode for " + pck.getClass().getSimpleName() + " defined.");
            }

            pck.setOpcode(opcode);
        }
    }

    private static final List<Runnable> _loaders = new CopyOnWriteArrayList<>();

    public static void addLoader(Runnable loader) {
        _loaders.add(loader);
    }

    public static void addLoaderAndRun(Runnable loader) {
        _loaders.add(loader);
        loader.run();
    }

    public static void reload() {
        _clientPackets.clear();
        _serverPackets.clear();

        try {
            BufferedReader r = new BufferedReader(new FileReader("./config/network/packets.cfg"));
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                String[] tokens = line.split("\\s+");

                if (tokens.length >= 3) {
                    parseClientPacket(tokens);
                } else if (tokens.length == 2) {
                    parseServerPacket(tokens);
                } else {
                    throw new Exception("Invalid format near: " + line);
                }
            }
            r.close();
        } catch (Exception e) {
            _log.error("", e);
        }

        for (Runnable loader : _loaders) {
            loader.run();
        }
    }

    @SuppressWarnings("unchecked")
    private static void parseClientPacket(String[] tokens) {
        String name = tokens[0];
        Integer opcode = Integer.decode(tokens[1]);

        int stateMask = 0;
        for (int i = 2; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("C")) {
                stateMask |= State.CONNECTED.getId();
            } else if (tokens[i].equalsIgnoreCase("A")) {
                stateMask |= State.AUTHED.getId();
            } else if (tokens[i].equalsIgnoreCase("G")) {
                stateMask |= State.IN_GAME.getId();
            }
        }

        try {
            Class<?> type = Class.forName(ClientPackets.class.getPackage().getName() + "." + name);
            if (type != null) {
                regCP((Class<? extends AionClientPacket>) type, opcode, stateMask);
            }
        } catch (Exception e) {
            _log.warn("", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void parseServerPacket(String[] tokens) {
        String name = tokens[0];
        Integer opcode = Integer.decode(tokens[1]);
        try {
            Class<?> type = Class.forName(ServerPackets.class.getPackage().getName() + "." + name);
            if (type != null) {
                regSP((Class<? extends AionServerPacket>) type, opcode);
            }
        } catch (Exception e) {
            _log.warn("", e);
        }
    }
}
