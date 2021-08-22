/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects;

import com.ne.gs.controllers.StaticObjectController;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.model.templates.staticdoor.StaticDoorState;
import com.ne.gs.model.templates.staticdoor.StaticDoorTemplate;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.utils.PacketSendUtility;
import mw.engines.geo.GeoEngine;
import mw.engines.geo.scene.AionModel;

import java.util.EnumSet;

/**
 * @author MrPoke
 */
public class StaticDoor extends StaticObject {

    private final EnumSet<StaticDoorState> states;
    private AionModel model;

    /**
     * @param objectId
     * @param controller
     * @param spawnTemplate
     * @param objectTemplate
     */
    public StaticDoor(int objectId, StaticObjectController controller, SpawnTemplate spawnTemplate, StaticDoorTemplate objectTemplate,
                      int instanceId) {
        super(objectId, controller, spawnTemplate, objectTemplate);
        states = EnumSet.copyOf(getObjectTemplate().getInitialStates());
        if (objectTemplate.getMeshFile() != null) {
            model = GeoEngine.getDoorModel(
                    objectTemplate.getMeshFile(),
                    spawnTemplate.getWorldId(),
                    objectTemplate.getX(),
                    objectTemplate.getY(),
                    objectTemplate.getZ());
        }
    }

    /**
     * @return the open state from states set
     */
    public boolean isOpen() {
        return states.contains(StaticDoorState.OPENED);
    }

    public AionModel getModel() {
        return model;
    }

    public EnumSet<StaticDoorState> getStates() {
        return states;
    }

    /**
     * @param open the open state to set
     */
    public void setOpen(boolean open) {
        EmotionType emotion;
        int packetState = 0; // not important IMO, similar to internal state
        if (open) {
            emotion = EmotionType.OPEN_DOOR;
            states.remove(StaticDoorState.CLICKABLE);
            states.add(StaticDoorState.OPENED); // 1001
            packetState = 0x9;
        } else {
            emotion = EmotionType.CLOSE_DOOR;
            if (getObjectTemplate().getInitialStates().contains(StaticDoorState.CLICKABLE)) {
                states.add(StaticDoorState.CLICKABLE);
            }
            states.remove(StaticDoorState.OPENED); // 1010
            packetState = 0xA;
        }
        if (model != null) {
            if (open)
                model.deactivateAt(getInstanceId());
            else
                model.activateAt(getInstanceId());
        }
        // int stateFlags = StaticDoorState.getFlags(states);
        PacketSendUtility.broadcastPacket(this, new SM_EMOTION(this.getSpawn().getStaticId(), emotion, packetState));
    }

    public void changeState(boolean open, int state) {
        state &= 0xF;
        StaticDoorState.setStates(state, states);
        EmotionType emotion = open ? emotion = EmotionType.OPEN_DOOR : EmotionType.CLOSE_DOOR;
        PacketSendUtility.broadcastPacket(this, new SM_EMOTION(this.getSpawn().getStaticId(), emotion, state));
    }

    @Override
    public StaticDoorTemplate getObjectTemplate() {
        return (StaticDoorTemplate) super.getObjectTemplate();
    }

}
