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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author userd
 */
public class PlayerGroupEventHolder extends SinglePlayerHolder {

    private final List<PlayerGroup> groups = new ArrayList<>();

    public PlayerGroupEventHolder(int index, EventType etype, EventPlayerLevel epl) {
        super(index, etype, epl);
    }

    @Override
    public boolean isReadyToGo() {
        if (groups.size() == this.getStartCondition().getGroupsToStart()) {
            for (PlayerGroup pg : this.groups) {
                if (pg.size() != this.getStartCondition().getPlayersForEachGroup()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canAddGroup(PlayerGroup group) {
        if (this.contains(group)) {
            return false;
        }
        for (Player m : group.getMembers()) {
            if (this.contains(m)) {
                return false;
            }
        }
        if (this.isReadyToGo()) {
            return false;
        }
        return true;
    }

    @Override
    public EventRergisterState addPlayerGroup(PlayerGroup group) {
        this.groups.add(group);
        for (Player m : group.getMembers()) {
            this.addPlayer(m);
        }
        return EventRergisterState.HOLDER_ADD_GROUP;
    }

    @Override
    public boolean deletePlayerGroup(PlayerGroup group) {
        for (int i = 0; i < this.groups.size(); i++) {
            PlayerGroup pg = this.groups.get(i);
            if (pg != null) {
                for (Player m : pg.getMembers()) {
                    this.deletePlayer(m);
                }
                this.groups.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(PlayerGroup group) {
        if (this.groups.contains(group)) {
            return true;
        }
        for (PlayerGroup pg : this.groups) {
            if (pg.getObjectId() == group.getObjectId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public EventRergisterState addPlayer(Player player) {
        this.allPlayers.add(player);
        return EventRergisterState.HOLDER_ADD_PLAYER;
    }

    public final List<PlayerGroup> getPlayerGroups() {
        return this.groups;
    }
}