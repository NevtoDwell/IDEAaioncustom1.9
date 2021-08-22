/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.instance.instancereward;

import javolution.util.FastList;
import org.apache.commons.lang3.mutable.MutableInt;

import com.ne.commons.utils.Rnd;
import com.ne.gs.configs.main.DredgionConfig;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.geometry.Point3D;
import com.ne.gs.model.instance.playerreward.DredgionPlayerReward;
import com.ne.gs.services.teleport.TeleportService;

/**
 * @author xTz
 */
public class DredgionReward extends InstanceReward<DredgionPlayerReward> {

    private int winnerPoints;
    private int looserPoints;
    private int drawPoins;
    private final MutableInt asmodiansPoints = new MutableInt(0);
    private final MutableInt elyosPoins = new MutableInt(0);
    private Race race;
    private final FastList<DredgionRooms> dredgionRooms = new FastList<>();
    private Point3D asmodiansStartPosition;
    private Point3D elyosStartPosition;

    public DredgionReward(Integer mapId, int instanceId) {
        super(mapId, instanceId);
        switch (mapId) {
            case 300110000:
                winnerPoints = DredgionConfig.WINNER_POINTS_DRED1;
                looserPoints = DredgionConfig.LOOSER_POINTS_DRED1;
                drawPoins = DredgionConfig.LOOSER_POINTS_DRED1;
                break;
            case 300210000:
                winnerPoints = DredgionConfig.WINNER_POINTS_DRED2;
                looserPoints = DredgionConfig.LOOSER_POINTS_DRED2;
                drawPoins = DredgionConfig.DRAW_POINTS_DRED2;
                break;
            case 300440000:
                winnerPoints = DredgionConfig.WINNER_POINTS_DRED3;
                looserPoints = DredgionConfig.LOOSER_POINTS_DRED3;
                drawPoins = DredgionConfig.DRAW_POINTS_DRED3;
                break;
            default:
                winnerPoints = DredgionConfig.WINNER_POINTS_DRED1;
                looserPoints = DredgionConfig.LOOSER_POINTS_DRED1;
                drawPoins = DredgionConfig.LOOSER_POINTS_DRED1;
        }
        setStartPositions();
        for (int i = 1; i < 15; i++) {
            dredgionRooms.add(new DredgionRooms(i));
        }
    }

    private void setStartPositions() {
        Point3D a = new Point3D(400.74f, 166.71f, 432.29f);
        Point3D b = new Point3D(570.46f, 166.89f, 432.28f);

            asmodiansStartPosition = a;
            elyosStartPosition = b;
    }

    public void portToPosition(Player player) {
        if (player.getRace() == Race.ASMODIANS) {
            TeleportService.teleportTo(player, mapId, instanceId, asmodiansStartPosition.getX(), asmodiansStartPosition.getY(), asmodiansStartPosition.getZ());
        } else {
            TeleportService.teleportTo(player, mapId, instanceId, elyosStartPosition.getX(), elyosStartPosition.getY(), elyosStartPosition.getZ());
        }
    }

    /**
     * 1 Primary Armory 2 Backup Armory 3 Gravity Control 4 Engine Room 5 Auxiliary Power 6 Weapons Deck 7 Lower Weapons Deck 8 Ready Room 1 9 Ready Room 2 10
     * Barracks 11 Logistics Managment 12 Logistics Storage 13 The Bridge 14 Captain's Room
     */
    public class DredgionRooms {

        private final int roomId;
        private int state = 0xFF;

        public DredgionRooms(int roomId) {
            this.roomId = roomId;
        }

        public int getRoomId() {
            return roomId;
        }

        public void captureRoom(Race race) {
            state = race.equals(Race.ASMODIANS) ? 0x01 : 0x00;
        }

        public int getState() {
            return state;
        }
    }

    public FastList<DredgionRooms> getDredgionRooms() {
        return dredgionRooms;
    }

    public DredgionRooms getDredgionRoomById(int roomId) {
        for (DredgionRooms dredgionRoom : dredgionRooms) {
            if (dredgionRoom.getRoomId() == roomId) {
                return dredgionRoom;
            }
        }
        return null;
    }

    public MutableInt getPointsByRace(Race race) {
        switch (race) {
            case ELYOS:
                return elyosPoins;
            case ASMODIANS:
                return asmodiansPoints;
        }
        return null;
    }

    public void addPointsByRace(Race race, int points) {
        MutableInt racePoints = getPointsByRace(race);
        racePoints.add(points);
        if (racePoints.intValue() < 0) {
            racePoints.setValue(0);
        }
    }

    public int getLooserPoints() {
        return looserPoints;
    }

    public int getWinnerPoints() {
        return winnerPoints;
    }

    public void setWinningRace(Race race) {
        this.race = race;
    }

    public Race getWinningRace() {
        return race;
    }

    public Race getWinningRaceByScore() {
        return asmodiansPoints.compareTo(elyosPoins) > 0 ? Race.ASMODIANS : Race.ELYOS;
    }

    @Override
    public void clear() {
        super.clear();
        dredgionRooms.clear();
    }
}
