/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.pvpevent;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import gnu.trove.map.hash.THashMap;
import org.apache.commons.lang3.mutable.MutableInt;

import com.ne.commons.annotations.Nullable;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.modules.common.PollRegistry;
import com.ne.gs.network.aion.serverpackets.SM_DIE;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.services.HTMLService;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.idfactory.IDFactory;

/**
 * @author hex1r0
 * @modified Alex
 */
public class MassPvpEventScript extends PvpEventScript {

    private final Map<Integer, MutableInt> _playerScores = new THashMap<>();
    private final Map<Team, MutableInt> _teamScores = new THashMap<>();

    public MassPvpEventScript(PvpLocTemplate tpl, Date expiresAt) {
        super(tpl, expiresAt);
    }

    @Override
    protected void setup() {
        super.setup();

        for (Team team : _teams) {
            _teamScores.put(team, new MutableInt(0));
        }

        for (Integer playerUid : _members.keySet()) {
            _playerScores.put(playerUid, new MutableInt(0));
        }
    }

    @Override
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
        addPlayerScore(winner);
        Team victimTeam = findTeam(victim);
        if (checkTeamMembersDead(victimTeam) && oldRound < _round) {
            Team winnerTeam = findTeam(winner);
            addTeamScore(winnerTeam);
            endRound("");
            oldRound = _round;
        }
    }

    private boolean checkTeamMembersDead(Team team) {
        boolean allDead = true;
        for (Player player : team.players) {
            if (!player.getLifeStats().isAlreadyDead()) {
                allDead = false;
                break;
            }
        }
        return allDead;
    }

    private void addPlayerScore(Player winner) {
        MutableInt score = _playerScores.get(winner.getObjectId());
        score.add(1);

        _log.info("MassPvpEventScript DEBUG: {} player score {}", winner.getName(), score.intValue());
    }

    private void addTeamScore(Team team) {
        MutableInt score = _teamScores.get(team);
        score.add(1);

        _log.info("MassPvpEventScript DEBUG: {} team score {}", team, score.intValue());
    }

    private Team findTeam(Player player) {
        for (Team team : _teams) {
            if (team.players.contains(player)) {
                return team;
            }
        }

        return null;
    }

    @Override
    protected void endRound(@Nullable String winnerName) {
        if (_roundTask != null) {
            _roundTask.cancel(false);
            _roundTask = null;
        }

        broadcastEventMsg(Messages.ROUND_FINISHED, _round);
        hideTimer();

        _log.info("loc={} round={} finished", getTemplate().getId(), _round);

        for (MutableInt score : _teamScores.values()) {
            if (score.intValue() >= _config.WIN_LIMIT) {
                _log.info("MassPvpEventScript DEBUG: {} vs {}", score.intValue(), _config.WIN_LIMIT);
                finishEvent(false);
                return;
            }
        }

//        if (_members.size() != _config.MEMBER_COUNT) {
//            finishEvent(true);
//            return; 
//        }
        prepareNextRound();
    }

    @Override
    protected void leaveGame(Player player) {
        Team victimTeam = findTeam(player);
        if (victimTeam != null) {
//            _teamScores.remove(victimTeam);
            victimTeam.players.remove(player);
        }

        if (_members.containsKey(player.getObjectId())) {
            _members.remove(player.getObjectId());
        }
        player.setInvul(false);
        unpreparePlayer(player);
        applyCooldown(player);
        if (_eventRunning) {
            if (victimTeam == null || victimTeam.players.isEmpty()) {
                if (_roundTask != null) {
                    _roundTask.cancel(false);
                    _roundTask = null;
                }
                finishEvent(false);
            }

            for (Penalty penalty : getTemplate().getPenalties()) {
                penalty.apply(player);
            }

            hideTimer();
            broadcastEventMsg(Messages.PLAYER_LEAVE_MASS_EVENT, player.getName(), getTemplate().getId());
        }
    }

    @Override
    protected void leaveMap(Player player) {
        Team victimTeam = findTeam(player);
        if (victimTeam != null) {
//            _teamScores.remove(victimTeam);
            victimTeam.players.remove(player);
        }

        if (_members.containsKey(player.getObjectId())) {
            _members.remove(player.getObjectId());
        }
        player.setInvul(false);
        unpreparePlayer(player);
        applyCooldown(player);
        if (_eventRunning) {
            if (victimTeam == null || victimTeam.players.isEmpty()) {
                if (_roundTask != null) {
                    _roundTask.cancel(false);
                    _roundTask = null;
                }
                finishEvent(false);
            }

            for (Penalty penalty : getTemplate().getPenalties()) {
                penalty.apply(player);
            }

            hideTimer();
            broadcastEventMsg(Messages.PLAYER_LEAVE_MASS_EVENT, player.getName(), getTemplate().getId());
        }

    }

    @Override
    protected void reward() {
        List<Tuple2<Player, MutableInt>> playersByRank = getPlayersByRank();
        List<PvpRewardList> playerRewards = getRewardsByRank(Collections2.filter(getTemplate().getRewards(),
                new Predicate<PvpRewardList>() {
            @Override
            public boolean apply(PvpRewardList list) {
                return "player".equalsIgnoreCase(list.getGroup());
            }
        }));

        List<Tuple2<Team, MutableInt>> teamsByRank = getTeamsByRank();
        List<PvpRewardList> teamRewards = getRewardsByRank(
                Collections2.filter(getTemplate().getRewards(), new Predicate<PvpRewardList>() {
                    @Override
                    public boolean apply(PvpRewardList list) {
                        return "team".equalsIgnoreCase(list.getGroup());
                    }
                }));

        new Rewarder<PvpRewardList, Tuple2<Player, MutableInt>>(playerRewards, playersByRank) {
            @Override
            public void apply(PvpRewardList reward, Tuple2<Player, MutableInt> receiver) {
                Player player = receiver._1;
                Integer pollUid = IDFactory.getInstance().nextId();
                PollRegistry.insert(pollUid, new PvpRewardQuery(player, reward));
                String poll = createPoll(player, reward);
                HTMLService.sendData(player, pollUid, poll);
            }
        }.run();

        new Rewarder<PvpRewardList, Tuple2<Team, MutableInt>>(teamRewards, teamsByRank) {
            @Override
            public void apply(PvpRewardList reward, Tuple2<Team, MutableInt> receiver) {
                Team team = receiver._1;
                for (Player player : team.players) {
                    Integer pollUid = IDFactory.getInstance().nextId();
                    PollRegistry.insert(pollUid, new PvpRewardQuery(player, reward));
                    String poll = createPoll(player, reward);
                    HTMLService.sendData(player, pollUid, poll);
                }
            }
        }.run();
    }

    private List<Tuple2<Player, MutableInt>> getPlayersByRank() {
        return sortByRank(
                members(),
                new Function<Player, Tuple2<Player, MutableInt>>() {
            @Override
            public Tuple2<Player, MutableInt> apply(Player p) {
                MutableInt score = _playerScores.get(p.getObjectId());
                return Tuple2.of(p, score);
            }
        }
        );
    }

    private List<Tuple2<Team, MutableInt>> getTeamsByRank() {
        return sortByRank(_teams, new Function<Team, Tuple2<Team, MutableInt>>() {
            @Override
            public Tuple2<Team, MutableInt> apply(Team t) {
                MutableInt score = _teamScores.get(t);
                return Tuple2.of(t, _teamScores.get(t));
            }
        }
        );
    }
}
