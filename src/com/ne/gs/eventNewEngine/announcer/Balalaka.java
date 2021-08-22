/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.eventNewEngine.announcer;

import com.ne.gs.model.ChatType;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.World;
import java.util.Iterator;

/**
 *
 * @author userd
 */
public class Balalaka {
    
    public static void sayInWorld(final String message) {
        Iterator<Player> iter = World.getInstance().getPlayersIterator();
        while (iter.hasNext()) {
            PacketSendUtility.sendYellowMessageOnCenter(iter.next(), message);
        }
    }

    /**
     * Посылает сообщение всем игрокам в указанной локации.
     *
     * @param worldId
     * @param message
     */
    public static void sayInWorld(final int worldId, final String message) {
        Iterator<Player> iter = World.getInstance().getPlayersIterator();
        while (iter.hasNext()) {
            Player p = iter.next();
            if (p.isOnline() && p.getWorldId() == worldId) {
                PacketSendUtility.sendYellowMessageOnCenter(p, message);
            }
        }
    }

    /**
     * Посылает сообщение всем игрокам в мире, с указаной задержкой.
     *
     * @param msg
     * @param delay в секундах
     */
    public static void sayInWorldWithDelay(final String msg, int delay) {
        if (delay == 0) {
            sayInWorld(msg);
        } else {
            ThreadPoolManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    sayInWorld(msg);
                }
            }, delay * 1000);
        }
    }

    /**
     * Посылает сообщение всем игрокам в мире, с указаной задержкой.
     *
     * @param msg
     * @param worldId
     * @param delay в секундах
     */
    public static void sayInWorldWithDelay(final String msg, final int worldId, int delay) {
        if (delay == 0) {
            sayInWorld(worldId, msg);
        } else {
            ThreadPoolManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    sayInWorld(worldId, msg);
                }
            }, delay * 1000);
        }
    }

    /**
     * Посылает сообщение всем игрокам в мире. [Sender]: Text
     *
     * @param sender
     * @param msg
     */
    public static void sayInWorldOrangeTextCenter(final String sender, final String msg) {
        Iterator<Player> iter = World.getInstance().getPlayersIterator();
        while (iter.hasNext()) {
            PacketSendUtility.sendMessage(iter.next(), sender, msg, ChatType.GROUP_LEADER);
        }
    }

    /**
     * Посылает сообщение всем игрокам в указанной локации. [Sender]: Text
     *
     * @param sender
     * @param msg
     * @param worldId
     */
    public static void sayInWorldOrangeTextCenter(final String sender, final String msg, final int worldId) {
        Iterator<Player> iter = World.getInstance().getPlayersIterator();
        while (iter.hasNext()) {
            Player p = iter.next();
            if (p.isOnline() && p.getWorldId() == worldId) {
                PacketSendUtility.sendMessage(p, sender, msg, ChatType.GROUP_LEADER);
            }
        }
    }

    /**
     * Посылает сообщение всем игрокам в мире, с указаной задержкой. [Sender]:
     * Text
     *
     * @param sender
     * @param msg
     * @param delay в секундах
     */
    public static void sayInWorldOrangeTextCenterWithDelay(final String sender, final String msg, int delay) {
        if (delay == 0) {
            sayInWorldOrangeTextCenter(sender, msg);
        } else {
            ThreadPoolManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    sayInWorldOrangeTextCenter(sender, msg);
                }
            }, delay * 1000);
        }
    }

    /**
     * Посылает сообщение всем игрокам в мире, с указаной задержкой. [Sender]:
     * Text
     *
     * @param sender
     * @param msg
     * @param worldId
     * @param delay в секундах
     */
    public static void sayInWorldOrangeTextCenterWithDelay(final String sender, final String msg, final int worldId, int delay) {
        if (delay == 0) {
            sayInWorldOrangeTextCenter(sender, msg, worldId);
        } else {
            ThreadPoolManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    sayInWorldOrangeTextCenter(sender, msg, worldId);
                }
            }, delay * 1000);
        }
    }
    
    
}
