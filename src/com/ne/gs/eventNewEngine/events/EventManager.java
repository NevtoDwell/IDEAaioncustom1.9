/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.eventNewEngine.events;

import com.ne.commons.services.CronService;
import com.ne.gs.eventNewEngine.events.enums.EventPlayerLevel;
import com.ne.gs.eventNewEngine.events.enums.EventRergisterState;
import com.ne.gs.eventNewEngine.events.enums.EventType;
import static com.ne.gs.eventNewEngine.events.enums.EventType.E_1x1;
import static com.ne.gs.eventNewEngine.events.enums.EventType.E_2x2;
import static com.ne.gs.eventNewEngine.events.enums.EventType.E_3x3;
import static com.ne.gs.eventNewEngine.events.enums.EventType.E_4x4;
import static com.ne.gs.eventNewEngine.events.enums.EventType.E_6x6;
import static com.ne.gs.eventNewEngine.events.enums.EventType.E_LHE;
import com.ne.gs.eventNewEngine.events.holders.IEventHolder;
import com.ne.gs.eventNewEngine.events.holders.PlayerGroupEventHolder;
import com.ne.gs.eventNewEngine.events.holders.SimpleSinglePlayerEventHolder;
import com.ne.gs.eventNewEngine.events.holders.SinglePlayerHolder;
import com.ne.gs.eventNewEngine.events.holders._1x1SamePlayerClassEventHolder;
import com.ne.gs.eventNewEngine.events.xml.EventStartPosition;
import com.ne.gs.eventNewEngine.events.xml.EventStartPositionList;
import com.ne.gs.eventNewEngine.utils.TimeUtil;
import com.ne.gs.model.TeleportAnimation;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.model.team2.group.PlayerGroupService;
import com.ne.gs.services.instance.InstanceService;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.utils.ColorUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.World;
import com.ne.gs.world.WorldMapInstance;
import com.ne.gs.world.knownlist.Visitor;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author userd
 */
public class EventManager {

    private final Logger log = LoggerFactory.getLogger(EventManager.class);
    private final Map<EventType, Map<Integer, PlayerInfo>> playedPlayers;
    private final List<IEventHolder> holders;
    private final Map<EventType, Boolean> events;
    private final Map<EventType, ScheduledFuture> endRegTasks;
    private boolean launcheAnnounce = false;
    private int holderCounter = 0;

    private EventManager() {
        this.events = new EnumMap<>(EventType.class);
        this.endRegTasks = new EnumMap<>(EventType.class);
        this.playedPlayers = new EnumMap<>(EventType.class);
        this.holders = new FastList<>();
    }

    public void onEnterWorldAnnounce(final Player player) {
        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if (hasActiveEvents()) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("==============================\n");
                    sb.append("Открыта регистрация на ивенты:\n");
                    for (Map.Entry<EventType, Boolean> entry : events.entrySet()) {
                        if (entry.getValue()) {
                            sb.append("- ").append(ColorUtil.convertFromUTF8(entry.getKey().getEventTemplate().getEventName())).append("\n");
                        }
                    }
                    sb.append("Регистрация командой: .event\n").append("==============================");
                    PacketSendUtility.sendMessage(player, sb.toString());
                }
            }
        }, 3000);
    }

    public void Init() {
        for (final EventType et : EventType.values()) {
            if (!et.IsDone()) {
                continue;
            }
            this.events.put(et, false);
            this.playedPlayers.put(et, new FastMap<Integer, PlayerInfo>().shared());
            CronService.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    RunEvent(et);
                }
            }, et.getEventTemplate().getSchedule());
        }
    }

    public boolean RunEvent(final EventType et) {
        if (this.eventIsActive(et)) {
            return false;
        }
        changeEventActiveState(et, true);
        switch (et) {
            // Regular type
            case E_1x1:
            case E_2x2:
            case E_3x3:
            case E_4x4:
            case E_6x6:
            case E_LHE:
                this.endRegTasks.put(et, ThreadPoolManager.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        changeEventActiveState(et, false);
                        endRegTasks.remove(et);
                    }
                }, et.getEventTemplate().getRegistrationTime() * 1000));
                break;
        }
        return true;
    }

    public synchronized EventRegisterInfo register(Player player, EventType etype) {
        switch (etype) {
            case E_1x1:
            case E_LHE:
                return this.registerSinglePlayer(player, etype);
            case E_2x2:
            case E_3x3:
            case E_4x4:
            case E_6x6:
                return this.registerPlayerGroup(player, etype);
            default:
                return new EventRegisterInfo(EventRergisterState.ERROR, "[ERROR_0001] Неизвестный тип ивента.");
        }
    }

    private EventRegisterInfo registerSinglePlayer(Player player, EventType etype) {
        EventRegisterInfo eri = this.standartCheck(etype);
        if (eri != null) {
            return eri;
        }

        if (this.containsInHolders(player)) {
            return new EventRegisterInfo(EventRergisterState.ALREADY_REGISTRED, "Вы уже зарегистрированы ивенте. Если вы хотите зарегистрироваться в другом ивенте, то сначала выйдите из текущего.");
        }

        eri = this.checkInPlayed(player, etype);
        if (eri != null) {
            return eri;
        }

        byte lvl = player.getLevel();
        EventPlayerLevel epl = EventPlayerLevel.getEventLevelByPlayerLevel(lvl);

        String registerMsg;
        IEventHolder holder = this.getFirstFreeHolderForSinglePlayer(player, etype, epl);
        if (holder == null) {
            holderCounter += 1;
            holder = this.createEventHolder(holderCounter, etype, epl);
            holder.addPlayer(player);
            this.holders.add(holder);
            registerMsg = String.format("Вы успешно зарегистрированы в новую группу регистратора.\nИвент: %s\nУровни: %s - %s", etype.name(), epl.getMin(), epl.getMax());
            log.info("[EventManager] Create new {} with index: {}", holder.getClass().getName(), holder.Index());
            log.info("[EventManager] Add {} to {} with index: {}", new Object[]{player.getName(), holder.getClass().getSimpleName(), holder.Index()});
        } else {
            holder.addPlayer(player);
            registerMsg = String.format("Вы зарегистрированны в ожидающую группу регистратора.\nИвент: %s\nУровни: %s - %s", etype.name(), epl.getMin(), epl.getMax());
            log.info("[EventManager] Add {} to {} with index: {}", new Object[]{player.getName(), holder.getClass().getSimpleName(), holder.Index()});
        }
        this.tryStartEvent(holder);

        return new EventRegisterInfo(EventRergisterState.REGISTRED, registerMsg);
    }

    private EventRegisterInfo registerPlayerGroup(Player leader, EventType etype) {
        if (!leader.isInGroup2()) {
            return new EventRegisterInfo(EventRergisterState.ERROR, "Вы должны быть в группе и быть её лидером.");
        }
        PlayerGroup group = leader.getPlayerGroup2();
        if (!group.isLeader(leader)) {
            return new EventRegisterInfo(EventRergisterState.PLAYER_IS_NOT_GROUP_LEADER, "Вы не лидер группы, вы не можете производить регистрацию группы.");
        }
        EventRegisterInfo eri = this.standartCheck(etype);
        if (eri != null) {
            return eri;
        }
        for (Player member : group.getMembers()) {
            if (this.containsInHolders(member)) {
                return new EventRegisterInfo(EventRergisterState.ALREADY_REGISTRED,
                        "Один или несколько игроков группы уже зарегистрированы в этом ивенте или других ивентах.");
            }
        }
        eri = this.checkInPlayedGroup(group, etype);
        if (eri != null) {
            return eri;
        }

        byte lvl = this.getHigherLvlInGroup(group);
        EventPlayerLevel epl = EventPlayerLevel.getEventLevelByPlayerLevel(lvl);
        String registerMsg;
        IEventHolder holder = this.getFirstFreeHolderForPlayerGroup(group, etype, epl);

        if (holder == null) {
            holderCounter += 1;
            holder = this.createEventHolder(holderCounter, etype, epl);
            holder.addPlayerGroup(group);
            this.holders.add(holder);
            registerMsg = String.format("Ваша группа зарегистрирована в новый список регистратора.\nИвент: %s\nУровни: %s - %s", etype.name(), epl.getMin(), epl.getMax());
            log.info("[EventManager] Create new {} with index: {}", holder.getClass().getName(), holder.Index());
            log.info("[EventManager] Add playerGroup Leader {} to {} with index: {}", new Object[]{leader.getName(), holder.getClass().getSimpleName(), holder.Index()});
        } else {
            holder.addPlayerGroup(group);
            registerMsg = String.format("Ваша группа зарегистрирована в список ожидания регистратора.\nИвент: %s\nУровни: %s - %s", etype.name(), epl.getMin(), epl.getMax());
            log.info("[EventManager] Add playerGroup Leader {} to {} with index: {}", new Object[]{leader.getName(), holder.getClass().getSimpleName(), holder.Index()});
        }
        this.tryStartEvent(holder);

        return new EventRegisterInfo(EventRergisterState.REGISTRED, registerMsg);
    }

    public synchronized EventRegisterInfo unregister(Player player, EventType etype) {
        switch (etype) {
            case E_1x1:
            case E_LHE:
                return this.unregisterSinglePlayer(player, etype);
            case E_2x2:
            case E_3x3:
            case E_4x4:
            case E_6x6:
                return this.unregisterPlayerGroup(player);
            default:
                return new EventRegisterInfo(EventRergisterState.ERROR, "[ERROR_0002] Неизвестный тип ивента.");
        }
    }

    private EventRegisterInfo unregisterSinglePlayer(Player player, EventType et) {
        IEventHolder ph = this.getHolderByPlayer(player);
        if (ph != null) {
            if (ph.deletePlayer(player)) {
                return new EventRegisterInfo(EventRergisterState.UNREGISTRED, String.format("Вы вышли из группы регистратора на ивент: %s.", et.getEventTemplate().getEventName()));
            }
        }
        return new EventRegisterInfo(EventRergisterState.ERROR, String.format("Вас нет в списке регистрации на ивент: %s.", et.getEventTemplate().getEventName()));
    }

    public EventRegisterInfo unregisterPlayerGroup(Player leader) {
        if (!leader.isInGroup2()) {
            return new EventRegisterInfo(EventRergisterState.ERROR, "Вы должны быть в группе и быть её лидером.");
        }
        PlayerGroup group = leader.getPlayerGroup2();
        if (!group.isLeader(leader)) {
            return new EventRegisterInfo(EventRergisterState.PLAYER_IS_NOT_GROUP_LEADER, "Вы не лидер группы, вы не можете производить выход из регистратора.");
        }
        IEventHolder holder = this.getHolderByPlayerGroup(group);
        if (holder != null) {
            if (holder.deletePlayerGroup(group)) {
                return new EventRegisterInfo(EventRergisterState.UNREGISTRED, String.format("Ваша группа вышла из списка регистратора на ивент: %s.",
                        holder.getEventType().getEventTemplate().getEventName()));
            }
        }
        return new EventRegisterInfo(EventRergisterState.ERROR, String.format("Вашей группы нет в списке регистрации."));
    }

    public synchronized boolean unregisterPlayer(Player player) {
        IEventHolder holder = this.getHolderByPlayer(player);
        if (holder != null) {
            holder.deletePlayer(player);
            log.info("[EventManager] Player: {} has been unregistered from {} with intex {}", new Object[]{player.getName(), holder.getClass().getSimpleName(), holder.Index()});
            if (holder.isEmpty()) {
                this.deleteHolder(holder);
                log.info("[EventManager] {} with index {} has been deleted from list", holder.getClass().getSimpleName(), holder.Index());
            }
            return true;
        }
        return false;
    }

    /**
     * Устанавливает
     * состояние
     * активности
     * эвента.
     *
     * @param etype тип евента
     * @param state состояние -
     * true (активен) - false
     * (неактивен)
     * @return операция
     * успешна или
     * нет
     */
    public boolean changeEventActiveState(EventType etype, boolean state) {
        if (this.events.containsKey(etype)) {
            this.events.put(etype, state);
            if (state) {
                this.createNewEventSession(etype);
                log.info("Event: {} is started", etype.name());
                if (!this.launcheAnnounce) {
                    this.launcheAnnounce = true;
                    this.announceActiveEvents();
                }
            } else {
                this.removeHolderByEventType(etype);
            }
            return true;
        }
        return false;
    }

    /**
     * Обновляет
     * списки тех
     * кто уже
     * участвовал в
     * евенте
     * заданого
     * типа, тем
     * самым
     * сосдавая
     * новую сессию
     * евента.
     *
     * @param etype тип евента
     */
    public void createNewEventSession(EventType etype) {
        if (this.playedPlayers.containsKey(etype)) {
            this.playedPlayers.get(etype).clear();
        }
    }

    /**
     * Удаляет
     * игрока из
     * списка
     * игравших.
     *
     * @param player
     * @param etype
     * @return true - удален ; false -
     * нет
     */
    public boolean removePlayerFromPlayedList(Player player, EventType etype) {
        if (this.playedPlayers.containsKey(etype)) {
            if (this.playedPlayers.get(etype).containsKey(player.getObjectId())) {
                return this.playedPlayers.get(etype).remove(player.getObjectId()) != null;
            }
        }
        return false;
    }

    /**
     * Проверяет
     * содержится ли
     * игрок в
     * списке уже
     * посещавших
     * евент.
     *
     * @param etype
     * @param player
     * @return
     */
    public boolean containsInPlayed(EventType etype, Player player) {
        return this.playedPlayers.containsKey(etype) && this.playedPlayers.get(etype).containsKey(player.getObjectId());
    }

    /**
     * Проверяет
     * активирован
     * ли ивент или
     * нет.
     *
     * @param etype
     * @return
     */
    public boolean eventIsActive(EventType etype) {
        return this.events.containsKey(etype) && this.events.get(etype);
    }

    /**
     * Проверяет
     * есть ли
     * активные
     * эвенты.
     *
     * @return
     */
    public boolean hasActiveEvents() {
        for (Boolean active : this.events.values()) {
            if (active) {
                return true;
            }
        }
        return false;
    }

    /**
     * Производит
     * попытку
     * запуска
     * евента.
     *
     * @param holder холдер
     * который
     * участвует в
     * запуске
     * евента
     */
    private void tryStartEvent(IEventHolder holder) {
        if (holder.isReadyToGo()) {
            WorldMapInstance inst = InstanceService.getNextAvailableEventInstance(holder);
            EventStartPositionList poss = holder.getEventType().getEventTemplate().getStartPositionInfo();
            switch (holder.getEventType()) {
                case E_1x1:
                case E_LHE:
                    this.startMovePlayers(poss, ((SinglePlayerHolder) holder).getAllPlayers(), holder, inst, 0);
                    break;
                case E_2x2:
                case E_3x3:
                case E_4x4:
                case E_6x6:
                    List<PlayerGroup> groups = ((PlayerGroupEventHolder) holder).getPlayerGroups();
                    for (int i = 0; i < groups.size(); i++) {
                        this.startMovePlayerGroup(groups.get(i), poss, holder.getEventType(), inst, i + 1);
                    }
                    break;
                default:
                    log.error("[EventManager] Unknown event type. Type: {}", holder);
                    break;
            }

            this.deleteHolder(holder);
            log.info("[EventManager] {} with index {} has been deleted from list", holder.getClass().getSimpleName(), holder.Index());
        }
    }

    private void startMovePlayers(EventStartPositionList positions, Collection<Player> players, IEventHolder ph, WorldMapInstance inst, int pos) {
        for (Player p : players) {
            if (ph.contains(p) && this.addPlayerToPlayedMap(p, ph.getEventType())) {
                InstanceService.registerPlayerWithInstance(inst, p);
                this.teleportSinglePlayer(p, inst.getMapId(), inst.getInstanceId(), positions.getPositions().get(pos), EventTeleportType.SINGLE_PLAYER);
                pos++;
                if (pos == positions.getPositions().size()) {
                    pos = 0;
                }
            }
        }
    }

    private void startMovePlayerGroup(PlayerGroup group, EventStartPositionList poss, EventType et, WorldMapInstance inst, int groupPos) {
        List<EventStartPosition> posList = poss.getPositionForGroup(groupPos);
        int pos = 0;
        for (Player m : group.getMembers()) {
            if (this.addPlayerToPlayedMap(m, et)) {
                InstanceService.registerPlayerWithInstance(inst, m);
                this.teleportSinglePlayer(m, inst.getMapId(), inst.getInstanceId(), posList.get(pos), EventTeleportType.PLAYER_IN_GROUP);
                pos++;
                if (pos == posList.size()) {
                    pos = 0;
                }
            }
        }
    }

    private void teleportSinglePlayer(Player p, int worldId, int instId, EventStartPosition spos, EventTeleportType tpType) {
        if (tpType == EventTeleportType.SINGLE_PLAYER) {
            if (p.getPlayerAlliance2() != null) {
                //PlayerAllianceService.removePlayer(p);
            }
            if (p.getPlayerGroup2() != null) {
                PlayerGroupService.removePlayer(p);
            }
        }
        if (p.getKisk() != null) {
            p.getKisk().removePlayer(p);
            p.setKisk(null);
        }
        if (p.getEffectController().getAbnormalEffects().size() > 0) {
            p.getEffectController().removeAllEffects();
        }
        TeleportService.teleportTo(p, worldId, instId, spos.getX(), spos.getY(), spos.getZ(), spos.getH(), TeleportAnimation.BEAM_ANIMATION);
    }

    /**
     * Добавляет
     * игрока в
     * список тех
     * кто уже
     * участвовал в
     * эвентах.
     *
     * @param p
     * @param etype
     */
    private boolean addPlayerToPlayedMap(Player p, EventType etype) {
        if (this.playedPlayers.containsKey(etype)) {
            if (this.playedPlayers.get(etype).containsKey(p.getObjectId())) {
                log.warn("Player has exist in played map, something wrong. Player: {}", p.getObjectId());
                return false;
            }
            PlayerInfo pi = new PlayerInfo(p.getObjectId(), etype.getEventTemplate().getReentryCooldown());
            this.playedPlayers.get(etype).put(p.getObjectId(), pi);
            log.info("[EventManager] Player: {} add to played list. Time in: {} Next time in: {}",
                    new Object[]{p.getName(), new Timestamp(pi.lastEntryTime).toString(), new Timestamp(pi.nextEntryTime).toString()});
        } else {
            Map<Integer, PlayerInfo> plrs = new FastMap<Integer, PlayerInfo>().shared();
            PlayerInfo pi = new PlayerInfo(p.getObjectId(), etype.getEventTemplate().getReentryCooldown());
            plrs.put(p.getObjectId(), pi);
            log.info("[EventManager] Player: {} add to played list. Time in: {} Next time in: {}",
                    new Object[]{p.getName(), new Timestamp(pi.lastEntryTime).toString(), new Timestamp(pi.nextEntryTime).toString()});
            this.playedPlayers.put(etype, plrs);
        }
        return true;
    }

    public boolean containsInHolders(Player player) {
        for (IEventHolder holder : this.holders) {
            if (holder.contains(player)) {
                return true;
            }
        }
        return false;
    }

    private void removeHolderByEventType(EventType etype) {
        for (IEventHolder holder : this.holders) {
            if (holder.getEventType() == etype) {
                this.holders.remove(holder);
                this.removeHolderByEventType(etype);
                break;
            }
        }
    }

    private void deleteHolder(IEventHolder h) {
        int index = h.Index();
        for (int i = 0; i < this.holders.size(); i++) {
            IEventHolder holder = this.holders.get(i);
            if (holder.Index() == index) {
                this.holders.remove(i);
                return;
            }
        }
    }

    private IEventHolder getFirstFreeHolderForSinglePlayer(Player player, EventType etype, EventPlayerLevel epl) {
        for (IEventHolder holder : this.holders) {
            if (holder.getEventType() == etype && holder.getHolderLevel() == epl && holder.canAddPlayer(player)) {
                return holder;
            }
        }
        return null;
    }

    private IEventHolder getFirstFreeHolderForPlayerGroup(PlayerGroup group, EventType etype, EventPlayerLevel epl) {
        for (IEventHolder holder : this.holders) {
            if (holder.getEventType() == etype && holder.getHolderLevel() == epl && holder.canAddGroup(group)) {
                return holder;
            }
        }
        return null;
    }

    private IEventHolder getHolderByPlayer(Player player, EventType etype, EventPlayerLevel epl) {
        for (IEventHolder holder : this.holders) {
            if (holder.getEventType() == etype && holder.getHolderLevel() == epl && holder.contains(player)) {
                return holder;
            }
        }
        return null;
    }

    private IEventHolder getHolderByPlayer(Player player) {
        for (IEventHolder holder : this.holders) {
            if (holder.contains(player)) {
                return holder;
            }
        }
        return null;
    }

    private IEventHolder getHolderByPlayerGroup(PlayerGroup group, EventType etype, EventPlayerLevel epl) {
        for (IEventHolder holder : this.holders) {
            if (holder.getEventType() == etype && holder.getHolderLevel() == epl && holder.contains(group)) {
                return holder;
            }
        }
        return null;
    }

    private IEventHolder getHolderByPlayerGroup(PlayerGroup group) {
        for (IEventHolder holder : this.holders) {
            if (holder.contains(group)) {
                return holder;
            }
        }
        return null;
    }

    private IEventHolder createEventHolder(int index, EventType etype, EventPlayerLevel epl) {
        switch (etype) {
            case E_1x1:
                return new _1x1SamePlayerClassEventHolder(index, etype, epl);
            case E_LHE:
                return new SimpleSinglePlayerEventHolder(index, etype, epl);
            case E_2x2:
            case E_3x3:
            case E_4x4:
            case E_6x6:
                return new PlayerGroupEventHolder(index, etype, epl);
            default:
                return null;
        }
    }

    private EventRegisterInfo standartCheck(EventType etype) {
        if (!this.events.containsKey(etype)) {
            return new EventRegisterInfo(EventRergisterState.CRITICAL_ERROR, String.format("Ивент: %s отсутствует в списке эвентов.", etype.name()));
        }
        /*if (CustomConfig.NEW_PVP_MODE && etype == EventType.E_1x1) {
         return null;
         }*/
        if (!this.eventIsActive(etype)) {
            return new EventRegisterInfo(EventRergisterState.EVENT_NOT_START, "Данный ивент ещё не запущен.");
        }
        return null;
    }

    private EventRegisterInfo checkInPlayed(Player player, EventType etype) {
        if (this.containsInPlayed(etype, player)) {
            if (etype.getEventTemplate().getReentryCooldown() <= 0) {
                return new EventRegisterInfo(EventRergisterState.PLAYER_HAS_VISIT_EVENT, String.format("Вы уже участвовали в ивенте: %s , в этой сессии, ожидайте следующей.", etype.name()));
            }
            PlayerInfo pi = this.playedPlayers.get(etype).get(player.getObjectId());
            log.info("[EventManager] Player: {} time remainig: {}", player.getName(), TimeUtil.convertToString(pi.getRemainingTime()));
            if (!pi.canEntryNow()) {
                String time = TimeUtil.convertToString(pi.getRemainingTime());
                return new EventRegisterInfo(EventRergisterState.PLAYER_HAS_VISIT_EVENT, String.format("Повторный вход возможен через: %s .", time));
            }
            this.removePlayerFromPlayedList(player, etype);
        }
        return null;
    }

    private EventRegisterInfo checkInPlayedGroup(PlayerGroup group, EventType etype) {
        for (Player member : group.getMembers()) {
            if (this.checkInPlayed(member, etype) != null) {
                return new EventRegisterInfo(EventRergisterState.PLAYER_IN_GROUP_ALREADY_VISIT_EVENT, "Один или несколько игроков группы уже участвовали в ивенте.");
            }
        }
        return null;
    }

    private byte getHigherLvlInGroup(PlayerGroup pg) {
        byte lvl = 0;
        for (Player p : pg.getMembers()) {
            if (p.getLevel() > lvl) {
                lvl = p.getLevel();
            }
        }
        return lvl;
    }

    /**
     * Запускает
     * задание
     * оповещения
     * игроков о
     * активных
     * эвентах.
     *
     * @return
     */
    private void announceActiveEvents() {
        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                boolean LHE = false;
                if (hasActiveEvents()) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("==============================\n");
                    sb.append("Открыта регистрация на ивенты:\n");
                    for (Map.Entry<EventType, Boolean> entry : events.entrySet()) {
                        if (entry.getValue()) {
                            if (entry.getKey() == EventType.E_LHE) {
                                LHE = true;
                            }
                            sb.append("- ").append(ColorUtil.convertFromUTF8(entry.getKey().getEventTemplate().getEventName())).append("\n");
                        }
                    }
                    sb.append("Регистрация командой: .event\n").append("==============================");
                    final boolean ll = LHE;
                    World.getInstance().doOnAllPlayers(new Visitor<Player>() {
                        @Override
                        public void visit(Player object) {
                            PacketSendUtility.sendMessage(object, sb.toString());
                            if (ll) {
                                PacketSendUtility.sendBrightYellowMessageOnCenter(object, "Открыта регистрация на ивент \"Последний Герой\" регистрация командой .event reg тип_ивента");
                            }
                        }
                    });
                }
                launcheAnnounce = false;
            }
        }, 5000);
    }

    public String CMD_StartEvent(EventType etype) {
        if (eventIsActive(etype)) {
            return "Ивент - " + etype.name() + " уже запущен. Перезапуск невозможен.";
        }
        if (RunEvent(etype)) {
            return String.format("Ивент: %s запущен.", etype.name());
        } else {
            return String.format("Произошла ошибка при попытке запуска ивента: %s", etype.name());
        }
    }

    //------------------------------------------------------------------------//
    public static EventManager getInstance() {
        return EventManagerHolder.INSTANCE;
    }

    private static class EventManagerHolder {

        private static final EventManager INSTANCE = new EventManager();
    }

    private class PlayerInfo {

        int playerId;
        long nextEntryTime;
        long lastEntryTime = 0;

        public PlayerInfo(int playerId, int reentryCd) {
            this.playerId = playerId;
            this.lastEntryTime = System.currentTimeMillis();
            // if (!CustomConfig.NEW_PVP_MODE) {
            this.nextEntryTime = this.lastEntryTime + reentryCd * 1000;
            //}
        }

        public boolean canEntryNow() {
            return this.nextEntryTime < System.currentTimeMillis();
        }

        public int getRemainingTime() {
            return (int) (this.nextEntryTime - System.currentTimeMillis()) / 1000;
        }
    }
}
