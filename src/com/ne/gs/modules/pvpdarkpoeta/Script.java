/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.pvpdarkpoeta;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import gnu.trove.map.hash.THashMap;

import com.ne.commons.Sys;
import com.ne.commons.annotations.NotNull;
import com.ne.commons.utils.Actor;
import com.ne.commons.utils.ActorRef;
import com.ne.commons.utils.EventNotifier;
import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.model.Race;
import com.ne.gs.model.conds.SpawnObjCond;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.instance.InstanceScoreType;
import com.ne.gs.modules.common.CustomLocManager;
import com.ne.gs.modules.common.CustomLocScript;
import com.ne.gs.modules.common.CustomLocTemplate;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.Packets;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.KiskService;
import com.ne.gs.services.abyss.AbyssPointsService;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.world.World;
import com.ne.gs.world.WorldMapInstance;
import com.ne.gs.world.knownlist.Visitor;

import static com.ne.gs.services.reward.RewardService.GiveRewardDecision;
import static java.lang.String.format;

/**
 * @author hex1r0
 */
public class Script extends CustomLocScript {

    private static final String ENTERED = "player=%s entered loc=%s";
    private static final String LEFT = "player=%s left loc=%s";
    private static final String GAINED = "player=%s points=%d gained=%d kills=%d";
    private static final String LOST = "player=%s points=%d lost=%d kills=%d";

    private static final String FORMAT = "Игрок %s(%s) вошел в %s. Сейчас в локации Элы: %d, Асмы: %d";

    private static final GiveRewardDecision COND = new GiveRewardDecision("PvPDarkPoetaScript");

    //private final int _pointGain;
    //private final int _pointLoss;

    private final AddAppListener _listener = new AddAppListener();

    private final Map<Integer, Entry> _rewards = new THashMap<>();
    private final ActorRef<?> _proc = ActorRef.of(new Actor());

    private long _lastAnnounceMs = 0;

    public Script(CustomLocTemplate loc, Date expiresAt) {
        super(loc, expiresAt);
        //_pointGain = Integer.parseInt(loc.getPropertyList().getValue("APGain", "0"));
        //_pointLoss = Integer.parseInt(loc.getPropertyList().getValue("APLoss", "0"));
    }

    @Override
    public void onInstanceCreate(WorldMapInstance instance) {
        super.onInstanceCreate(instance);

        EventNotifier.GLOBAL.attach(_listener);
    }

    @Override
    public void onInstanceDestroy() {
        super.onInstanceDestroy();

        EventNotifier.GLOBAL.detach(_listener);
    }

    @Override
    public void onEnterInstance(final Player p) {
        info(format(ENTERED, p.getName(), _template.getId()));

        p.getConditioner().attach(SpawnObjCustomCond.STATIC);
        p.getConditioner().attach(COND);

        _proc.tell(new Runnable() {
            @Override
            public void run() {
                Entry entry = _rewards.get(p.getObjectId());
                if (entry == null) {
                    entry = new Entry();
                    _rewards.put(p.getObjectId(), entry);
                }
                p.sendPck(new SpInstanceScore(entry, remainingTimeMs()));

                if (Sys.millis() - _lastAnnounceMs > TimeUnit.SECONDS.toMillis(10)) {
                    _lastAnnounceMs = Sys.millis();
                    String race = p.getRace() == Race.ELYOS ? "Элиос" : "Асмодианин";

                    final int[] counts = new int[2];
                    instance.doOnAllPlayers(new Visitor<Player>() {
                        @Override
                        public void visit(Player o) {
                            if (o.getRace() == Race.ELYOS) {
                                counts[0]++;
                            } else {
                                counts[1]++;
                            }
                        }
                    });


                    final String msg = String.format(FORMAT, p.getName(), race, _template.getId(), counts[0], counts[1]);
                    World.getInstance().doOnAllPlayers(new Visitor<Player>() {
                        @Override
                        public void visit(Player o) {
                            o.sendMsg(msg);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onExitInstance(final Player player) {
        info(format(LEFT, player.getName(), _template.getId()));
        player.getConditioner().detach(SpawnObjCustomCond.STATIC);
        player.getConditioner().detach(COND);

        _proc.tell(new Runnable() {
            @Override
            public void run() {
                _rewards.remove(player.getObjectId());
            }
        });

        TeleportService.moveToBindLocation(player, true);
    }

    @Override
    public void onLeaveInstance(Player player) {
        info(format(LEFT, player.getName(), _template.getId()));
        player.getConditioner().detach(SpawnObjCustomCond.STATIC);
    }

    private void increasePoints(Player p, int points) {
        Entry entry = calcPoints(p, points);
        entry.kills += 1;
        p.sendPck(new SpInstanceScore(entry, remainingTimeMs()));
        info(format(GAINED, p.getName(), entry.AP, points, entry.kills));
    }

    private void decreasePoints(Player p, int points) {
        Entry entry = calcPoints(p, points);
        entry.deaths += 1;
        p.sendPck(new SpInstanceScore(entry, remainingTimeMs()));
        info(format(LOST, p.getName(), entry.AP, -points, entry.kills));
    }

    private Entry calcPoints(Player p, int points) {
        Entry entry = _rewards.get(p.getObjectId());
        entry.AP += points;
        return entry;
    }

    private void info(String msg) {
        CustomLocManager.log().info(format("PvPDarkPoetaScript: %s", msg));
    }

    private static final class Entry implements Cloneable {
        int AP;
        int kills;
        int deaths;

        Entry() {}

        Entry(int AP, int kills, int deaths) {
            this.AP = AP;
            this.kills = kills;
            this.deaths = deaths;
        }

        @SuppressWarnings("CloneDoesntCallSuperClone")
        @Override
        protected final Entry clone() {
            return new Entry(AP, kills, deaths);
        }
    }

    static {
        Packets.addLoaderAndRun(new Runnable() {
            @Override
            public void run() {
                Packets.regSP(SpInstanceScore.class, 0x79);
            }
        });
    }

    private class AddAppListener extends AbyssPointsService.ApAddCallback {
        @Override
        public void onApAdd(final Player player, VisibleObject vo, final int points, Class rewarder) {
            // async thread-safe points logic
            _proc.tell(new Runnable() {
                @Override
                public void run() {
                    // check if player is in instance
                    if (_rewards.containsKey(player.getObjectId())) {
                        if (points > 0) {
                            increasePoints(player, points);
                        } else if (points < 0) {
                            decreasePoints(player, points);
                        }
                    }
                }
            });
        }
    }

    private static class SpInstanceScore extends AionServerPacket {
        private final Entry _entry;
        private final long _remainingTimeMS;

        public SpInstanceScore(Entry entry, long remainingTimeMS) {
            _entry = entry.clone();
            _remainingTimeMS = remainingTimeMS;
        }

        @Override
        protected void writeImpl(AionConnection con) {
            writeD(300040000);
            writeD((int) _remainingTimeMS);
            writeD(InstanceScoreType.START_PROGRESS.getId());
            writeD(_entry.AP);
            writeD(_entry.kills);
            writeD(_entry.deaths);
            writeD(0);
        }
    }

    private static class SpawnObjCustomCond extends SpawnObjCond {
        public static final SpawnObjCustomCond STATIC = new SpawnObjCustomCond();

        @Override
        public Boolean onEvent(@NotNull Env env) {
            // do not allow while flying
            if (env.player.getFlyState() != 0) {
                env.player.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_USE_BINDSTONE_ITEM_WHILE_FLYING);
                return false;
            }

            // do not allow multiple kisks
            if (!CustomConfig.TOYPETSPAWN_NEW_KISK_SPAWN_ENABLE && KiskService.getInstance().haveKisk(env.player.getObjectId())) {
                env.player.sendPck(new SM_SYSTEM_MESSAGE(1390160));
                return false;
            }

            return true;
        }
    }
}
