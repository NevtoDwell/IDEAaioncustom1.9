/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.model;

/**
 * @author MrPoke
 */

public class QuestVars {

    /**
     * Sorry =(
     * No wanna rework half of quest system right now
     */
    public static class QuestWarsTenbits extends QuestVars{

        public QuestWarsTenbits(int var) {
            super(var);
        }

        @Override
        public int getQuestVars() {
            int var = 0;
            for (int i = 3; i >= 0; i--) {
                var <<= 0xA;
                var |= questVars[i];
            }
            return var;
        }

        @Override
        public void setVar(int var) {
            for (int i = 0; i <= 3; i++) {
                questVars[i] = var & 0x3FF;
                var >>= 0xA;
            }
        }
    }

    protected final Integer[] questVars = new Integer[6];


    public QuestVars(int var) {
        setVar(var);
    }

    /**
     * @param id
     *
     * @return Quest var by id.
     */
    public int getVarById(int id) {
        return questVars[id];
    }

    /**
     * @param id
     * @param var
     */
    public void setVarById(int id, int var) {
        questVars[id] = var;
    }

    /**
     * @return int value of all values, stored in the array.
     *         Representation: Sum(value_on_index_i * 64^i)
     */
    public int getQuestVars() {
        int var = 0;
        for (int i = 5; i >= 0; i--) {
            var <<= 0x06; //shif to 10 bits if any variable greater than 63
            var |= questVars[i];
        }
        return var;
    }

    /**
     * Fill the array with values, based on
     *
     */
    public void setVar(int var) {
        for (int i = 0; i <= 5; i++) {
            questVars[i] = var & 0x3F;
            var >>= 0x06;
        }
    }
}
