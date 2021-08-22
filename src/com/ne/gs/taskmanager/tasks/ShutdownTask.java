/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.taskmanager.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.ShutdownHook;
import com.ne.gs.ShutdownHook.ShutdownMode;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.tasks.TaskFromDBHandler;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.Visitor;

/**
 * @author Divinity
 */
public class ShutdownTask extends TaskFromDBHandler {

    private static final Logger log = LoggerFactory.getLogger(ShutdownTask.class);

    private int countDown;
    private int announceInterval;
    private int warnCountDown;

    @Override
    public String getTaskName() {
        return "shutdown";
    }

    @Override
    public boolean isValid() {
        return params.length == 3;

    }

    @Override
    public void run() {
        log.info("Task[" + id + "] launched : shuting down the server !");
        setLastActivation();

        countDown = Integer.parseInt(params[0]);
        announceInterval = Integer.parseInt(params[1]);
        warnCountDown = Integer.parseInt(params[2]);

        World.getInstance().doOnAllPlayers(new Visitor<Player>() {

            @Override
            public void visit(Player player) {
                PacketSendUtility.sendBrightYellowMessageOnCenter(player, "Automatic Task: The server will shutdown in " + warnCountDown
                    + " seconds ! Please find a peace place and disconnect your character.");
            }
        });

        ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                ShutdownHook.getInstance().doShutdown(countDown, announceInterval, ShutdownMode.SHUTDOWN);
            }
        }, warnCountDown * 1000);
    }
}
