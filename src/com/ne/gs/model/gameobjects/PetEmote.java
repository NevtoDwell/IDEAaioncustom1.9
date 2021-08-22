/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author ATracer
 */
public enum PetEmote {

    MOVE_STOP(0),
    MOVETO(12),
    ALARM(-114),
    UNK_M110(-110),
    UNK_M111(-111),
    UNK_M123(-123),
    FLY(-125),
    UNK_M128(-128),
    UNKNOWN(255);

    private static final TIntObjectHashMap<PetEmote> petEmotes;

    static {
        petEmotes = new TIntObjectHashMap<>();
        for (PetEmote emote : values()) {
            petEmotes.put(emote.getEmoteId(), emote);
        }
    }

    private final int emoteId;

    private PetEmote(int emoteId) {
        this.emoteId = emoteId;
    }

    public int getEmoteId() {
        return emoteId;
    }

    public static PetEmote getEmoteById(int emoteId) {
        PetEmote emote = petEmotes.get(emoteId);
        return emote != null ? emote : UNKNOWN;
    }
}
