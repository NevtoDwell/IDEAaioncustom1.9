/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.customrifts;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.DateUtil;
import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple3;
import com.ne.commons.services.CronService;
import com.ne.commons.utils.Actor;
import com.ne.commons.utils.ActorRef;
import com.ne.commons.utils.Callback;
import com.ne.commons.utils.Rnd;
import com.ne.commons.utils.xml.XmlUtil;
import com.ne.gs.controllers.effect.EffectController;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.RequestResponseHandler;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.modules.common.Item;
import com.ne.gs.modules.common.Pos;
import com.ne.gs.modules.common.TeleportHandler;
import com.ne.gs.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.utils.idfactory.IDFactory;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.NpcKnownList;

import static com.ne.gs.modules.common.TeleportHandler.TeleportCallback;

/**
 * @author hex1r0
 */
public final class CustomRiftManager extends Actor {

    /*Актор — это изолированный (нет прямых ссылок на экземпляр) объект, занимающийся асинхронной обработкой входящих сообщений.
    Сообщение — любой неизменяемый объект реализующий интерфейс Serializable. Они складируются в очередь,
    и когда система передаёт управление актору (об этом далее) — объекты извлекаются из очереди по принципу FIFO и обрабатываются согласно внутренней логике,
    которую мы должны явно задать. Акторы образуют древовидную иерархию, каждый из них имеет актора (или систему акторов) в качестве создателя.
    Чтобы отправить сообщение актору нужно иметь на него ссылку (не путать ссылкой на объект). Она бывает двух видов — ActorRef и ActorSelection.
    Ссылка имеет ключевой метод .tell(message, sender) отправляющий сообщение по указанному пути.
    Sender — это тоже ссылка типа ActorRef, именно её актор получатель получит при вызове метода getSender() при обработке сообщения.*/

    public static final ActorRef REF = ActorRef.of(new CustomRiftManager());

    /*Существует много технологий для организации параллельных вычислений, одна из наиболее перспективных и простых (да-да) — модель акторов.
    Она позволяет частично избавится от насущных проблем параллелизма, вроде состояния гонки, блокирующих ожиданий окончания операций,
    бесконечных мьютексов(взаимное исключение) и синхронизаций и многого иного. Так же подобный подход существенно облегчает распараллеливание кода.*/

    /*ActorRef — ссылка на конкретный, гарантированно существовавший (на момент создания ссылки)актор, аналог StrongReference.
    В явном виде ActorRef можно получить, вызвав метод getSender() при обработке сообщений, получив прямую ссылку на отправителя,
    или же при создании дочерних акторов. Для гарантированной доставки сообщений используется сообщение-ответ или агент, об этом далее.*/

    private static final Logger _log = LoggerFactory.getLogger(CustomRiftManager.class);

    /*Логирование*/

    private final Map<Integer, RiftSpawn> _riftSpawns = new THashMap<>();

    /*Интерфейс Map<K, V> представляет отображение или иначе говоря словарь, где каждый элемент представляет пару "ключ-значение".
    При этом все ключи уникальные в рамках объекта Map. Такие коллекции облегчают поиск элемента, если нам известен ключ - уникальный идентификатор объекта.*/

    private final Set<Integer> _spawnedUids = new THashSet<>();

    /*Что такое множество (Set)
    Как мы уже говорили, множество - это такой же способ хранения данных, как массив или список.
    Но особенность множества в том, что оно может хранить только уникальные значения.
    Например, если у нас есть множество Integer-ов - в нем лежат числа 1, 2 , 3, 4 и 5:
    Если мы попробуем добавить туда единицу, у нас не получится - потому что в множестве уже есть единица.
    В множествах Set каждый элемент хранится только в одном экземпляре.*/

    /* HashSet
    Название Hash... происходит от понятия хэш-функция. Хэш-функция — это функция, сужающая множество значений объекта до некоторого подмножества целых чисел.
    Класс Object имеет метод hashCode(), который используется классом HashSet для эффективного размещения объектов,
    заносимых в коллекцию. В классах объектов, заносимых в HashSet, этот метод должен быть переопределен (override).

    Имеет два основных конструктора (аналогично ArrayList):

                // Строит пустое множество
                public HashSet()

                // Строит множество из элементов коллекции
                public HashSet(Collection c)
    */

    private final List<JobDetail> _jobs = Lists.newArrayList();  /*создаётся массив из JobDetail*/

    /*Для создания простых списков применяется интерфейс List, который расширяет функцональность интерфейса Collection.

     JobDetail - тригер для заданий.Триггеры - это «механизм», с помощью которого планируются задания.
    Многие триггеры могут указывать на одно и то же задание, но один триггер может указывать только на одно задание.*/

    private CustomRiftManager() {
    }

    private void init() throws Exception {
        RiftSpawnList list;
        try {
            list = XmlUtil.loadXmlJAXB(RiftSpawnList.class, "./config/custom_data/custom_rifts/rift_spawns.xml");
        } catch (FileNotFoundException e) {
            list = XmlUtil.loadXmlJAXB(RiftSpawnList.class, "./data/static_data/custom_rifts/rift_spawns.xml");
        }

        for (Integer id : ImmutableSet.copyOf(_spawnedUids)) {
            despawnRift(id);
        }

        /*Проще говоря, метод ImmutableSet.copyOf () возвращает копию всех элементов в наборе
         ImmutableSet - неизменный набор
        * */

        for (JobDetail jobDetail : _jobs) {
            CronService.getInstance().cancel(jobDetail);
        }

        _riftSpawns.clear();
        for (RiftSpawn t : list) {
            _riftSpawns.put(t.getId(), t);
        }

        for (final RiftSpawn riftSpawn : list) {
            if (riftSpawn.getTime() == null) {
                spawnRifts(riftSpawn); /*когда <time> </time> в xml удалён портал спавнится*/
            } else {
                DateUtil.CronExpr from = riftSpawn.getTime().getFrom();
                DateUtil.CronExpr to = riftSpawn.getTime().getTo();

                if (DateUtil.cronBetween(from, to)) {
                    spawnRifts(riftSpawn);
                } else {
                    CronService.ScheduleResult sr = CronService.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            REF.tell(new Msg() {
                                @Override
                                public void run() {
                                    actor().spawnRifts(riftSpawn.getId());
                                }
                            });
                        }
                    }, from.toString());

                    _jobs.add(sr.getJobDetail());
                }
            }
        }
    }

    private void spawnRifts(Integer uid) {
        RiftSpawn riftSpawn = _riftSpawns.get(uid);
        if (riftSpawn != null) {
            spawnRifts(riftSpawn);
        }
    }

    private void spawnRifts(RiftSpawn riftSpawn) {
        if (riftSpawn.getSourceList() != null) {
            for (Pos pos : riftSpawn.getSourceList()) {
                spawnRift(riftSpawn, pos);
            }
        }

        //        if (riftSpawn.getTargetList() != null) {
        //            for (final Pos loc : riftSpawn.getTargetList())
        //                spawnRift(riftSpawn.getId(), loc);
        //        }
    }

    private void spawnRift(RiftSpawn riftSpawn, Pos pos) {
        _log.info("CustomRiftManager: Spawning rift " + riftSpawn.getId());

        NpcTemplate npcTpl = DataManager.NPC_DATA.getNpcTemplate(700143);
        SpawnTemplate spawnTpl = SpawnEngine.addNewSingleTimeSpawn(700143, pos);
        final Npc npc = new Npc(IDFactory.getInstance().nextId(), new RiftController(riftSpawn.getId()), spawnTpl, npcTpl);

        npc.setKnownlist(new NpcKnownList(npc));
        npc.setEffectController(new EffectController(npc));
        npc.setDesc(riftSpawn._info);

        World.getInstance().storeObject(npc);
        World.getInstance().setPosition(npc, 1, pos);
        World.getInstance().spawn(npc);

        _spawnedUids.add(npc.getObjectId());

        if (riftSpawn.getTime() != null) {
            CronService.ScheduleResult sr = CronService.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    REF.tell(new Msg() {
                        @Override
                        public void run() {
                            actor().despawnRift(npc.getObjectId());
                        }
                    });
                }
            }, riftSpawn.getTime().getTo().toString());

            _jobs.add(sr.getJobDetail());
        }
    }

    private void despawnRift(Integer npcId) {
        VisibleObject vo = World.getInstance().findVisibleObject(npcId);
        if (vo != null) {
            vo.getController().onDelete();
        }

        _spawnedUids.remove(npcId);
    }

    private void useRift(Integer uid, final Npc npc, final Player player) {
        final RiftSpawn riftSpawn = _riftSpawns.get(uid);
        if (riftSpawn == null) {
            return;
        }

        if (!riftSpawn.getRaceList().getRaces().contains(player.getRace())) {
            return;
        }

        Level level = riftSpawn.getLevel();
        if (level != null) {
            if (level.getMin() != 0 && player.getLevel() < level.getMin()) {
                player.sendMsg("Минимальный допустимый уровень: " + level.getMin());
                return;
            }
            if (level.getMax() != 0 && player.getLevel() > level.getMax()) {
                player.sendMsg("Максимальный допустимый уровень: " + level.getMax());
                return;
            }
        }

        RequestResponseHandler rrh = new RequestResponseHandler<Creature>(npc) {

            /*@SuppressWarnings("unchecked")*/
            @Override
            public void acceptRequest(Creature requester, Player responder) {
                if (riftSpawn.getTargetList() != null) {
                    Pos pos = Rnd.get(riftSpawn.getTargetList().getPositions());

                    TeleportCallback onTeleport = new TeleportCallback() {
                        @Override
                        public Object onEvent(@NotNull Tuple3<Player, Pos, TeleportCallback> e) {
                            if (riftSpawn.getItemList() != null) {
                                for (Item item : riftSpawn.getItemList()) {
                                    ItemService.addItem(player, item.getItemId(), item.getCount());
                                }
                            }
                            return null;
                        }
                    };

                    if (pos.getHandler() == null) {
                        TeleportHandler.SIMPLE.teleport(Tuple3.of(player, pos, onTeleport));
                    } else {
                        try {
                            Class<TeleportHandler> clazz = (Class<TeleportHandler>) Class.forName(pos.getHandler());
                            clazz.newInstance().teleport(Tuple3.of(player, pos, onTeleport));
                        } catch (Exception e) {
                            _log.error("", e);
                        }
                    }
                }
            }

            @Override
            public void denyRequest(Creature requester, Player responder) {
            }
        };

        boolean requested = player.getResponseRequester()
                                  .putRequest(SM_QUESTION_WINDOW.STR_ASK_PASS_BY_DIRECT_PORTAL, rrh);
        if (requested) {
            player.sendPck(new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_ASK_PASS_BY_DIRECT_PORTAL, 0, 0));
        }
    }

    private static abstract class Msg extends Message<CustomRiftManager> {}

    public static class Init extends Msg {

        private static final String LOADING = "CustomRiftManager: Loading custom rift spawns";
        private static final String SUCCESS = "CustomRiftManager: Custom rift spawns loaded successfully";
        private static final String FAILURE = "CustomRiftManager: Error while loading custom rift spawns";

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

    public static class UseRift extends Msg {

        private final Integer _uid;
        private final Npc _npc;
        private final Player _player;

        public UseRift(Integer uid, Npc npc, Player player) {
            _uid = uid;
            _npc = npc;
            _player = player;
        }

        @Override
        public void run() {
            actor().useRift(_uid, _npc, _player);
        }
    }
}
