/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.eventNewEngine.events.holders;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.ne.gs.eventNewEngine.events.enums.EventPlayerLevel;
import com.ne.gs.eventNewEngine.events.enums.EventType;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;
import java.util.Collection;
import java.util.List;
import javolution.util.FastList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author userd
 */
public abstract class SinglePlayerHolder extends BaseEventHolder {

    protected static final Logger log = LoggerFactory.getLogger(SinglePlayerHolder.class);
    protected List<Player> allPlayers = new FastList<>();

    public SinglePlayerHolder(int index, EventType etype, EventPlayerLevel epl) {
        super(index, etype, epl);
    }

    @Override
    public final boolean contains(Player p) {
        for (Player plr : this.allPlayers) {
            if (plr != null && plr.getObjectId().equals(p.getObjectId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean deletePlayer(Player player) {
        for (int i = 0; i < this.allPlayers.size(); i++) {
            Player p = this.allPlayers.get(i);
            if (p == null || !p.isOnline()) {
                this.allPlayers.remove(i);
                i--;
                continue;
            }
            if (p.getObjectId().equals(player.getObjectId())) {
                this.allPlayers.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.allPlayers.isEmpty();
    }

    public final List<Player> getAllPlayers() {
        return allPlayers;
    }

    public final Collection<Player> getPlayresByRace(final Race race) {
        return Collections2.filter(this.allPlayers, new Predicate<Player>() {
            @Override
            public boolean apply(Player t) {
                return t.getRace() == race;
            }
        });
    }

    public final int getPlayersCountByRace(Race race) {
        int count = 0;
        for (Player p : this.allPlayers) {
            if (p.getRace() == race) {
                count += 1;
            }
        }
        return count;
    }
}

