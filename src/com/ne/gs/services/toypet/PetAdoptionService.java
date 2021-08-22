/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.toypet;

import com.ne.gs.taskmanager.tasks.ExpireTimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.player.PetCommonData;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.network.aion.serverpackets.SM_PET;

/**
 * @author ATracer
 */
public final class PetAdoptionService {

    private static final Logger log = LoggerFactory.getLogger(PetAdoptionService.class);

    /**
     * Create a pet for player (with validation)
     *
     * @param player
     * @param eggObjId
     * @param petId
     * @param name
     * @param decorationId
     */
    public static void adoptPet(Player player, int eggObjId, int petId, String name, int decorationId) {

        int eggId = player.getInventory().getItemByObjId(eggObjId).getItemId();
        ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(eggId);
        if (template == null || template.getFuncPetId() != petId) {
            return;
        }

        if(!player.getInventory().decreaseByObjectId(eggObjId, 1))
            return;

        addPet(player, petId, template.getPetExpire(), name, decorationId);
    }

    /**
     * Add pet to player
     *
     * @param player
     * @param petId
     * @param name
     * @param decorationId
     */
    public static void addPet(Player player, int petId, int lifeTime,  String name, int decorationId) {
        if (player.getPetList().hasPet(petId)) {
            log.warn("Duplicate pet adoption");
            return;
        }
        PetCommonData petCommonData = player.getPetList().addPet(player, petId, decorationId, name, lifeTime);
        if (petCommonData != null) {
            player.sendPck(new SM_PET(1, petCommonData));

            if (lifeTime > 0) {
                ExpireTimerTask.getInstance().addTask(petCommonData, player);
            }
        }
    }

    /**
     * Delete pet
     *
     * @param player
     * @param petId
     */
    public static void surrenderPet(Player player, int petId) {
        PetCommonData petCommonData = player.getPetList().getPet(petId);
        if (player.getPet() != null && player.getPet().getPetId() == petCommonData.getPetId()) {
            if (petCommonData.getFeedProgress() != null) {
                petCommonData.setCancelFeed(true);
            }
            PetSpawnService.dismissPet(player, false);
        }
        player.getPetList().deletePet(petCommonData.getPetId());
        player.sendPck(new SM_PET(2, petCommonData));
    }

}
