/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.taskmanager.tasks;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.taskmanager.AbstractFIFOPeriodicTaskManager;

/**
 * @author lord_rex and MrPoke
 */
public final class PacketBroadcaster extends AbstractFIFOPeriodicTaskManager<Creature> {

    private static final class SingletonHolder {

        private static final PacketBroadcaster INSTANCE = new PacketBroadcaster();
    }

    public static PacketBroadcaster getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private PacketBroadcaster() {
        super(200);
    }

    public static enum BroadcastMode {
        UPDATE_STATS {
            @Override
            public void sendPacket(Creature creature) {
                creature.getGameStats().updateStatInfo();
            }
        },

        UPDATE_SPEED {
            @Override
            public void sendPacket(Creature creature) {
                creature.getGameStats().updateSpeedInfo();
            }
        },

        UPDATE_PLAYER_HP_STAT {
            @Override
            public void sendPacket(Creature creature) {
                ((Player) creature).getLifeStats().sendHpPacketUpdateImpl();
            }
        },
        UPDATE_PLAYER_MP_STAT {
            @Override
            public void sendPacket(Creature creature) {
                ((Player) creature).getLifeStats().sendMpPacketUpdateImpl();
            }
        },
        UPDATE_PLAYER_EFFECT_ICONS {
            @Override
            public void sendPacket(Creature creature) {
                creature.getEffectController().updatePlayerEffectIconsImpl();
            }
        },

        UPDATE_PLAYER_FLY_TIME {
            @Override
            public void sendPacket(Creature creature) {
                ((Player) creature).getLifeStats().sendFpPacketUpdateImpl();
            }
        },

	    UPDATE_PLAYER_DP_STAT {
		    @Override
	        public void sendPacket(Creature creature) {
			    ((Player) creature).getLifeStats().sendDpPacketUpdateImpl();
		    }
	    },

        BROAD_CAST_EFFECTS {
            @Override
            public void sendPacket(Creature creature) {
                creature.getEffectController().broadCastEffectsImp();
            }
        };

        private final byte MASK;

        private BroadcastMode() {
            MASK = (byte) (1 << ordinal());
        }

        public byte mask() {
            return MASK;
        }

        protected abstract void sendPacket(Creature creature);

        protected final void trySendPacket(Creature creature, byte mask) {
            if ((mask & mask()) == mask()) {
                sendPacket(creature);
                creature.removePacketBroadcastMask(this);
            }
        }
    }

    private static final BroadcastMode[] VALUES = BroadcastMode.values();

    @Override
    protected void callTask(Creature creature) {
        for (byte mask; (mask = creature.getPacketBroadcastMask()) != 0; ) {
            for (BroadcastMode mode : VALUES) {
                mode.trySendPacket(creature, mask);
            }
        }
    }

    @Override
    protected String getCalledMethodName() {
        return "packetBroadcast()";
    }
}
