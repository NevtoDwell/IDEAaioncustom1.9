/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.eventNewEngine.events.holders;

import com.ne.gs.eventNewEngine.events.enums.EventPlayerLevel;
import com.ne.gs.eventNewEngine.events.enums.EventRergisterState;
import com.ne.gs.eventNewEngine.events.enums.EventType;
import com.ne.gs.eventNewEngine.events.xml.EventStartCondition;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.group.PlayerGroup;

/**
 *
 * @author userd
 */
public abstract class BaseEventHolder implements IEventHolder {

    private final int _index;
    private final EventPlayerLevel holderLevel;
    private final EventType eventType;
    private EventStartCondition startCond;

    public BaseEventHolder(int index, EventType etype, EventPlayerLevel epl) {
        this._index = index;
        this.holderLevel = epl;
        this.eventType = etype;
        this.startCond = this.eventType.getEventTemplate().getStartCondition();
    }

    @Override
    public int Index() {
        return this._index;
    }

    @Override
    public final EventPlayerLevel getHolderLevel() {
        return holderLevel;
    }

    @Override
    public final EventType getEventType() {
        return eventType;
    }

    public EventStartCondition getStartCondition() {
        return this.startCond;
    }

    @Override
    public boolean canAddPlayer(Player player) {
        return false;
    }

    @Override
    public EventRergisterState addPlayer(Player player) {
        return null;
    }

    @Override
    public boolean deletePlayer(Player player) {
        return false;
    }

    @Override
    public boolean isReadyToGo() {
        return false;
    }

    @Override
    public boolean contains(Player p) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean canAddGroup(PlayerGroup group) {
        return false;
    }

    @Override
    public EventRergisterState addPlayerGroup(PlayerGroup group) {
        return null;
    }

    @Override
    public boolean deletePlayerGroup(PlayerGroup group) {
        return false;
    }

    @Override
    public boolean contains(PlayerGroup group) {
        return false;
    }
}
