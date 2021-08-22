/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.housing;

import com.ne.gs.controllers.NpcController;
import com.ne.gs.controllers.effect.EffectController;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.utils.idfactory.IDFactory;
import com.ne.gs.world.knownlist.PlayerAwareKnownList;

import static com.ne.gs.modules.housing.Housing.housing;

/**
 * @author hex1r0
 */
public final class Spawner {

    static void spawnHouses() {
        for (House.HouseTemplate tpl : Housing.HOUSE_TEMPLATES.values()) {
            if (tpl.getType() != House.HouseType.FLAT) {
                housing().getHousesByHouseId().put(tpl.getHouseId(), new House(tpl));
                spawnTrigger(tpl);
            }
        }
    }

    static void spawnTrigger(House.HouseTemplate tpl) {
        House.Trigger t = House.Trigger.create(IDFactory.getInstance().nextId(), tpl);
        SpawnEngine.bringIntoWorld(t, tpl.getMapId(), 1, tpl.getX(), tpl.getY(), tpl.getZ(), (byte) 0);

        House h = housing().getHouseByTpl(tpl);

        housing().getTriggers().put(tpl.getHouseId(), t);

        spawnSign(tpl, h.getState());
    }

    static void spawnSign(House.HouseTemplate tpl, House.State state) {
        int npcId = Housing.findSignNpcId(tpl, state);

        House.Trigger tr = housing().getTrigger(tpl);

        Npc sign = tr.getSign();
        if (sign != null) {
            sign.getController().onDelete();
        }

        tr.setSign(spawn(tr, npcId, tpl.getMapId(), tpl.getSignX(), tpl.getSignY(), tpl.getSignZ(), tpl.getSignH()));
    }

    static void spawnManager(House.HouseTemplate tpl) {
        House.Trigger tr = housing().getTrigger(tpl);

        tr.setManager(spawn(tr, Housing.findManagerId(tpl), tpl.getMapId(), tpl.getManagerX(), tpl.getManagerY(), tpl
            .getManagerZ(), tpl.getManagerH()));
    }

    static void spawnPortal(House.HouseTemplate tpl) {
        House.Trigger tr = housing().getTrigger(tpl);

        tr.setPortal(spawn(tr, Housing.findPorlalId(tpl), tpl.getMapId(), tpl.getPortalX(), tpl.getPortalY(), tpl
            .getPortalZ(), tpl.getPortalH()));
    }

    static void despawnManager(House.HouseTemplate tpl) {
        House.Trigger tr = housing().getTrigger(tpl);

        Npc npc = tr.getManager();
        if (npc != null) {
            tr.getController().onDelete();
        }

        tr.setManager(null);
    }

    static void despawnPortal(House.HouseTemplate tpl) {
        House.Trigger tr = housing().getTrigger(tpl);

        Npc npc = tr.getPortal();
        if (npc != null) {
            tr.getController().onDelete();
        }

        tr.setPortal(null);
    }

    private static Npc spawn(House.Trigger tr, int npcId, int mapId, float x, float y,
                             float z, byte h) {
        SpawnTemplate stpl = SpawnEngine.addNewSingleTimeSpawn(mapId, npcId, x, y, z, h);

        NpcTemplate ntpl = DataManager.NPC_DATA.getNpcTemplate(npcId);
        if (ntpl == null) {
            throw new NullPointerException();
        }

        Npc s = new Npc(
            IDFactory.getInstance().nextId(),
            new NpcController(), stpl,
            ntpl,
            (byte) 0);

        s.setKnownlist(new PlayerAwareKnownList(s));
        s.setEffectController(new EffectController(s));
        s.setCreatorId(tr.getHouseTemplate().getHouseId());
        s.setMasterName("");

        SpawnEngine.bringIntoWorld(s, stpl, 1);

        return s;
    }
}
