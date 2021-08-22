package com.ne.gs.modules.pvpevent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.Sys;
import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.utils.Actor;
import com.ne.commons.utils.ActorRef;
import com.ne.commons.utils.Chainer;
import com.ne.commons.utils.EventNotifier;
import com.ne.gs.model.ChatType;
import com.ne.gs.model.events.PlayerEnteredGame;
import com.ne.gs.model.events.PlayerLeftGame;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.modules.common.CustomLocManager;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.Visitor;

import static com.ne.commons.Sys.millis;
import static com.ne.commons.Sys.toBdTime;

/**
 * @author hex1r0
 */
public class Recorder {

    private static final Logger _log = LoggerFactory.getLogger(Recorder.class);

    private final OnEnterGame ENTER_GAME_LISTENER = new OnEnterGame();
    private final OnLeaveGame LEAVE_GAME_LISTENER = new OnLeaveGame();
    private final OnApply APPLY_LISTENER;
    private final OnCancel CANCEL_LISTENER;

    private final ActorRef<?> _processor = ActorRef.of(new Actor());

    private final Map<Integer, Player> _players = new HashMap<>();
    private final PvpLocTemplate _tpl;
    private final Config _cfg;
    private final String _cmd;

    private boolean _open = true;
    private ScheduledFuture<?> _prepareEventTask;

    private long _readyTimeMs;

    public Recorder(PvpLocTemplate tpl) {
        _tpl = tpl;
        _cfg = new Config(tpl);
        _cmd = _tpl.getPlayerCmd().getName();

        APPLY_LISTENER = new OnApply(_cmd);
        CANCEL_LISTENER = new OnCancel(_cmd);

        _readyTimeMs = millis();
        _prepareEventTask = schedule(new Runnable() {
            @Override
            public void run() {
                prepareEvent(true);
            }
        }, _cfg.APPLY_TIME_MS);

        World.getInstance().doOnAllPlayers(new Visitor<Player>() {
            @Override
            public void visit(Player player) {
                if (player.isInInstance()
                        || player.isInPrison()
                        || player.isInState(CreatureState.EVENT)) {
                    return;
                }

                sendApplyOpen(player);
            }
        });
        info("scheduled start in {}", toBdTime(_cfg.APPLY_TIME_MS));

        Chainer.GLOBAL.attach(APPLY_LISTENER);
        Chainer.GLOBAL.attach(CANCEL_LISTENER);
        EventNotifier.GLOBAL.attach(ENTER_GAME_LISTENER);
    }

    public static Map<Integer, Long> COOLDOWNS = Collections.synchronizedMap(new HashMap<Integer, Long>());

    private void apply(Player player) {
        if (!_open) {
            player.sendMsg(Messages.REGISTER_WRONG_TIME);
            return;
        }

//        Long cd = player.getVars().getVar(EventCooldown.TYPE);
//        if (cd != null) {
//            long diff = cd - Sys.millis();
//            if (diff > 0) {
//                player.sendMsg(Messages.REGISTER_COOLDOWN, toBdTime(diff));
//                return;
//            }
//        }
        // TODO temp solution, implement player_vars.sql
        Long cd = COOLDOWNS.get(player.getObjectId());
        if (cd != null && !player.isGM()) {
            long diff = cd - Sys.millis();
            if (diff > 0) {
                player.sendMsg(Messages.REGISTER_COOLDOWN, toBdTime(diff));
                return;
            }
        }

        if (player.getLevel() < 55) {
            player.sendMsg(Messages.REGISTER_INVALID_LEVEL, "55+");
            if (_players.containsKey(player.getObjectId())) {
                _players.remove(player.getObjectId());
            }
            return;
        }

        if (!EntryCheckHandler.check(player)) {
            player.sendMsg(Messages.REGISTER_INVALID_STATE, _tpl.getId());
            if (_players.containsKey(player.getObjectId())) {
                _players.remove(player.getObjectId());
            }
            return;
        }

        if (_players.containsKey(player.getObjectId())) {
            player.sendMsg(Messages.REGISTER_TWICE_ERROR, _tpl.getId());
            return;
        }

        if (player.isInState(CreatureState.EVENT)) {
            player.sendMsg(Messages.REGISTER_TWICE_ERROR, _tpl.getId());
            return;
        }

        if (remainingTime() <= 0) {
            player.sendMsg(Messages.REGISTER_TIME_OVER, _tpl.getId());
            return;
        }

        _players.put(player.getObjectId(), player);

        init(player);

        for (Player participant : _players.values()) {
            if (participant.getObjectId().equals(player.getObjectId())) {
                continue;
            }
            participant.sendMsg(Messages.REGISTER_PARTICIPANT,
                    player.getName(),
                    _tpl.getId(),
                    _players.size(),
                    _cfg.MEMBER_COUNT
            );
        }

        if (_players.size() == _cfg.MEMBER_COUNT) {
            prepareEvent(false);
        }
    }

    private void cancel(Integer playerUid) {
        Player player = _players.remove(playerUid);
        if (player != null) {
            player.sendMsg(Messages.CANCEL_SUCCESS, _tpl.getId());

            restore(player);
        }
    }

    private long remainingTime() {
        return (_readyTimeMs + _cfg.APPLY_TIME_MS) - millis();
    }

    private void init(Player player) {
        player.sendMsg(Messages.REGISTER_SUCCESS, _tpl.getId(), _players.size(), _cfg.MEMBER_COUNT);
        info("player={} registered", player.getName());

        player.getNotifier().attach(LEAVE_GAME_LISTENER);
        player.setState(CreatureState.EVENT);
    }

    private void restore(Player player) {
        player.getNotifier().detach(LEAVE_GAME_LISTENER);
        player.unsetState(CreatureState.EVENT);
    }

    private void prepareEvent(boolean timeout) {
        for (Player player : ImmutableList.copyOf(_players.values())) {
            if (!EntryCheckHandler.check(player)) {
                _players.remove(player.getObjectId());
                String msg = player.translate(Messages.REGISTER_INVALID_STATE);
                sendEventMsg(player, msg);
            }
        }

        // continue finding players if there is some time left
        if (!timeout) {
            if (_players.size() != _cfg.MEMBER_COUNT) {
                return;
            }
        }

        cleanup();

        if (_players.size() != _cfg.MEMBER_COUNT) {
            info("not enough players");
            schedule(new Runnable() {
                @Override
                public void run() {
                    CustomLocManager.getInstance().tell(new CustomLocManager.Delete(_tpl.getId()));
                }
            }, TimeUnit.SECONDS.toMillis(20));

            for (Player player : _players.values()) {
                String msg = player.translate(Messages.REGISTER_NOT_ENOUGH_PLS);
                sendEventMsg(player, msg);
            }
            return;
        }

        CustomLocManager.getInstance().tell(new CustomLocManager.SendMsg(_tpl.getId(), "SETUP", _players.values()));
    }

    private void cleanup() {
        Chainer.GLOBAL.detach(APPLY_LISTENER);
        Chainer.GLOBAL.detach(CANCEL_LISTENER);
        EventNotifier.GLOBAL.detach(ENTER_GAME_LISTENER);

        _open = false;
        if (_prepareEventTask != null) {
            _prepareEventTask.cancel(false);
            _prepareEventTask = null;
        }

        for (Player player : _players.values()) {
            restore(player);
        }
    }

    public void acleanup() {
        _processor.tell(new Runnable() {
            @Override
            public void run() {
                cleanup();
            }
        });
    }

    private ScheduledFuture<?> schedule(final Runnable task, long delayMs) {
        return ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                _processor.tell(task);
            }
        }, delayMs);
    }

    private void info(String msg, Object... args) {
        _log.info("location=" + _tpl.getId() + " " + msg, args);
    }

    private void error(String msg, Object... args) {
        _log.error("location=" + _tpl.getId() + " " + msg, args);
    }

    private void sendApplyOpen(Player player) {
        String msg = player.translate(Messages.REGISTER_OPEN, _tpl.getId(), toBdTime(remainingTime()), _cmd);
        sendEventMsg(player, msg);
    }

    private void sendEventMsg(Player player, String msg) {
        PacketSendUtility.sendMessage(player, _tpl.getId(), msg, ChatType.GROUP_LEADER);
    }

    private class OnEnterGame extends PlayerEnteredGame {

        @Override
        public Object onEvent(@NotNull final Player player) {
            _processor.tell(new Runnable() {
                @Override
                public void run() {
                    sendApplyOpen(player);
                }
            });

            return null;
        }
    }

    private class OnLeaveGame extends PlayerLeftGame {

        @Override
        public Object onEvent(@NotNull final Player player) {
            _processor.tell(new Runnable() {
                @Override
                public void run() {
                    _players.remove(player.getObjectId());
                }
            });
            return null;
        }
    }

    private abstract class OnAction extends EventCallback {

        private final String _cmd;

        public OnAction(String cmd) {
            _cmd = cmd;
        }

        @Override
        public final Boolean onEvent(@NotNull Tuple2<Player, String> e) {
            String cmd = e._2;
            if (_cmd.equalsIgnoreCase(cmd)) {
                onAction(e._1);
                return true;
            }
            return false;
        }

        protected abstract void onAction(Player player);
    }

    private class OnApply extends OnAction {

        public OnApply(String cmd) {
            super(cmd);
        }

        @Override
        protected void onAction(final Player player) {
            _processor.tell(new Runnable() {
                @Override
                public void run() {
                    apply(player);
                }
            });
        }

        @NotNull
        @Override
        public String getType() {
            return Apply.class.getName();
        }
    }

    private class OnCancel extends OnAction {

        public OnCancel(String cmd) {
            super(cmd);
        }

        @Override
        protected void onAction(final Player player) {
            _processor.tell(new Runnable() {
                @Override
                public void run() {
                    cancel(player.getObjectId());
                }
            });
        }

        @NotNull
        @Override
        public String getType() {
            return Cancel.class.getName();
        }
    }
}
