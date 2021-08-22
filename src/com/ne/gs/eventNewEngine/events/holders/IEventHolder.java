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
import com.ne.gs.model.team2.group.PlayerGroup;

/**
 *
 * @author userd
 */
public interface IEventHolder {

    public boolean canAddGroup(PlayerGroup group);

    public boolean canAddPlayer(Player player);

    public EventRergisterState addPlayer(Player player);

    public EventRergisterState addPlayerGroup(PlayerGroup group);

    public boolean deletePlayer(Player player);

    public boolean deletePlayerGroup(PlayerGroup group);

    public boolean isReadyToGo();

    public boolean contains(Player p);

    public boolean contains(PlayerGroup group);

    public EventPlayerLevel getHolderLevel();

    public EventType getEventType();

    public int Index();

    public boolean isEmpty();
}

