/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import javolution.util.FastList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.network.util.ThreadPoolManager;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.curingzone.CuringObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.curingzones.CuringTemplate;
import com.ne.gs.skillengine.SkillEngine;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.world.knownlist.Visitor;

public class CuringZoneService {

    Logger log = LoggerFactory.getLogger(CuringZoneService.class);
    private final FastList<CuringObject> curingObjects = new FastList<>();

    private CuringZoneService() {
        for (CuringTemplate t : DataManager.CURING_OBJECTS_DATA.getCuringObject()) {
            CuringObject obj = new CuringObject(t, 0);
            obj.spawn();
            curingObjects.add(obj);
        }
        log.info("spawned Curing Zones");
        startTask();
    }

    private void startTask() {
        ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                for (final CuringObject obj : curingObjects) {
                    obj.getKnownList().doOnAllPlayers(new Visitor<Player>() {

                        @Override
                        public void visit(Player player) {
                            if ((MathUtil.isIn3dRange(obj, player, obj.getRange())) && (!player.getEffectController().hasAbnormalEffect(8751))) {
                                SkillEngine.getInstance().getSkill(player, 8751, 1, player).useNoAnimationSkill();
                            }
                        }
                    });
                }
            }
        }, 1000, 1000);
    }

    public static CuringZoneService getInstance() {
        return SingletonHolder.instance;
    }

    private static final class SingletonHolder {

        protected static final CuringZoneService instance = new CuringZoneService();
    }
}
