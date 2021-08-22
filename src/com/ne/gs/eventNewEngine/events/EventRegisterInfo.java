/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.eventNewEngine.events;

import com.ne.gs.eventNewEngine.debug.DebugInfo;
import com.ne.gs.eventNewEngine.events.enums.EventRergisterState;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.utils.PacketSendUtility;

/**
 *
 * @author userd
 */
public class EventRegisterInfo extends DebugInfo<EventRergisterState> {

    public EventRegisterInfo(EventRergisterState state, String message) {
        super(state, message);
    }

    public EventRegisterInfo(EventRergisterState state) {
        super(state);
    }

    public void sendMessageToGroupMebmers(PlayerGroup pg) {
        switch (this.state) {
            case GROUP_NOT_REGISTRED:
                break;
            default:
                for (Player p : pg.getMembers()) {
                    if (p != null && p.isOnline()) {
                        PacketSendUtility.sendMessage(p, message);
                    }
                }
                break;
        }
    }
}
