/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.siegeservice;

import java.util.ArrayList;
import java.util.List;

import com.ne.commons.utils.Rnd;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.siege.SiegeModType;
import com.ne.gs.model.siege.SiegeRace;
import com.ne.gs.model.templates.npc.AbyssNpcType;
import com.ne.gs.model.templates.spawns.SpawnGroup2;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.world.knownlist.Visitor;

public class FortressAssault extends Assault<FortressSiege> {

    private final boolean isBalaurea;
    private boolean spawned = false;
    private List<float[]> spawnLocations;

    public FortressAssault(FortressSiege siege) {
        super(siege);
        isBalaurea = worldId != 400010000;
    }

    @Override
    protected void scheduleAssault(int delay) {
        dredgionTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                BalaurAssaultService.getInstance().spawnDredgion(getSpawnIdByFortressId());

                spawnTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

                    @Override
                    public void run() {
                        spawnAttackers();
                    }
                }, Rnd.get(240, 300) * 1000);
            }
        }, delay * 1000);
    }

    @Override
    protected void onAssaultFinish(boolean captured) {
        spawnLocations.clear();

        if (!spawned) {
            return;
        }
        if (!captured) {
            rewardDefendingPlayers();
        } else {
            siegeLocation.doOnAllPlayers(new Visitor<Player>() {

                @Override
                public void visit(Player player) {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_ABYSS_DRAGON_BOSS_KILLED);
                }
            });
        }
    }

    private void spawnAttackers() {
        if (spawned) {
            return;
        }
        spawned = true;
        float x = boss.getX();
        float y = boss.getY();
        float z = boss.getZ();
        int heading = boss.getSpawn().getHeading();
        int radius1 = isBalaurea ? 5 : Rnd.get(7, 13);
        int radius2 = isBalaurea ? 9 : Rnd.get(15, 20);
        int amount = isBalaurea ? Rnd.get(30, 50) : Rnd.get(20, 30);
        int templateId;
        SiegeSpawnTemplate spawn;

        float minAngle = MathUtil.convertHeadingToDegree(heading) - 90;
        if (minAngle < 0) {
            minAngle += 360;
        }
        double minRadian = Math.toRadians(minAngle);
        float interval = (float) (Math.PI / (amount / 2));
        float x1;
        float y1;

        List<Integer> idList = getSpawnIds();
        int commanderCount = isBalaurea ? 0 : Rnd.get(2);
        spawnRegularBalaurs();

        for (int i = 0; amount > i; i++) {
            if (i < (amount / 2)) {
                x1 = (float) (Math.cos(minRadian + interval * i) * radius1);
                y1 = (float) (Math.sin(minRadian + interval * i) * radius1);
            } else {
                x1 = (float) (Math.cos(minRadian + interval * (i - amount / 2)) * radius2);
                y1 = (float) (Math.sin(minRadian + interval * (i - amount / 2)) * radius2);
            }
            templateId = (i <= commanderCount) ? idList.get(0) : idList.get(Rnd.get(1, idList.size() - 1));

            Npc attaker;
            if ((i > Math.round(amount / 3)) && !spawnLocations.isEmpty()) {
                float[] coords = spawnLocations.get(Rnd.get(spawnLocations.size()));
                spawn = SpawnEngine.addNewSiegeSpawn(worldId, templateId, locationId, SiegeRace.BALAUR, SiegeModType.ASSAULT, coords[0], coords[1], coords[2],
                    heading);

                attaker = (Npc) SpawnEngine.spawnObject(spawn, 1);
                attaker.getSpawn().setX(x + x1);
                attaker.getSpawn().setY(y + y1);
                attaker.getSpawn().setZ(z);
            } else {
                spawn = SpawnEngine.addNewSiegeSpawn(worldId, templateId, locationId, SiegeRace.BALAUR, SiegeModType.ASSAULT, x + x1, y + y1, z, heading);
                SpawnEngine.spawnObject(spawn, 1);
            }
        }

        siegeLocation.doOnAllPlayers(new Visitor<Player>() {

            @Override
            public void visit(Player player) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_ABYSS_CARRIER_DROP_DRAGON);
            }
        });
        idList.clear();
    }

    private void spawnRegularBalaurs() {
        spawnLocations = new ArrayList<>();
        List<SpawnGroup2> siegeSpawns = DataManager.SPAWNS_DATA2.getSiegeSpawnsByLocId(locationId);
        for (SpawnGroup2 spawnGroup : siegeSpawns) {
            for (SpawnTemplate spawnTemplate : spawnGroup.getSpawnTemplates()) {
                SiegeSpawnTemplate temp = (SiegeSpawnTemplate) spawnTemplate;
                AbyssNpcType type = DataManager.NPC_DATA.getNpcTemplate(temp.getNpcId()).getAbyssNpcType();
                if (temp.getSiegeRace() != SiegeRace.BALAUR || !temp.isPeace() || type.equals(AbyssNpcType.ARTIFACT) || type.equals(AbyssNpcType.TELEPORTER)) {
                    continue;
                }
                float[] loc = {spawnTemplate.getX() + 2, spawnTemplate.getY() + 2, spawnTemplate.getZ()};
                SiegeSpawnTemplate spawn = SpawnEngine.addNewSiegeSpawn(spawnTemplate.getWorldId(), spawnTemplate.getNpcId(), locationId,
                    SiegeRace.BALAUR, SiegeModType.ASSAULT, loc[0], loc[1], loc[2], spawnTemplate.getHeading());

                VisibleObject attaker = SpawnEngine.spawnObject(spawn, 1);

                if (MathUtil.isIn3dRange(attaker, boss, isBalaurea ? 100 : 70)) {
                    spawnLocations.add(loc);
                }
            }
        }
    }

    private int getSpawnIdByFortressId() {
        switch (locationId) {
            case 2011:
                return 5;
            case 2021:
                return 6;
            case 3021:
                return 10;
            case 3011:
                return 11;
            case 1141:
                return 12;
            case 1221:
                return 13;
            case 1131:
                return 15;
            case 1132:
                return 14;
            case 1241:
                return 16;
            case 1231:
                return 17;
            case 1211:
                return 18;
            case 1251:
                return 19;
            case 1011:
                return 20;
        }
        return 1;
    }

    private List<Integer> getSpawnIds() {
        List<Integer> Spawns = new ArrayList<>();
        switch (locationId) {
            case 1131:
            case 1132:
            case 1141:
                Spawns.add(276649);
                Spawns.add(276767);
                Spawns.add(276766);
                Spawns.add(276720);
                Spawns.add(276719);
                Spawns.add(276717);
                Spawns.add(276715);
                Spawns.add(276714);
                Spawns.add(276710);
                Spawns.add(276709);
                Spawns.add(276699);
                Spawns.add(276690);
                Spawns.add(276689);
                Spawns.add(276687);
                Spawns.add(276684);
                Spawns.add(276680);
                Spawns.add(276679);
                Spawns.add(276675);
                Spawns.add(276674);
                Spawns.add(276672);
                Spawns.add(276670);
                Spawns.add(276669);
                Spawns.add(276668);
                Spawns.add(276667);
                Spawns.add(276665);
                Spawns.add(276664);
                Spawns.add(276663);
                Spawns.add(276660);
                Spawns.add(276659);
                Spawns.add(276658);
                Spawns.add(276655);
                Spawns.add(276654);
                Spawns.add(276653);
                Spawns.add(276645);
                Spawns.add(276644);
                Spawns.add(276643);
                Spawns.add(276640);
                Spawns.add(276639);
                Spawns.add(276638);
                Spawns.add(276635);
                Spawns.add(276634);
                Spawns.add(276633);
                Spawns.add(276632);
                Spawns.add(276630);
                Spawns.add(276629);
                Spawns.add(276628);
                return Spawns;
            case 1011:
            case 1211:
            case 1221:
            case 1231:
            case 1241:
            case 1251:
                Spawns.add(276871);
                Spawns.add(277037);
                Spawns.add(277036);
                Spawns.add(277034);
                Spawns.add(277033);
                Spawns.add(277022);
                Spawns.add(277021);
                Spawns.add(277020);
                Spawns.add(277019);
                Spawns.add(277017);
                Spawns.add(277016);
                Spawns.add(277015);
                Spawns.add(277014);
                Spawns.add(277012);
                Spawns.add(277011);
                Spawns.add(277007);
                Spawns.add(277006);
                Spawns.add(277002);
                Spawns.add(277001);
                Spawns.add(276999);
                Spawns.add(276992);
                Spawns.add(276991);
                Spawns.add(276990);
                Spawns.add(276989);
                Spawns.add(276987);
                Spawns.add(276986);
                Spawns.add(276982);
                Spawns.add(276981);
                Spawns.add(276977);
                Spawns.add(276976);
                Spawns.add(276972);
                Spawns.add(276971);
                Spawns.add(276970);
                Spawns.add(276953);
                Spawns.add(276952);
                Spawns.add(276951);
                Spawns.add(276950);
                Spawns.add(276949);
                Spawns.add(276948);
                Spawns.add(276947);
                Spawns.add(276946);
                Spawns.add(276945);
                Spawns.add(276943);
                Spawns.add(276942);
                Spawns.add(276941);
                Spawns.add(276940);
                Spawns.add(276933);
                Spawns.add(276932);
                Spawns.add(276931);
                Spawns.add(276930);
                Spawns.add(276929);
                Spawns.add(276913);
                Spawns.add(276912);
                Spawns.add(276911);
                Spawns.add(276910);
                Spawns.add(276909);
                Spawns.add(276893);
                Spawns.add(276892);
                Spawns.add(276891);
                Spawns.add(276890);
                Spawns.add(276889);
                Spawns.add(276888);
                Spawns.add(276887);
                Spawns.add(276886);
                Spawns.add(276885);
                Spawns.add(276884);
                Spawns.add(276883);
                Spawns.add(276882);
                Spawns.add(276881);
                Spawns.add(276880);
                Spawns.add(276879);
                Spawns.add(276878);
                Spawns.add(276877);
                Spawns.add(276876);
                Spawns.add(276875);
                Spawns.add(276874);
                Spawns.add(276873);
                Spawns.add(276868);
                Spawns.add(276867);
                Spawns.add(276866);
                Spawns.add(276865);
                Spawns.add(276863);
                Spawns.add(276862);
                Spawns.add(276861);
                Spawns.add(276860);
                Spawns.add(276858);
                Spawns.add(276857);
                Spawns.add(276856);
                Spawns.add(276855);
                Spawns.add(276853);
                Spawns.add(276852);
                Spawns.add(276851);
                Spawns.add(276850);
                Spawns.add(276700);
                return Spawns;
            case 2011:
            case 2021:
            case 3011:
            case 3021:
                Spawns.add(258236);
                Spawns.add(258246);
                Spawns.add(258245);
                Spawns.add(258244);
                Spawns.add(258243);
                Spawns.add(258242);
                Spawns.add(258241);
                Spawns.add(258240);
                Spawns.add(258239);
                Spawns.add(258238);
                Spawns.add(258237);
                return Spawns;
        }
        return Spawns;
    }

    private void rewardDefendingPlayers() {
    }
}
