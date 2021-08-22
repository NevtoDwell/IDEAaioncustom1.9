/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import com.ne.commons.services.CronService;
import com.ne.gs.configs.main.AdvCustomConfig;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.world.World;
import com.ne.gs.world.knownlist.Visitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alexsis
 * sry for that shit solution=)
 */
public class QuestRepeatUpdate {

    private static final Logger _log = LoggerFactory.getLogger(QuestRepeatUpdate.class);
    private static final String UpdateTime = AdvCustomConfig.QUEST_REPEAT_UPDATE; //everyday at 9 am
    private static final QuestRepeatUpdate instance = new QuestRepeatUpdate();

    public static QuestRepeatUpdate getInstance() {
        return instance;
    }

    public void update() {
        CronService.getInstance().schedule(new Runnable() {
                    @Override
                    public void run() {
                        World.getInstance().doOnAllPlayers(new Visitor<Player>() {

                            @Override
                            public void visit(Player player) {
                               if (!player.isInInstance()) {
                                   player.getController().updateNearbyQuests();
                                   _log.info("Repeated quests updated at 9 am.......");
                                   PacketSendUtility.sendYellowMessageOnCenter(player, "Ежедневные задания обновлены. | Daily quests updated.");
                               }
                            }
                        });
                    }
                }, UpdateTime);
    }
}
