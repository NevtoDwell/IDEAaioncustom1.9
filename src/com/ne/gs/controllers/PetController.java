/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers;

import com.ne.gs.database.GDB;
import com.ne.gs.database.dao.PlayerPetsDAO;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.Pet;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_PET;

/**
 * @author ATracer
 */
public class PetController extends VisibleObjectController<Pet> {

    @Override
    public void see(VisibleObject object) {

    }

    @Override
    public void notSee(VisibleObject object, boolean isOutOfRange) {
    }

    public static class PetUpdateTask implements Runnable {

        private final Player player;
        private long startTime = 0;

        public PetUpdateTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }

            try {
                Pet pet = player.getPet();
                if (pet == null) {
                    throw new IllegalStateException("Pet is null");
                }

                int currentPoints = 0;
                boolean saved = false;

                if (pet.getCommonData().getMoodPoints(false) < 9000) {
                    if (System.currentTimeMillis() - startTime >= 60 * 1000) {
                        currentPoints = pet.getCommonData().getMoodPoints(false);
                        if (currentPoints == 9000) {
                            player.sendPck(new SM_PET(pet, 4, 0));
                        }

                        GDB.get(PlayerPetsDAO.class).savePetMoodData(pet.getCommonData());
                        saved = true;
                        startTime = System.currentTimeMillis();
                    }
                }

                if (currentPoints < 9000) {
                    player.sendPck(new SM_PET(pet, 4, 0));
                } else {
                    player.sendPck(new SM_PET(pet, 3, 0));
                    // Save if it reaches 100% after player snuggles the pet, not by the scheduler itself
                    if (!saved) {
                        GDB.get(PlayerPetsDAO.class).savePetMoodData(pet.getCommonData());
                    }
                }
            } catch (Exception ex) {
                player.getController().cancelTask(TaskId.PET_UPDATE);
            }
        }
    }

}
