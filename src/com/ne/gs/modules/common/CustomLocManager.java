/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.common;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Longs;
import com.ne.gs.model.Race;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.DateUtil;
import com.ne.commons.func.Each;
import com.ne.commons.func.tuple.Tuple3;
import com.ne.commons.services.CronService;
import com.ne.commons.utils.Actor;
import com.ne.commons.utils.ActorRef;
import com.ne.commons.utils.Callback;
import com.ne.commons.utils.Rnd;
import com.ne.commons.utils.xml.XmlUtil;
import com.ne.gs.instance.InstanceEngine;
import com.ne.gs.instance.handlers.InstanceHandler;
import com.ne.gs.model.gameobjects.Kisk;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.modules.customrifts.RiftPos;
import com.ne.gs.modules.pvpevent.PvpLocTemplate;
import com.ne.gs.network.aion.SystemMessageId;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.instance.InstanceService;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.World;
import com.ne.gs.world.WorldMap;
import com.ne.gs.world.WorldMap2DInstance;
import com.ne.gs.world.WorldMap3DInstance;
import com.ne.gs.world.WorldMapInstance;
import com.ne.gs.world.WorldMapType;
import com.ne.gs.world.knownlist.Visitor;

import static com.ne.gs.modules.common.TeleportHandler.TeleportCallback;

/**
 * @author hex1r0
 */
public class CustomLocManager extends Actor {

    private static final Logger _log = LoggerFactory.getLogger(CustomLocManager.class);

    private static final ActorRef _instance = ActorRef.of(new CustomLocManager());

    private final Map<String, CustomLocTemplate> _templates = new THashMap<>();
    private final Map<String, WorldMapInstance> _channels = new THashMap<>();
    private final Set<Integer> _spawnedObjectsUids = new THashSet<>();
    private final List<JobDetail> _jobs = new ArrayList<>();

    private final Ordering<Time> BY_TIME = new Ordering<Time>() {
        public int compare(Time left, Time right) {
            return Longs.compare(
                    DateUtil.cronAfter(left.getFrom()).getTime(),
                    DateUtil.cronAfter(right.getFrom()).getTime());
        }
    };

    public static Logger log() {
        return _log;
    }

    public static CronService cron() {
        return CronService.getInstance();
    }

    public static CronService.ScheduleResult schedule(final Runnable r, String cronExpression) {
        return cron().schedule(new Runnable() {
            @Override
            public void run() {
                getInstance().tell(r);
            }
        }, cronExpression);
    }

    public static ScheduledFuture<?> schedule(final Runnable r, long delayMs) {
        return ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                getInstance().tell(r);
            }
        }, delayMs);
    }

    public static ActorRef getInstance() {
        return _instance;
    }

    private CustomLocManager() {
    }

    private void init() throws Exception {
        Collection<File> files = XmlUtil.listFiles("./config/custom_data/custom_loc/");
        CustomLocList customLocs = new CustomLocList();
        for (File file : files) {
            try {
                CustomLocList t = XmlUtil.loadXmlJAXB(CustomLocList.class, file);
                customLocs.getLocs().addAll(t.getLocs());
            } catch (Exception e) {
                log().error("Error while loading " + file, e);
            }
        }

        Set<String> duplicateIds = new HashSet<>(customLocs.getLocs().size());
        for (CustomLocTemplate tpl : ImmutableList.copyOf(customLocs.getLocs())) {
            if (!duplicateIds.add(tpl.getId())) {
                _log.warn("Duplicate locId=" + tpl.getId());
                customLocs.getLocs().remove(tpl);
            }
        }

        for (Integer id : ImmutableSet.copyOf(_spawnedObjectsUids)) {
            despawnNpc(id);
        }

        for (JobDetail jobDetail : _jobs) {
            cron().cancel(jobDetail);
        }

        _jobs.clear();

        for (Map.Entry<String, WorldMapInstance> e : ImmutableSet.copyOf(_channels.entrySet())) {
            deleteChannel(e.getKey(), e.getValue());
        }

        _channels.clear();
        _templates.clear();

        for (CustomLocTemplate tpl : customLocs) {
            _templates.put(tpl.getId(), tpl);
        }

        _pvpLocs.clear();
        for (CustomLocTemplate tpl : customLocs) {
            // FIXME temp solution
            if (tpl instanceof PvpLocTemplate) {
                enqueuePvpLoc((PvpLocTemplate) tpl);
            } else {
                scheduleCustomLoc(tpl);
            }
        }

        scheduleNextPvpLoc();
    }

    // ---------------------------------------------------------
    // ---------------------------------------------------------
    private List<PvpLocTemplate> _pvpLocs = new ArrayList<>();
    private Set<PvpLocTemplate> _alreadyRun = new THashSet<>();

    private void enqueuePvpLoc(PvpLocTemplate tpl) {
        _pvpLocs.add(tpl);
    }

    // ordering by time
    private final Ordering<PvpLocTemplate> LOCS_BY_TIME = new Ordering<PvpLocTemplate>() {
        public int compare(PvpLocTemplate o1, PvpLocTemplate o2) {
            List<Time> t1 = o1.getTimeList().getTimes();
            List<Time> t2 = o2.getTimeList().getTimes();
            if (t1.isEmpty() && t2.isEmpty()) {
                return 0;
            }

            if (t1.isEmpty()) {
                return -1;
            }

            if (t2.isEmpty()) {
                return 1;
            }

            return BY_TIME.compare(findSoonerTime(t1), findSoonerTime(t2));
        }
    };

    private void scheduleNextPvpLoc() {
        // try to find a schedule that satisfies 'now'
        PvpLocTemplate loc = null;
        List<PvpLocTemplate> canRunNow = findLocsCanRunNow();
        Collections.shuffle(canRunNow);
        for (PvpLocTemplate loc0 : canRunNow) {
            if (_alreadyRun.contains(loc0)) {
                continue;
            }

            loc = loc0;
            break;
        }

        if (loc == null) {
            if (canRunNow.isEmpty() && !_pvpLocs.isEmpty()) {
                loc = LOCS_BY_TIME.min(_pvpLocs);
            } else if (!canRunNow.isEmpty()) {
                _alreadyRun.clear();
                loc = Rnd.get(canRunNow);
            }
        }

        if (loc != null) {
            scheduleCustomLoc(loc);
            _alreadyRun.add(loc);
        }
    }

    private List<PvpLocTemplate> findLocsCanRunNow() {
        List<PvpLocTemplate> locs = new ArrayList<>();

        for (PvpLocTemplate loc : _pvpLocs) {
            List<Time> timeList = loc.getTimeList().getTimes();
            if (timeList.isEmpty()) {
                locs.add(loc);
            } else {
                for (Time time : timeList) {
                    if (DateUtil.cronBetween(time.getFrom(), time.getTo())) {
                        locs.add(loc);
                        break;
                    }
                }
            }
        }

        return locs;
    }

    //    private PvpLocTemplate findLocCanRunNowOrSoon() {
//        for (PvpLocTemplate loc : _queue) {
//            List<Time> timeList = loc.getTimeList().getTimes();
//            if (timeList.isEmpty()) {
//                return loc;
//            } else {
//                for (Time time : timeList) {
//                    if (DateUtil.cronBetween(time.getFrom(), time.getTo())) {
//                        return loc;
//                    }
//                }
//            }
//        }
//
//        return LOCS_BY_TIME.min(_queue);
//    }
//
//    private List<PvpLocTemplate> findLocsCanRunNow() {
//        List<PvpLocTemplate> locs = new ArrayList<>();
//
//        for (PvpLocTemplate loc : _alreadyRun) {
//            List<Time> timeList = loc.getTimeList().getTimes();
//            if (timeList.isEmpty()) {
//                locs.add(loc);
//            } else {
//                for (Time time : timeList) {
//                    if (DateUtil.cronBetween(time.getFrom(), time.getTo())) {
//                        locs.add(loc);
//                        break;
//                    }
//                }
//            }
//        }
//
//        return locs;
//    }
    // ---------------------------------------------------------
    // ---------------------------------------------------------
    private void scheduleCustomLoc(final CustomLocTemplate tpl) {
        if (tpl.getTimeList().getTimes().isEmpty()) {
            spawnCustomLoc(tpl, null);
        } else {
            boolean spawned = false;
            for (Time time : tpl.getTimeList()) {
                DateUtil.CronExpr from = time.getFrom();
                DateUtil.CronExpr to = time.getTo();

                if (DateUtil.cronBetween(from, to)) {
                    spawnCustomLoc(tpl, time);
                    spawned = true;
                    break;
                }
            }

            if (!spawned) {
                final Time time = findSoonerTime(tpl.getTimeList());
                CronService.ScheduleResult sr = schedule(new Msg() {
                    @Override
                    public void run() {
                        spawnCustomLoc(tpl.getId(), time);
                    }
                }, time.getFrom().toString());

                _jobs.add(sr.getJobDetail());

                _log.info("CustomLocManager: scheduled loc=" + tpl.getId() + " date=" + sr.getDate());
            }
        }
    }

    private void spawnCustomLoc(String locId, Time time) {
        CustomLocTemplate tpl = _templates.get(locId);
        if (tpl != null) {
            spawnCustomLoc(tpl, time);
        }
    }

    private void spawnCustomLoc(final CustomLocTemplate tpl, Time time) {
        _channels.put(tpl.getId(), createChannel(tpl));
        _log.info("CustomLocManager: spawned loc id=" + tpl.getId());

        for (NpcSpawnList spawnList : tpl) {
            for (final NpcSpawn spawn : spawnList) {
                if (spawn.getTime() == null) {
                    spawnNpcs(tpl.getId(), spawn);
                } else {
                    DateUtil.CronExpr from = spawn.getTime().getFrom();
                    DateUtil.CronExpr to = spawn.getTime().getTo();

                    if (DateUtil.cronBetween(from, to)) {
                        spawnNpcs(tpl.getId(), spawn);
                    } else {
                        CronService.ScheduleResult sr = schedule(new Msg() {
                            @Override
                            public void run() {
                                spawnNpcs(tpl.getId(), spawn);
                            }
                        }, from.toString());

                        _jobs.add(sr.getJobDetail());
                    }
                }
            }
        }

        Date expiresAt = scheduleExpire(tpl, time);
        WorldMapInstance ch = initChannelScript(tpl, expiresAt);
        // notify that location is ready
        final CustomLocScript script = ((CustomLocScript) ch.getInstanceHandler());
        script.getProcessor().tell(new Runnable() {
            @Override
            public void run() {
                script.onReady();
            }
        });
    }

    private Date scheduleExpire(final CustomLocTemplate tpl, Time time) {
        Date expiresAt = new Date();
        if (time != null) {
            CronService.ScheduleResult sr = schedule(new Msg() {
                @Override
                public void run() {
                    expire(tpl.getId());
                }
            }, time.getTo().toString());

            expiresAt = sr.getDate();
            _jobs.add(sr.getJobDetail());
        }

        return expiresAt;
    }

    @SuppressWarnings("unchecked")
    private WorldMapInstance initChannelScript(CustomLocTemplate tpl, Date expiresAt) {
        WorldMapInstance ch = _channels.get(tpl.getId());
        if (tpl.getHandler() == null) {
            ch.setInstanceHandler(new CustomLocScript(tpl, expiresAt));
        } else {
            try {
                Class<InstanceHandler> clazz = (Class<InstanceHandler>) Class.forName(tpl.getHandler());
                Constructor<InstanceHandler> ctor = (Constructor<InstanceHandler>) clazz.getConstructors()[0];
                InstanceHandler ih = ctor.newInstance(tpl, expiresAt);
                ch.setInstanceHandler(ih);
            } catch (Exception e) {
                ch.setInstanceHandler(new CustomLocScript(tpl, expiresAt));
                _log.error("", e);
            }
        }

        InstanceEngine.getInstance().onInstanceCreate(ch);

        return ch;
    }

    @SuppressWarnings("unchecked")
    private WorldMapInstance createChannel(CustomLocTemplate tpl) {
        int mapId = tpl.getMapId();
        WorldMap map = World.getInstance().getWorldMap(mapId);

        if (!map.isInstanceType()) {
            throw new UnsupportedOperationException("Invalid call for next available instance  of " + mapId);
        }

        int id = map.getNextInstanceId();

        WorldMapInstance channel;
        if (mapId == WorldMapType.RESHANTA.getId()) {
            channel = new WorldMap3DInstance(map, id);
        } else {
            channel = new WorldMap2DInstance(map, id, 0);
        }

        channel.setInstanceHandler(new CustomLocScript(tpl, new Date())); // fake script, real is set on spawn
        map.addInstance(id, channel);

        _log.info("CustomLocManager: created channel for tpl=" + tpl.getId() + " channelId=" + id);

        return channel;
    }

    private Time findSoonerTime(Iterable<Time> times) {
        return BY_TIME.min(times);
    }

    private void spawnNpcs(String locId, NpcSpawn spawn) {
        if (spawn.getPositions() != null) {
            switch (spawn.getPositions().getPickPolicy()) {
                case ALL:
                    for (Pos pos : spawn.getPositions()) {
                        spawnNpc(locId, spawn, pos);
                    }
                    break;
                case ANY:
                    Pos pos = Rnd.get(spawn.getPositions().getPositions());
                    spawnNpc(locId, spawn, pos);
                    break;
            }
        }
    }

    private void spawnNpc(String locId, NpcSpawn spawn, Pos pos) {
        WorldMapInstance channel = _channels.get(locId);
        pos.setMapId(channel.getMapId());
        SpawnTemplate stpl = SpawnEngine.addNewSpawn(spawn.getId(), pos, spawn.getRespawn());
        final VisibleObject vo = SpawnEngine.spawnObject(stpl, channel.getInstanceId());

        _spawnedObjectsUids.add(vo.getObjectId());

        if (spawn.getTime() != null) {
            CronService.ScheduleResult sr = schedule(new Msg() {
                @Override
                public void run() {
                    despawnNpc(vo.getObjectId());
                }
            }, spawn.getTime().getTo().toString());

            _jobs.add(sr.getJobDetail());
        }

        _log.info("CustomLocManager: spawned npc=" + spawn.getId() + " uid=" + vo.getObjectId());
    }

    private void despawnNpc(Integer npcId) {
        VisibleObject vo = World.getInstance().findVisibleObject(npcId);
        if (vo != null) {
            vo.getController().onDelete();
            if (vo instanceof Npc) {
                _log.info("CustomLocManager: despawned npc=" + ((Npc) vo).getNpcId() + " uid=" + vo.getObjectId());
            } else {
                _log.info("CustomLocManager: despawned object uid=" + vo.getObjectId());
            }
        }

        _spawnedObjectsUids.remove(npcId);
    }

    private CustomLocScript getScript(WorldMapInstance ch) {
        return (CustomLocScript) ch.getInstanceHandler();
    }

    private void teleport(Tuple3<Player, Pos, TeleportCallback> e) {
        Player player = e._1;
        RiftPos pos = (RiftPos) e._2;
        TeleportCallback tc = e._3;

        if (!checkEntryTime(e)) {
            player.sendMsg("Локация на данный момент не доступна!");
            return;
        }

        WorldMapInstance channel = _channels.get(pos.getLocId());
        if (!getScript(channel).canEnter(player)) {
            return;
        }
        pos.setMapId(channel.getMapId()); // just to make sure the map id is correct

        CustomLocTemplate tpl = _templates.get(pos.getLocId());
        int raceRestriction = tpl.race_restriction;
        if (raceRestriction > -1) {

            int els = channel.playersCount(Race.ELYOS);
            int asms = channel.playersCount(Race.ASMODIANS);

            if ((player.getRace() == Race.ELYOS && els - asms > raceRestriction) || (player.getRace() == Race.ASMODIANS && asms - els > raceRestriction)) {
                player.sendMsg("Не возможно принять участие - у вашей расы явное количественное преимущество!");
                return;
            }
        }

        int maxPlayers = tpl.max_players;
        if(maxPlayers > 0 && channel.playersCount() >= maxPlayers)
        {
            player.sendMsg("Не возможно принять участие - участвует максимальное количество игроков!");
            return;
        }

        int channelId = channel.getInstanceId();
        InstanceService.registerPlayerWithInstance(channel, player);

        TeleportService.teleportBeam(player, channelId, pos);
        tc.onEvent(Tuple3.of(player, (Pos) pos, tc));
    }

    private void send(String locId, final String messageId, final Object[] args) {
        CustomLocTemplate tpl = _templates.get(locId);

        if (tpl == null) {
            _log.error("CustomLocManager: missing loc=" + locId + " provided");
            return;
        }

        WorldMapInstance channel = _channels.get(locId);
        if (channel == null) {
            _log.error("CustomLocManager: missing channel=" + locId + " provided");
            return;
        }

        final CustomLocScript script = ((CustomLocScript) channel.getInstanceHandler());
        script.getProcessor().tell(new Runnable() {
            @Override
            public void run() {
                script.onRecv(messageId, args);
            }
        });
    }

    //    private void broadcast(final String messageId, final Object[] args) {
//        for (CustomLocTemplate tpl : _templates.values()) {
//            WorldMapInstance channel = _channels.get(tpl.getId());
//            final CustomLocScript script = ((CustomLocScript) channel.getInstanceHandler());
//            script.getProcessor().tell(new Runnable() {
//                @Override
//                public void run() {
//                    script.onRecv(messageId, args);
//                }
//            });
//        }
//    }
    private void foreach(Each<CustomLocTemplate> func) {
        for (CustomLocTemplate tpl : _templates.values()) {
            func.apply(tpl);
        }
    }

    private boolean checkEntryTime(Tuple3<Player, Pos, TeleportCallback> e) {
        RiftPos pos = (RiftPos) e._2;
        CustomLocTemplate tpl = _templates.get(pos.getLocId());
        if (tpl == null) {
            return false;
        }

        if (tpl.getTimeList().getTimes().isEmpty()) {
            return true;
        }

        for (Time time : tpl.getTimeList()) {
            if (DateUtil.cronBetween(time.getFrom(), time.getTo())) {
                return true;
            }
        }

        return false;
    }

    private void deleteChannel(String locId, WorldMapInstance channel) {
        channel.doOnAllPlayers(new Visitor<Player>() {
            @Override
            public void visit(Player p) {
                Kisk kisk = p.getKisk();
                if (kisk != null) {
                    kisk.getController().onDelete();
                }
                p.sendPck(new SM_SYSTEM_MESSAGE(SystemMessageId.LEAVE_INSTANCE_NOT_PARTY));
                TeleportService.moveToInstanceExit(p, p.getWorldId(), p.getRace());
            }
        });

        Iterator<VisibleObject> it = channel.objectIterator();
        while (it.hasNext()) {
            VisibleObject o = it.next();
            o.getController().onDelete();
            if (o instanceof Npc) {
                _log.info("CustomLocManager: despawned npc=" + ((Npc) o).getNpcId() + " uid=" + o.getObjectId());
            } else {
                _log.info("CustomLocManager: despawned object uid=" + o.getObjectId());
            }
        }

        channel.getInstanceHandler().destroy();
        channel.getParent().removeWorldMapInstance(channel.getInstanceId());

        _channels.remove(locId);
    }

    private void expire(String locId) {
        CustomLocTemplate tpl = _templates.get(locId);
        if (tpl != null) {
            _log.info("CustomLocManager: loc=" + tpl.getId() + " expired");

            WorldMapInstance channel = _channels.get(tpl.getId());
            if (channel == null) {
                return;
            }

            deleteChannel(tpl.getId(), channel);

            if (tpl instanceof PvpLocTemplate) {
                scheduleNextPvpLoc();
            } else {
                scheduleCustomLoc(tpl);
            }
        }
    }

    private static abstract class Msg extends Actor.Message<CustomLocManager> {
    }

    public static class Init extends Msg {

        private static final String LOADING = "CustomLocManager: loading custom locs";
        private static final String SUCCESS = "CustomLocManager: custom locs loaded";
        private static final String FAILURE = "CustomLocManager: error while loading custom locs";
        private final Callback _callback;

        public Init() {
            this(Callback.DUMMY);
        }

        public Init(Callback callback) {
            _callback = callback;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            _log.info(LOADING);
            _callback.onEvent(LOADING);
            try {
                actor().init();
                _log.info(SUCCESS);
                _callback.onEvent(SUCCESS);
            } catch (Exception e) {
                _log.error(FAILURE, e);
                _callback.onEvent(FAILURE + " " + e.getMessage());
            }
        }

    }

    public static class Teleport extends Msg {

        private final Tuple3<Player, Pos, TeleportCallback> _e;

        public Teleport(Tuple3<Player, Pos, TeleportCallback> e) {
            _e = e;
        }

        @Override
        public void run() {
            actor().teleport(_e);
        }
    }

    public static class SendMsg extends Msg {

        private final String _customLocId;
        private final String _messageId;
        private final Object[] _args;

        public SendMsg(String customLocId, String messageId, Object... args) {
            _customLocId = customLocId;
            _messageId = messageId;
            _args = args;
        }

        @Override
        public void run() {
            actor().send(_customLocId, _messageId, _args);
        }
    }

    public static class Foreach extends Msg {

        private final Each<CustomLocTemplate> _func;

        public Foreach(Each<CustomLocTemplate> func) {
            _func = func;
        }

        @Override
        public void run() {
            actor().foreach(_func);
        }
    }

    public static class Delete extends Msg {

        private final String _locId;

        public Delete(String locId) {
            _locId = locId;
        }

        @Override
        public void run() {
            actor().expire(_locId);
        }
    }
}
