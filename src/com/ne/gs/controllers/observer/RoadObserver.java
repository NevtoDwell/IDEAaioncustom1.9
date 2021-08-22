/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.observer;

import com.ne.gs.model.Race;
import com.ne.gs.model.TeleportAnimation;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.road.Road;
import com.ne.gs.model.templates.road.RoadExit;
import com.ne.gs.model.utils3d.Point3D;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.world.WorldType;

/**
 * @author SheppeR
 */
public class RoadObserver extends ActionObserver {

    private final Player player;
    private final Road road;
    private Point3D oldPosition;

    public RoadObserver() {
        super(ObserverType.MOVE);
        player = null;
        road = null;
        oldPosition = null;
    }

    public RoadObserver(Road road, Player player) {
        super(ObserverType.MOVE);
        this.player = player;
        this.road = road;
        oldPosition = new Point3D(player.getX(), player.getY(), player.getZ());
    }

    @Override
    public void moved() {
        Point3D newPosition = new Point3D(player.getX(), player.getY(), player.getZ());
        boolean passedThrough = false;

        if (road.getPlane().intersect(oldPosition, newPosition)) {
            Point3D intersectionPoint = road.getPlane().intersection(oldPosition, newPosition);
            if (intersectionPoint != null) {
                double distance = Math.abs(road.getPlane().getCenter().distance(intersectionPoint));

                if (distance < road.getTemplate().getRadius()) {
                    passedThrough = true;
                }
            } else if (MathUtil.isIn3dRange(road, player, road.getTemplate().getRadius())) {
                passedThrough = true;
            }
        }

        if (passedThrough) {
            RoadExit exit = road.getTemplate().getRoadExit();

            WorldType type = road.getWorldType();
            if (type == WorldType.ELYSEA) {
                if (player.getRace() == Race.ELYOS) {
                    TeleportService.teleportTo(player, exit.getMap(), exit.getX(), exit.getY(), exit.getZ(), (byte) 0, TeleportAnimation.BEAM_ANIMATION);
                }
            } else if (type == WorldType.ASMODAE) {
                if (player.getRace() == Race.ASMODIANS) {
                    TeleportService.teleportTo(player, exit.getMap(), exit.getX(), exit.getY(), exit.getZ(), (byte) 0, TeleportAnimation.BEAM_ANIMATION);
                }
            } else {
                TeleportService.teleportTo(player, exit.getMap(), exit.getX(), exit.getY(), exit.getZ(), (byte) 0, TeleportAnimation.BEAM_ANIMATION);
            }
        }
        oldPosition = newPosition;
    }
}
