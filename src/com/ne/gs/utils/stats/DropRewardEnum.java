package com.ne.gs.utils.stats;

public enum DropRewardEnum {

    MINUS_10(10, 0),
    MINUS_9(9, 39),
    MINUS_8(8, 79),
    MINUS_7(7, 100);

    private final int dropRewardPercent;

    private final int levelDifference;

    private DropRewardEnum(int levelDifference, int dropRewardPercent) {
        this.levelDifference = levelDifference;
        this.dropRewardPercent = dropRewardPercent;
    }

    public int rewardPercent() {
        return dropRewardPercent;
    }

    /**
     * @param levelDifference between two objects
     * @return Drop reward percentage
     */
    public static int dropRewardFrom(int levelDifference) {
        if (levelDifference >= MINUS_10.levelDifference) {
            return MINUS_10.dropRewardPercent;
        } else if (levelDifference == MINUS_9.levelDifference) {
            return MINUS_9.dropRewardPercent;
        } else if (levelDifference == MINUS_8.levelDifference) {
            return MINUS_8.dropRewardPercent;
        } else if (levelDifference <= MINUS_7.levelDifference) {
            return MINUS_7.dropRewardPercent;
        }
        return 100;
    }
}
