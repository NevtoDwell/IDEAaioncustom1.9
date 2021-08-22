package com.ne.gs.modules.customspawner;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import gnu.trove.set.hash.THashSet;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.DateUtil;
import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.services.CronService;
import com.ne.commons.utils.Actor;
import com.ne.commons.utils.ActorRef;
import com.ne.commons.utils.Callback;
import com.ne.commons.utils.Chainer;
import com.ne.commons.utils.Rnd;
import com.ne.commons.utils.xml.XmlUtil;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.handlers.CmdReloadHandler;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.modules.common.NpcSpawn;
import com.ne.gs.modules.common.Pos;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.world.World;

/**
 * @author hex1r0
 */
public final class CustomSpawner extends Actor {
    private static final String FILENAME = "custom_spawns/custom_spawns.xml";

    public static final ActorRef REF = ActorRef.of(new CustomSpawner());
    private static final Logger _log = LoggerFactory.getLogger(CustomSpawner.class);

    private final Set<Integer> _spawnedIds = new THashSet<>();
    private final List<JobDetail> _jobs = Lists.newArrayList();

    public static CronService cron() {
        return CronService.getInstance();
    }

    private void init() throws Exception {
        NpcSpawnList list;
        try {
            list = XmlUtil.loadXmlJAXB(NpcSpawnList.class, "./config/custom_data/" + FILENAME);
        } catch (FileNotFoundException e) {
            list = XmlUtil.loadXmlJAXB(NpcSpawnList.class, "./data/static_data/" + FILENAME);
        }

        for (Integer id : ImmutableSet.copyOf(_spawnedIds)) {
            despawnNpc(id);
        }

        for (JobDetail jobDetail : _jobs) {
            cron().cancel(jobDetail);
        }

        _jobs.clear();

        for (final NpcSpawn spawn : list.getSpawns()) {
            if (spawn.getTime() == null) {
                spawnNpcs(spawn);
            } else {
                DateUtil.CronExpr from = spawn.getTime().getFrom();
                DateUtil.CronExpr to = spawn.getTime().getTo();

                if (DateUtil.cronBetween(from, to)) {
                    spawnNpcs(spawn);
                } else {
                    CronService.ScheduleResult sr = cron().schedule(new Runnable() {
                        @Override
                        public void run() {
                            REF.tell(new Msg() {
                                @Override
                                public void run() {
                                    spawnNpcs(spawn);
                                }
                            });
                        }
                    }, from.toString());

                    _jobs.add(sr.getJobDetail());
                }
            }
        }
    }

    private void spawnNpcs(NpcSpawn spawn) {
        if (spawn.getPositions() != null) {
            switch (spawn.getPositions().getPickPolicy()) {
                case ALL:
                    for (Pos pos : spawn.getPositions()) {
                        spawnNpc(spawn, pos);
                    }
                    break;
                case ANY:
                    Pos pos = Rnd.get(spawn.getPositions().getPositions());
                    spawnNpc(spawn, pos);
                    break;
            }
        }
    }

    private void spawnNpc(NpcSpawn spawn, Pos pos) {
        SpawnTemplate stpl = SpawnEngine.addNewSpawn(
            pos.getMapId(), spawn.getId(), pos.getX(), pos.getY(), pos.getZ(),
            (byte) pos.getH(), spawn.getRespawn());
        final VisibleObject vo = SpawnEngine.spawnObject(stpl, 1);

        _spawnedIds.add(vo.getObjectId());

        if (spawn.getTime() != null) {
            CronService.ScheduleResult sr = cron().schedule(new Runnable() {
                @Override
                public void run() {
                    REF.tell(new Msg() {
                        @Override
                        public void run() {
                            despawnNpc(vo.getObjectId());
                        }
                    });
                }
            }, spawn.getTime().getTo().toString());

            _jobs.add(sr.getJobDetail());
        }

        _log.info("CustomSpawner: spawned npc=" + spawn.getId() + " uid=" + vo.getObjectId());
    }

    private void despawnNpc(Integer npcUid) {
        VisibleObject vo = World.getInstance().findVisibleObject(npcUid);
        if (vo != null) {
            vo.getController().onDelete();
            if (vo instanceof Npc) {
                _log.info("CustomSpawner: despawned npc=" + ((Npc) vo).getNpcId() + " uid=" + vo.getObjectId());
            } else {
                _log.info("CustomSpawner: despawned object uid=" + vo.getObjectId());
            }
        }

        _spawnedIds.remove(npcUid);
    }

    private static abstract class Msg extends Actor.Message<CustomSpawner> {}

    public static class Init extends Msg {
        static {
            Chainer.GLOBAL.attach(new Reload());
        }

        private static final String LOADING = "CustomSpawner: loading custom spawns";
        private static final String SUCCESS = "CustomSpawner: custom spawns loaded";
        private static final String FAILURE = "CustomSpawner: error while loading custom spawns";
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

    private static class Reload extends CmdReloadHandler {
        @Override
        public Boolean onEvent(@NotNull final Tuple2<Player, String[]> e) {
            if (e._2.length > 0 && e._2[0].equalsIgnoreCase("customspawner")) {
                CustomSpawner.REF.tell(new Init(new Callback<String, Object>() {
                    @Override
                    public Object onEvent(@NotNull String msg) {
                        e._1.sendMsg(msg);
                        return null;
                    }
                }));
                return true; // break
            }
            return false; // continue
        }
    }
}
