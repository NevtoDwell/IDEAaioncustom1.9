/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.eventNewEngine.events.holders;

import com.ne.gs.eventNewEngine.events.enums.EventPlayerLevel;
import com.ne.gs.eventNewEngine.events.enums.EventRergisterState;
import com.ne.gs.eventNewEngine.events.enums.EventType;
import com.ne.gs.model.PlayerClass;
import com.ne.gs.model.gameobjects.player.Player;

/**
 *
 * @author userd
 */
public class _1x1SamePlayerClassEventHolder extends SinglePlayerHolder {

    private PlayerClass holderClass = PlayerClass.ALL;

    public _1x1SamePlayerClassEventHolder(int index, EventType etype, EventPlayerLevel epl) {
        super(index, etype, epl);
    }

    @Override
    public boolean canAddPlayer(Player player) {
        if (this.contains(player)) {
            return false;
        }
        /* проверка класса игрока, раскомментировать для того чтобы ивент стал классовым
         if (this.holderClass != PlayerClass.ALL && this.holderClass != player.getPlayerClass()) {
         return false;
         }
         */
        return this.allPlayers.size() != 2;
    }

    @Override
    public EventRergisterState addPlayer(Player player) {
        if (this.holderClass == PlayerClass.ALL) {
            this.holderClass = player.getPlayerClass();
        }
        this.allPlayers.add(player);
        return EventRergisterState.HOLDER_ADD_PLAYER;
    }

    @Override
    public boolean deletePlayer(Player player) {
        boolean r = super.deletePlayer(player);
        if (this.allPlayers.isEmpty()) {
            this.holderClass = PlayerClass.ALL;
        }
        return r;
    }

    @Override
    public boolean isReadyToGo() {
        return this.allPlayers.size() == 2;
    }
}
