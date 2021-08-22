/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.audit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javolution.util.FastMap;

import com.ne.commons.utils.L10N;
import com.ne.gs.configs.administration.AdminConfig;
import com.ne.gs.model.ChatType;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_MESSAGE;

/**
 * @author MrPoke
 */
public class GMService {

    public static GMService getInstance() {
        return SingletonHolder.instance;
    }

    private final Map<Integer, Player> gms = new FastMap<>();
    private boolean announceAny = false;
    private final List<Byte> announceList;

    private GMService() {
        announceList = new ArrayList<>();
        announceAny = AdminConfig.ANNOUNCE_LEVEL_LIST.equals("*");
        if (!announceAny) {
            try {
                for (String level : AdminConfig.ANNOUNCE_LEVEL_LIST.split(",")) {
                    announceList.add(Byte.parseByte(level));
                }
            } catch (Exception e) {
                announceAny = true;
            }
        }
    }

    public Collection<Player> getGMs() {
        return gms.values();
    }

    public void onPlayerLogin(Player player) {
        if (player.isGM()) {
            gms.put(player.getObjectId(), player);

            if (announceAny || announceList.contains(Byte.valueOf(player.getAccessLevel()))) {
                broadcastMesage(
                    String.format(player.translate(Messages.ENTERED),
                        //player.getCustomTag(true),
                        player.getName()
                    )
                );
            }
        }
    }

    public void onPlayerLogedOut(Player player) {
        gms.remove(player.getObjectId());
    }

    public void broadcastMesage(String message) {
        SM_MESSAGE packet = new SM_MESSAGE(0, null, message, ChatType.YELLOW);
        for (Player player : gms.values()) {
            player.sendPck(packet);
        }
    }

    @SuppressWarnings("synthetic-access")
    private static final class SingletonHolder {
        protected static final GMService instance = new GMService();
    }

    public static enum Messages implements L10N.Translatable {
        ENTERED("\uE042 Annonce: %s just entered into Atreia.");
        //ENTERED("\uE042 Annonce: %s%s just entered into Atreia."); - было (%s (тэг) + %s(имя))player.getAccessLevel()
        private final String _defaultValue;

        private Messages(String defaultValue) {
            _defaultValue = defaultValue;
        }

        @Override
        public String id() {
            return toString();
        }

        @Override
        public String defaultValue() {
            return _defaultValue;
        }
    }
}
