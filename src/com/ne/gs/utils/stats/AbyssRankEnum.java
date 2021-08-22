/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils.stats;

import javax.xml.bind.annotation.XmlEnum;

import com.ne.gs.configs.main.RateConfig;
import com.ne.gs.model.DescId;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author ATracer
 * @author Sarynth
 * @author Imaginary
 */
@XmlEnum
public enum AbyssRankEnum {
    GRADE9_SOLDIER		(1, 120, 24, 0, 0, 1802431),
    GRADE8_SOLDIER		(2, 168, 37, 1200, 0, 1802433),
    GRADE7_SOLDIER		(3, 235, 58, 4220, 0, 1802435),
    GRADE6_SOLDIER		(4, 329, 91, 10990, 0, 1802437),
    GRADE5_SOLDIER		(5, 461, 143, 23500, 0, 1802439),
    GRADE4_SOLDIER		(6, 645, 225, 42780, 0, 1802441),
    GRADE3_SOLDIER		(7, 903, 356, 69700, 0, 1802443),
    GRADE2_SOLDIER		(8, 1264, 561, 105600, 0, 1802445),
    GRADE1_SOLDIER		(9, 1770, 885, 150800, 0, 1802447),
    STAR1_OFFICER		(10, 2124, 1195, 214100, 1000, 1802449),
    STAR2_OFFICER		(11, 2549, 1616, 278700, 700, 1802451),
    STAR3_OFFICER		(12, 3059, 2181, 344500, 500, 1802453),
    STAR4_OFFICER		(13, 3671, 2949, 411700, 300, 1802455),
    STAR5_OFFICER		(14, 4405, 3981, 488200, 100, 1802457),
    GENERAL		 		(15, 5286, 5374, 565400, 30, 1802459),
    GREAT_GENERAL		(16, 6343, 7258, 643200, 10, 1802461),
    COMMANDER			(17, 7612, 9799, 721600, 3, 1802463),
    SUPREME_COMMANDER	(18, 9134, 13229, 800700, 1, 1802465);

    private final int id;
    private final int pointsGained;
    private final int pointsLost;
    private final int required;
    private final int quota;
    private final int descriptionId;

    /**
     * @param id
     * @param pointsGained
     * @param pointsLost
     * @param required
     * @param quota
     */
    private AbyssRankEnum(int id, int pointsGained, int pointsLost, int required,
                          int quota, int descriptionId) {
        this.id = id;
        this.pointsGained = pointsGained;
        this.pointsLost = pointsLost;
        this.required = required * RateConfig.ABYSS_RANK_RATE;
        this.quota = quota;
        this.descriptionId = descriptionId;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the pointsLost
     */
    public int getPointsLost() {
        return pointsLost;
    }

    /**
     * @return the pointsGained
     */
    public int getPointsGained() {
        return pointsGained;
    }

    /**
     * @return AP required for Rank
     */
    public int getRequired() {
        return required;
    }

    /**
     * @return The quota is the maximum number of allowed player to have the rank
     */
    public int getQuota() {
        return quota;
    }

    public int getDescriptionId() {
        return descriptionId;
    }

    public static DescId getRankDescriptionId(Player player) {
        int pRankId = player.getAbyssRank().getRank().getId();
        for (AbyssRankEnum rank : values()) {
            if (rank.getId() == pRankId) {
                int descId = rank.getDescriptionId();
                return (player.getRace() == Race.ELYOS) ? DescId.of(descId) : DescId.of(descId + 36);
            }
        }
        throw new IllegalArgumentException("No rank Description Id found for player: " + player);
    }

    /**
     * @param id
     *
     * @return The abyss rank enum by his id
     */
    public static AbyssRankEnum getRankById(int id) {
        for (AbyssRankEnum rank : values()) {
            if (rank.getId() == id) {
                return rank;
            }
        }
        throw new IllegalArgumentException("Invalid abyss rank provided" + id);
    }

    /**
     * @param ap
     *
     * @return The abyss rank enum for his needed ap
     */
    public static AbyssRankEnum getRankForAp(int ap) {
        AbyssRankEnum r = AbyssRankEnum.GRADE9_SOLDIER;
        for (AbyssRankEnum rank : values()) {
            if (rank.getRequired() <= ap) {
                r = rank;
            } else {
                break;
            }
        }
        return r;
    }
}
