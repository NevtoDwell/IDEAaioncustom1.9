/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.siege;

import java.util.Iterator;

import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_INFLUENCE_RATIO;
import com.ne.gs.services.SiegeService;
import com.ne.gs.world.World;

/**
 * Calculates fortresses as 10 points and artifacts as 1 point each. Need to find retail calculation. (Upper forts worth more...)
 *
 * @author Sarynth
 */
public class Influence {

    private static final Influence instance = new Influence();

    private float inggison_e = 0;
    private float inggison_a = 0;
    private float inggison_b = 0;
    private float gelkmaros_e = 0;
    private float gelkmaros_a = 0;
    private float gelkmaros_b = 0;
    private float abyss_e = 0;
    private float abyss_a = 0;
    private float abyss_b = 0;
    //    private final float tiamaranta_e = 0;
    //    private final float tiamaranta_a = 0;
    //    private final float tiamaranta_b = 0;
    private float global_e = 0;
    private float global_a = 0;
    private float global_b = 0;

    private Influence() {
        calculateInfluence();
    }

    public static Influence getInstance() {
        return instance;
    }

    /**
     * Recalculates Influence and Broadcasts new values
     */
    public void recalculateInfluence() {
        calculateInfluence();

    }

    /**
     * calculate influence
     */
    private void calculateInfluence() {
        float balaurea = 0.001951219f;
        float abyss = 0.006097561f;
        float e_inggison = 0f;
        float a_inggison = 0f;
        float b_inggison = 0f;
        float t_inggison = 0f;
        float e_gelkmaros = 0f;
        float a_gelkmaros = 0f;
        float b_gelkmaros = 0f;
        float t_gelkmaros = 0f;
        float e_abyss = 0f;
        float a_abyss = 0f;
        float b_abyss = 0f;
        float t_abyss = 0f;

        for (SiegeLocation sLoc : SiegeService.getInstance().getSiegeLocations().values()) {
            switch (sLoc.getWorldId()) {
                case 210050000:
                    if (sLoc instanceof FortressLocation) {
                        t_inggison += sLoc.getInfluenceValue();
                        switch (sLoc.getRace()) {
                            case ELYOS:
                                e_inggison += sLoc.getInfluenceValue();
                                break;
                            case ASMODIANS:
                                a_inggison += sLoc.getInfluenceValue();
                                break;
                            case BALAUR:
                                b_inggison += sLoc.getInfluenceValue();
                        }
                    }
                    break;
                case 220070000:
                    if (sLoc instanceof FortressLocation) {
                        t_gelkmaros += sLoc.getInfluenceValue();
                        switch (sLoc.getRace()) {
                            case ELYOS:
                                e_gelkmaros += sLoc.getInfluenceValue();
                                break;
                            case ASMODIANS:
                                a_gelkmaros += sLoc.getInfluenceValue();
                                break;
                            case BALAUR:
                                b_gelkmaros += sLoc.getInfluenceValue();
                        }
                    }
                    break;
                case 400010000:
                    t_abyss += sLoc.getInfluenceValue();
                    switch (sLoc.getRace()) {
                        case ELYOS:
                            e_abyss += sLoc.getInfluenceValue();
                            break;
                        case ASMODIANS:
                            a_abyss += sLoc.getInfluenceValue();
                            break;
                        case BALAUR:
                            b_abyss += sLoc.getInfluenceValue();
                    }
                    break;
            }
        }

        inggison_e = (e_inggison / t_inggison);
        inggison_a = (a_inggison / t_inggison);
        inggison_b = (b_inggison / t_inggison);

        gelkmaros_e = (e_gelkmaros / t_gelkmaros);
        gelkmaros_a = (a_gelkmaros / t_gelkmaros);
        gelkmaros_b = (b_gelkmaros / t_gelkmaros);

        abyss_e = (e_abyss / t_abyss);
        abyss_a = (a_abyss / t_abyss);
        abyss_b = (b_abyss / t_abyss);

        global_e = (inggison_e * balaurea + gelkmaros_e * balaurea + abyss_e * abyss) * 100f;
        global_a = (inggison_a * balaurea + gelkmaros_a * balaurea + abyss_a * abyss) * 100f;
        global_b = (inggison_b * balaurea + gelkmaros_b * balaurea + abyss_b * abyss) * 100f;
    }

    @SuppressWarnings("unused")
    private void broadcastInfluencePacket() {
        SM_INFLUENCE_RATIO pkt = new SM_INFLUENCE_RATIO();

        Iterator<Player> iter = World.getInstance().getPlayersIterator();
        while (iter.hasNext()) {
            Player player = iter.next();
            player.sendPck(pkt);
        }
    }

    public float getGlobalElyosInfluence() {
        return global_e;
    }

    public float getGlobalAsmodiansInfluence() {
        return global_a;
    }

    public float getGlobalBalaursInfluence() {
        return global_b;
    }

    public float getInggisonElyosInfluence() {
        return inggison_e;
    }

    public float getInggisonAsmodiansInfluence() {
        return inggison_a;
    }

    public float getInggisonBalaursInfluence() {
        return inggison_b;
    }

    public float getGelkmarosElyosInfluence() {
        return gelkmaros_e;
    }

    public float getGelkmarosAsmodiansInfluence() {
        return gelkmaros_a;
    }

    public float getGelkmarosBalaursInfluence() {
        return gelkmaros_b;
    }

    public float getAbyssElyosInfluence() {
        return abyss_e;
    }

    public float getAbyssAsmodiansInfluence() {
        return abyss_a;
    }

    public float getAbyssBalaursInfluence() {
        return abyss_b;
    }

    public float getTiamarantaElyosInfluence() {
        return 0;//tiamaranta_e;
    }

    public float getTiamarantaAsmodiansInfluence() {
        return 0;//tiamaranta_a;
    }

    public float getTiamarantaBalaursInfluence() {
        return 0;//tiamaranta_b;
    }

    public float getPvpRaceBonus(Race attRace) {
        float bonus = 1;
        float elyos = getGlobalElyosInfluence();
        float asmo = getGlobalAsmodiansInfluence();
        switch (attRace) {
            case ASMODIANS:
                if (elyos >= 0.81f && asmo <= 0.10f) {
                    bonus = 1.2f;
                } else if (elyos >= 0.81f || (elyos >= 0.71f && asmo <= 0.10f)) {
                    bonus = 1.15f;
                } else if (elyos >= 0.71f) {
                    bonus = 1.1f;
                }
                break;
            case ELYOS:
                if (asmo >= 0.81f && elyos <= 0.10f) {
                    bonus = 1.2f;
                } else if (asmo >= 0.81f || (asmo >= 0.71f && elyos <= 0.10f)) {
                    bonus = 1.15f;
                } else if (asmo >= 0.71f) {
                    bonus = 1.1f;
                }
                break;
        }
        return bonus;
    }
}
