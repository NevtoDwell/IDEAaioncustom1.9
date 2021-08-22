/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.toypet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.gameobjects.Pet;
import com.ne.gs.network.aion.serverpackets.SM_PET;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.utils.audit.AuditLogger;

/**
 * @author ATracer
 */
public final class PetMoodService {

    private static final Logger log = LoggerFactory.getLogger(PetMoodService.class);

    public static void checkMood(Pet pet, int type, int shuggleEmotion) {
        switch (type) {
            case 0:
                startCheckingMood(pet);
                break;
            case 1:
                interactWithPet(pet, shuggleEmotion);
                break;
            case 3:
                requestPresent(pet);
                break;
        }
    }

    /**
     * @param pet
     */
    private static void requestPresent(Pet pet) {
        if (pet.getCommonData().getMoodPoints(false) < 9000) {
            log.warn("Requested present before mood fill up: {}", pet.getMaster().getName());
            return;
        }

        if (pet.getCommonData().getGiftRemainingTime() > 0) {
            AuditLogger.info(pet.getMaster(), "Trying to get gift during CD for pet " + pet.getPetId());
            return;
        }

        if (pet.getMaster().getInventory().isFull()) {
            pet.getMaster().sendPck(SM_SYSTEM_MESSAGE.STR_WAREHOUSE_FULL_INVENTORY);
            return;
        }

        pet.getCommonData().clearMoodStatistics();
        pet.getMaster().sendPck(new SM_PET(pet, 4, 0));
        pet.getMaster().sendPck(new SM_PET(pet, 3, 0));
        int itemId = pet.getPetTemplate().getConditionReward();
        if (itemId != 0) {
            ItemService.addItem(pet.getMaster(), pet.getPetTemplate().getConditionReward(), 1);
        }
    }

    /**
     * @param pet
     * @param shuggleEmotion
     */
    private static void interactWithPet(Pet pet, int shuggleEmotion) {
        if (pet.getCommonData() != null) {
            if (pet.getCommonData().increaseShuggleCounter()) {
                pet.getMaster().sendPck(new SM_PET(pet, 2, shuggleEmotion));
                pet.getMaster().sendPck(new SM_PET(pet, 4, 0));
            }
        }
    }

    /**
     * @param pet
     */
    private static void startCheckingMood(Pet pet) {
        pet.getMaster().sendPck(new SM_PET(pet, 0, 0));
    }

}
