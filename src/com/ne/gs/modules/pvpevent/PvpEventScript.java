package com.ne.gs.modules.pvpevent;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import gnu.trove.map.hash.THashMap;
import org.apache.commons.lang3.mutable.MutableInt;

import com.ne.commons.Sys;
import com.ne.commons.annotations.NotNull;
import com.ne.commons.annotations.Nullable;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.func.tuple.Tuple4;
import com.ne.commons.utils.L10N;
import com.ne.commons.utils.SimpleCond;
import com.ne.gs.model.ChatType;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.Race;
import com.ne.gs.model.conds.CanReadChatMessageCond;
import com.ne.gs.model.conds.IsAggroIconCond;
//import com.ne.gs.model.conds.IsEnemyCond;
import com.ne.gs.model.conds.SkillUseCond;
import com.ne.gs.model.events.PlayerLeftGame;
import com.ne.gs.model.events.PlayerLeftMap;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Homing;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.handlers.EffectResurrectBaseHandler;
import com.ne.gs.modules.common.CustomLocManager;
import com.ne.gs.modules.common.CustomLocScript;
import com.ne.gs.modules.common.Item;
import com.ne.gs.modules.common.PollBuilder;
import com.ne.gs.modules.common.PollRegistry;
import com.ne.gs.modules.common.Pos;
import com.ne.gs.modules.common.PosList;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.network.aion.serverpackets.SM_PLAYER_SPAWN;
import com.ne.gs.network.aion.serverpackets.SM_QUEST_ACTION;
import com.ne.gs.services.HTMLService;
import com.ne.gs.services.player.PlayerReviveService;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.skillengine.SkillEngine;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.idfactory.IDFactory;
import com.ne.gs.world.World;

import static com.ne.gs.modules.common.PollBuilder.TextBuilder;
import com.ne.gs.network.aion.serverpackets.SM_DIE;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author hex1r0
 * @modified Alex
 */
public class PvpEventScript extends CustomLocScript<PvpLocTemplate> {

    private final int PROTECTION_SKILL_ID = 9833;
    protected final Config _config;

    private final PlayerLeftMap LEAVE_MAP_LISTENER = new OnLeaveMap();
    private final PlayerLeftGame LEAVE_GAME_LISTENER = new OnLeaveGame();
    private final OnResurrectBase RES_BASE_LISTENER = new OnResurrectBase();
    private final CanUseSkill SKILL_USE_LISTENER = new CanUseSkill();

    /**
     * @key - player uid
     * @value - number of points (wins)
     */
    protected final Map<Integer, MutableInt> _members = new THashMap<>();

    /**
     * @key - player uid
     * @value - condition
     */
    private final Map<Integer, List<SimpleCond<Tuple2<Player, Player>>>> _enemyConds = new THashMap<>(); // TODO rework after "string identifiers for callbacks" is implemented

    /**
     * @key - player uid
     * @value - bind point
     */
    private final Map<Integer, Pos> _bindPoints = new THashMap<>();

    protected final List<Team> _teams = new ArrayList<>();

    protected ScheduledFuture<?> _roundTask;
    protected int _round = 0;
    protected int oldRound = 0;
    private Recorder _recorder;

    protected boolean _eventRunning = false;
    protected boolean _violator = false;

    public PvpEventScript(PvpLocTemplate tpl, Date expiresAt) {
        super(tpl, expiresAt);

        _config = new Config(tpl);
    }

    @Override
    public PvpLocTemplate getTemplate() {
        return _template;
    }

    @Override
    public void onEnterInstance(final Player player) {
        getProcessor().tell(new Runnable() {
            @Override
            public void run() {
                if (_members.containsKey(player.getObjectId())) {
                    preparePlayer(player);
                }
            }
        });
    }

    @Override
    public boolean onDie(final Player player, final Creature lastAttacker) {
        getProcessor().tell(new Runnable() {
            @Override
            public void run() {
                handleDeath(player, lastAttacker);
            }
        });

        return true;
    }

    @Override
    public void onInstanceDestroy() {
        getProcessor().tell(new Runnable() {
            @Override
            public void run() {
                for (Player player : members()) {
                    unpreparePlayer(player);
                }

                if (_roundTask != null) {
                    _roundTask.cancel(false);
                    _roundTask = null;
                }

                _recorder.acleanup();
            }
        });
    }

    @Override
    public void onReady() {
        _recorder = new Recorder(getTemplate());
    }

    @Override
    public void onRecv(String messageId, Object[] args) {
        switch (messageId) {
            case "SETUP": {
                @SuppressWarnings("unchecked")
                Collection<Player> players = (Collection<Player>) args[0];
                for (Player player : players) {
                    _members.put(player.getObjectId(), new MutableInt(0));
                }

                // participants required to check if they are online
                if (ImmutableList.copyOf(members()).size() == _config.MEMBER_COUNT) {
                    for (Player player : players) {
                        player.setState(CreatureState.EVENT);
                    }
                    setup();
                }
            }
            break;
        }
    }

    protected void preparePlayer(Player player) {
        player.getNotifier().attach(LEAVE_MAP_LISTENER);
        player.getNotifier().attach(LEAVE_GAME_LISTENER);
        player.getChainer().attach(RES_BASE_LISTENER);
        player.getConditioner().attach(CanRead.STATIC);
        player.getConditioner().attach(CanUseSkill.STATIC);
        player.setState(CreatureState.EVENT);
    }

    protected void unpreparePlayer(Player player) {
        player.getNotifier().detach(LEAVE_MAP_LISTENER);
        player.getNotifier().detach(LEAVE_GAME_LISTENER);
        player.getChainer().detach(RES_BASE_LISTENER);
        player.getConditioner().detach(CanRead.STATIC);
        player.getConditioner().detach(CanUseSkill.STATIC);
        detachEnemyCond(player);
        player.unsetState(CreatureState.EVENT);
    }

    protected Collection<Player> members() {
        return Collections2.filter(Collections2.transform(_members.keySet(), new Function<Integer, Player>() {
            @Override
            public Player apply(Integer playerUid) {
                return World.getInstance().findPlayer(playerUid);
            }
        }), Predicates.notNull());
    }

    protected void setup() {
        _eventRunning = true;
        _log.info("loc={} id={} starting...", getTemplate().getId(), instanceId);

        List<Player> players = new ArrayList<>(_config.MEMBER_COUNT);
        for (Integer playerUid : _members.keySet()) {
            Player player = World.getInstance().findPlayer(playerUid);
            if (player != null) {
                if (!EntryCheckHandler.check(player)) {
                    _log.error("loc={} id={} canceled", getTemplate().getId(), instanceId);
                    cleanup();
                    return;
                }
                players.add(player);
            }
        }

        if (players.size() != _config.MEMBER_COUNT) {
            cleanup();
            _log.error("loc={} id={} canceled", getTemplate().getId(), instanceId);
            return;
        }

        Iterator<Player> pit = members().iterator();
        for (PosList posList : getTemplate().getStartPositions()) {
            Set<Player> team = new HashSet<>(posList.getPositions().size());
            for (Pos pos : posList) {
                Player player = pit.next();
                pos.setMapId(getTemplate().getMapId()); // in case it is not provided or different
                _bindPoints.put(player.getObjectId(), pos);
                team.add(player);
            }

            // best way is to set here
            // before players are teleported
            // and then necessary packet will be sent automatically
            for (Player player : team) {
                attachEnemyCond(player, team);
            }

            _teams.add(new Team(team));
        }

        prepareNextRound();
    }

    private void attachEnemyCond(Player player, final Set<Player> team) {
//        IsEnemyCond cond = new IsEnemyCond() {
//            @Override
//            public Boolean onEvent(@NotNull Tuple2<Player, Player> e) {
//                Player owner = e._1;
//                Player opponent = e._2;
//
//                //return owner.getRace() != opponent.getRace();
//                return !(team.contains(owner) && team.contains(opponent));
//            }
//        };    

        IsAggroIconCond cond2 = new IsAggroIconCond() {
            @Override
            public Boolean onEvent(@NotNull Tuple2<Player, Player> e) {
                Player owner = e._1;
                Player opponent = e._2;

                //return owner.getRace() != opponent.getRace();
                return !(team.contains(owner) && team.contains(opponent));
            }
        };

//        List<SimpleCond<Tuple2<Player, Player>>> list = Arrays.asList(cond, cond2);
//
//        _enemyConds.put(player.getObjectId(), list);
//
//        player.getConditioner().attach(cond);
//        player.getConditioner().attach(cond2);
    }

    private void detachEnemyCond(Player player) {
        List<SimpleCond<Tuple2<Player, Player>>> list = _enemyConds.get(player.getObjectId());
        if (list != null) {
            for (SimpleCond<Tuple2<Player, Player>> cond : list) {
                player.getConditioner().detach(cond);
            }
        }
    }

    private void cleanup() {
        _eventRunning = false;
        CustomLocManager.getInstance().tell(new CustomLocManager.Delete(getTemplate().getId()));
    }

    protected void prepareNextRound() {
        _round++;
        // to make it more interesting adding 6 sec delay
        schedule(new Runnable() {
            @Override
            public void run() {
                for (Player player : members()) {
                    if (player.getLifeStats().isAlreadyDead()) {
                        revive(player);
                    }

                    // teleport
                    TeleportService.teleportBeam(player, instanceId, _bindPoints.get(player.getObjectId()));

                    player.getEffectController().removeAllEffects();
                    healPlayer(player, false);
                    removeCd(player);

                    // add protection
                    player.setInvul(true);
                    SkillEngine.getInstance().applyEffectDirectly(PROTECTION_SKILL_ID, player, player, (int) _config.PREPARATION_TIME_MS);
                }

                broadcastEventMsg(Messages.ROUND_STARTS_IN, _round, Sys.toBdTime(_config.PREPARATION_TIME_MS));
                showTimer(_config.PREPARATION_TIME_MS);

                _log.info("loc={} round={} prepared", getTemplate().getId(), _round);

                schedule(new Runnable() {
                    @Override
                    public void run() {
                        startRound();
                    }
                }, _config.PREPARATION_TIME_MS);
            }
        }, SECONDS.toMillis(6));
    }

    private void startRound() {
        for (Player player : members()) {
            player.setInvul(false);
            player.getEffectController().removeEffect(PROTECTION_SKILL_ID);
        }

        hideTimer();
        oldRound--;
        broadcastEventMsg(Messages.ROUND_STARTED, _round);
        showTimer(_config.ROUND_TIME_MS);

        _log.info("loc={} round={} started", getTemplate().getId(), _round);

        _roundTask = schedule(new Runnable() {
            @Override
            public void run() {
                endRound("");
            }
        }, _config.ROUND_TIME_MS);
    }

    protected void endRound(@Nullable String winnerName) {
        if (_roundTask != null) {
            _roundTask.cancel(false);
            _roundTask = null;
        }

        if (winnerName == null || winnerName.isEmpty()) {
            broadcastEventMsg(Messages.ROUND_FINISHED_NOT_WINNER, _round);
        } else {
            broadcastEventMsg(Messages.ROUND_FINISHED_WINNER, _round, winnerName);
        }
        hideTimer();
        _log.info("loc={} round={} finished", getTemplate().getId(), _round);

        for (MutableInt score : _members.values()) {
            if (score.getValue() == _config.WIN_LIMIT) {
                _log.info("PvpEventScript SCORE DEBUG: {} vs {}", score.intValue(), _config.WIN_LIMIT);
                finishEvent(false);
                return;
            }
        }

        if (_members.size() > _config.MEMBER_COUNT) {
            _log.error("INVALID STATE: loc={} round={} finished", getTemplate().getId(), _round); // FIXME temp debug
            finishEvent(true);
            return;
        }

        if (_members.size() != _config.MEMBER_COUNT) {
            finishEvent(true);
            return;
        }

        prepareNextRound();
    }

    private void healPlayer(Player player, boolean dp) {
        player.getLifeStats().setCurrentHpPercent(100);
        player.getLifeStats().setCurrentMpPercent(100);
        player.getLifeStats().setCurrentDp(dp ? 4000 : 0);

        player.getLifeStats().sendHpPacketUpdate();
        player.getLifeStats().sendMpPacketUpdate();
    }

    private void removeCd(Player player) {
        player.flushSkillCd(
                8552,
                8553,
                8554,
                8555,
                8556,
                8557,
                8558,
                8559,
                8560,
                8561,
                11885,
                11886,
                11887,
                11888,
                11889,
                11890,
                11891,
                11892,
                11893,
                11894,
                11907,
                11908,
                11909,
                11910,
                11911,
                11912,
                11913,
                11914,
                11915,
                11916);

        player.flushItemCd();
    }

    protected void handleDeath(Player victim, Creature lastAttacker) {
        //fix summon winner
        if (lastAttacker instanceof Creature && lastAttacker.getMaster() != null) {
            lastAttacker = lastAttacker.getMaster();
        }
        if (!(lastAttacker instanceof Player)) {
            return;
        }

        victim.getMoveController().abortMove();
        victim.setCasting(null);
        victim.getEffectController().removeAllEffects();
        PacketSendUtility.broadcastPacket(victim, new SM_EMOTION(victim, EmotionType.DIE, 0, victim.equals(lastAttacker) ? 0 : lastAttacker.getObjectId()), true);

        if (!_members.containsKey(victim.getObjectId()) || !_members.containsKey(lastAttacker.getObjectId())) {
            //если труп не участник ивента, то показывать окно телепорта на точку воскрешения, как при обычной смерти
            if (!_members.containsKey(victim.getObjectId())) {
                int kiskTimeRemaining = (victim.getKisk() != null ? victim.getKisk().getRemainingLifetime() : 0);
                victim.sendPck(new SM_DIE(victim.canUseRebirthRevive(), victim.haveSelfRezItem(), kiskTimeRemaining, 0));
            }
            return;
        }

        Player winner = (Player) lastAttacker;
        for (Map.Entry<Integer, MutableInt> e : _members.entrySet()) {
            Integer playerUid = e.getKey();
            MutableInt score = e.getValue();
            if (playerUid.equals(winner.getObjectId())) {
                score.add(1);
                endRound(winner.getName());
                break;
            }
        }
    }

    protected final void revive(Player player) {
        PlayerReviveService.revive(player, 100, 100, false, 0);
        player.getGameStats().updateStatsAndSpeedVisually();
        PacketSendUtility.broadcastPacketAndReceive(player, new SM_EMOTION(player, EmotionType.RESURRECT));
        player.sendPck(new SM_PLAYER_SPAWN(player));
    }

    protected void finishEvent(boolean noReward) {
        _eventRunning = false;
        broadcastEventMsg(Messages.EVENT_FINISHED, getTemplate().getId());
        _log.info("loc={} id={} finished", getTemplate().getId(), instanceId);

        for (final Player player : members()) {
            if (player.getLifeStats().isAlreadyDead()) {
                revive(player);
            }
            removeCd(player);
            player.setInvul(false);
            unpreparePlayer(player);
            applyCooldown(player);

            // just minor delay to make it more fancy :)
            schedule(new Runnable() {
                @Override
                public void run() {
                    TeleportService.moveToBindLocation(player, true);
                }
            }, SECONDS.toMillis(5));
        }

        if (!noReward) {
            reward();
        }

        // clean up in 20 sec
        schedule(new Runnable() {
            @Override
            public void run() {
                CustomLocManager.getInstance().tell(new CustomLocManager.Delete(getTemplate().getId()));
            }
        }, SECONDS.toMillis(20));
    }

    protected void applyCooldown(Player player) {
        long cd = Sys.millis() + _config.EVENT_COOLDOWN_MS;

        //player.getVars().setVar(EventCooldown.TYPE, cd);
        Recorder.COOLDOWNS.put(player.getObjectId(), cd);
    }

    protected void reward() {
        List<Tuple2<Player, Integer>> playersByRank = sortByRank(members(),
                new Function<Player, Tuple2<Player, Integer>>() {
            @Override
            public Tuple2<Player, Integer> apply(Player p) {
                Integer score = _members.get(p.getObjectId()).getValue();
                return Tuple2.of(p, score);
            }
        }
        );

        List<PvpRewardList> rewardsByRank = getRewardsByRank(getTemplate().getRewards());
        new Rewarder<PvpRewardList, Tuple2<Player, Integer>>(rewardsByRank, playersByRank) {
            @Override
            public void apply(PvpRewardList reward, Tuple2<Player, Integer> receiver) {
                Player player = receiver._1;
                Integer pollUid = IDFactory.getInstance().nextId();
                PollRegistry.insert(pollUid, new PvpRewardQuery(player, reward));
                String poll = createPoll(player, reward);
                HTMLService.sendData(player, pollUid, poll);
            }
        }.run();
    }

    protected List<PvpRewardList> getRewardsByRank(Collection<PvpRewardList> rewards) {
        List<PvpRewardList> list = new ArrayList<>(rewards);
        Collections.sort(list, REWARDS_BY_RANK);
        return list;
    }

    protected String createPoll(Player player, PvpRewardList reward) {
        PvpItemList randomItems = reward.getRandomRewardList();
        PvpItemList selectiveItems = reward.getSelectiveRewardList();

        String title = new TextBuilder(player.getLang())
                .printf(Messages.POLL_EVENT, getTemplate().getId())
                .build();

        TextBuilder tb = new TextBuilder(player.getLang());
        tb.println(Messages.POLL_EVENT_REWARD);

        if (reward.getAp() > 0) {
            tb.printlnf(Messages.POLL_EVENT_REWARD_AP, reward.getAp());
        }

        if (reward.getLvl() > 0) {
            tb.printlnf(Messages.POLL_EVENT_REWARD_LVL, reward.getLvl());
        }

        if (reward.getGp() > 0) {
            tb.printlnf(Messages.POLL_EVENT_REWARD_GP, reward.getGp());
        }

        if (!randomItems.getItems().isEmpty()) {
            tb.printlnf(Messages.POLL_EVENT_REWARD_RND_ITEM, randomItems.getLimit());
        }

        String body = tb.build();
        PollBuilder pb = new PollBuilder()
                .setTitle(title)
                .setBody(body)
                .setItemCaption(player.translate(Messages.CHOOSE_ITEM, selectiveItems.getLimit()))
                .setItemChooseLimit(selectiveItems.getLimit());

        for (Item item : selectiveItems.getItems()) {
            pb.addItem(item.getItemId(), (int) item.getCount());
        }

        return pb.build();
    }

    private void showTimer(long millis) {
        for (Player player : members()) {
            player.sendPck(new SM_QUEST_ACTION(0, (int) (millis / 1000)));
        }
    }

    protected void hideTimer() {
        for (Player player : members()) {
            player.sendPck(new SM_QUEST_ACTION(0, 0));
        }
    }

    protected void broadcastEventMsg(L10N.Translatable msg, Object... args) {
        for (Player player : members()) {
            PacketSendUtility.sendMessage(player, getTemplate().getId(), player.translate(msg, args), ChatType.GROUP_LEADER);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T, E extends Number> List<Tuple2<T, E>> sortByRank(Collection<T> c, Function<T, Tuple2<T, E>> f) {
        List<Tuple2<T, E>> list = new ArrayList<>(Collections2.transform(c, f));
        Collections.sort((List) list, (Comparator) BY_RANK);
        return list;
    }

    private final static Comparator<Tuple2<?, Number>> BY_RANK = new Comparator<Tuple2<?, Number>>() {
        @Override
        public int compare(Tuple2<?, Number> o1, Tuple2<?, Number> o2) {
            return Integer.compare(o2._2.intValue(), o1._2.intValue());
        }
    };

    protected final static Comparator<PvpRewardList> REWARDS_BY_RANK = new Comparator<PvpRewardList>() {
        @Override
        public int compare(PvpRewardList o1, PvpRewardList o2) {
            return Integer.compare(o1.getRank(), o2.getRank());
        }
    };

    protected void leaveGame(final Player player) {
        _members.remove(player.getObjectId());
        if (_eventRunning && !_violator) {
            applyCooldown(player);
            for (Penalty penalty : getTemplate().getPenalties()) {
                penalty.apply(player);
            }
            if (_roundTask != null) {
                _roundTask.cancel(false);
                _roundTask = null;
            }

            hideTimer();
            broadcastEventMsg(Messages.PLAYER_LEAVE_EVENT, getTemplate().getId());
             finishEvent(false);
            _violator = true;
        }
    }

    protected void leaveMap(final Player player) {
        _members.remove(player.getObjectId());
        player.setInvul(false);
        unpreparePlayer(player);
        if (_eventRunning && !_violator) {
            applyCooldown(player);
            for (Penalty penalty : getTemplate().getPenalties()) {
                penalty.apply(player);
            }
            if (_roundTask != null) {
                _roundTask.cancel(false);
                _roundTask = null;
            }
            hideTimer();
            broadcastEventMsg(Messages.PLAYER_LEAVE_EVENT, getTemplate().getId());
            finishEvent(false);
            _violator = true;
        }
    }

    private class OnLeaveGame extends PlayerLeftGame {

        @Override
        public Object onEvent(@NotNull final Player player) {
            getProcessor().tell(new Runnable() {
                @Override
                public void run() {
                    leaveGame(player);
                }
            });
            return null;
        }
    }

    private class OnLeaveMap extends PlayerLeftMap {

        @Override
        public Object onEvent(@NotNull final Tuple2<Player, Integer> e) {
            getProcessor().tell(new Runnable() {
                @Override
                public void run() {
                    Player player = e._1;
                    Integer mapId = e._2;
                    // required coz listener is set before players are teleported
                    if (mapId.equals(getTemplate().getMapId())) {
                        leaveMap(player);
                    }
                }
            });
            return null;
        }
    }

    private class OnResurrectBase extends EffectResurrectBaseHandler {

        @Override
        public Boolean onEvent(@NotNull Tuple2<Player, Integer> e) {
            return true;
        }
    }

    // Players on the event must understand each other no matter what race they are
    private static class CanRead extends CanReadChatMessageCond {

        static CanRead STATIC = new CanRead();

        @Override
        public Boolean onEvent(@NotNull Tuple4<Race, ChatType, Player, Player> e) {
            return true;
        }
    }

    // Players can not use resurrect skills on event
    private static class CanUseSkill extends SkillUseCond {

        static CanUseSkill STATIC = new CanUseSkill();

        @Override
        public Boolean onEvent(@NotNull Tuple2<Player, Skill> e) {
            Skill skill = e._2;

            if (skill.getSkillTemplate().hasResurrectEffect()) {
                return false;
            }

            if (skill.getSkillTemplate().hasRecallInstant()) {
                return false;
            }

            return SkillUseCond.STATIC.onEvent(e); // redirect to default handler
        }
    }

    protected static class Team {

        public final Set<Player> players;
        public boolean round;

        public Team(Set<Player> players) {
            this.players = players;
        }
    }

}
