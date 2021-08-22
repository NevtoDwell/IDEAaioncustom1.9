/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.eventNewEngine.events.holders;

import com.ne.gs.eventNewEngine.events.enums.EventPlayerLevel;
import com.ne.gs.eventNewEngine.events.enums.EventRergisterState;
import com.ne.gs.eventNewEngine.events.enums.EventType;
import com.ne.gs.model.gameobjects.player.Player;

/**
 *
 * @author userd
 */
public class SimpleSinglePlayerEventHolder extends SinglePlayerHolder {

    public SimpleSinglePlayerEventHolder(int index, EventType etype, EventPlayerLevel epl) {
        super(index, etype, epl);
    }

    @Override
    public boolean canAddPlayer(Player player) {
        if (this.contains(player)) {
            return false;
        }
        return this.allPlayers.size() != this.getStartCondition().getSinglePlayersToStart();
    }

    @Override
    public EventRergisterState addPlayer(Player player) {
        this.allPlayers.add(player);
        return EventRergisterState.HOLDER_ADD_PLAYER;
    }

    @Override
    public boolean deletePlayer(Player player) {
        boolean r = super.deletePlayer(player);
        return r;
    }

    @Override
    public boolean isReadyToGo() {
        return this.allPlayers.size() == this.getStartCondition().getSinglePlayersToStart();
    }
}
