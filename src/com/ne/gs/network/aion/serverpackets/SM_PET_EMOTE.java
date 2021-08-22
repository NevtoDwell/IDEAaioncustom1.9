/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.Pet;
import com.ne.gs.model.gameobjects.PetEmote;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author ATracer
 */
public class SM_PET_EMOTE extends AionServerPacket {

    private final Pet pet;
    private final PetEmote emote;
    private final float x, y, z, x2, y2, z2;
    private final byte heading;
    private int emotionId, param1;

    public SM_PET_EMOTE(Pet pet, PetEmote emote) {
        this(pet, emote, 0, 0, 0, (byte) 0);
    }

    public SM_PET_EMOTE(Pet pet, PetEmote emote, float x, float y, float z, byte h) {
        this(pet, emote, x, y, z, 0, 0, 0, h);
    }

    public SM_PET_EMOTE(Pet pet, PetEmote emote, float x, float y, float z, float x2, float y2, float z2,
                        byte h) {
        this.pet = pet;
        this.emote = emote;
        this.x = x;
        this.y = y;
        this.z = z;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        heading = h;
    }

    public SM_PET_EMOTE(Pet pet, PetEmote emote, int emotionId, int param1) {
        this(pet, emote);
        this.emotionId = emotionId;
        this.param1 = param1;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeD(pet.getObjectId());
        writeC(emote.getEmoteId());
        switch (emote) {
            case MOVE_STOP:
                writeF(x);
                writeF(y);
                writeF(z);
                writeC(heading);
                break;
            case MOVETO:
                writeF(x);
                writeF(y);
                writeF(z);
                writeC(heading);
                writeF(x2);
                writeF(y2);
                writeF(z2);
                break;
            default:
                writeC(emotionId);
                writeC(param1); // happinessAdded?
                break;
        }
    }
}