/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.toypet;

import java.sql.Timestamp;

import com.ne.gs.database.GDB;
import com.ne.gs.configs.main.PeriodicSaveConfig;
import com.ne.gs.controllers.PetController;
import com.ne.gs.database.dao.PlayerPetsDAO;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.Pet;
import com.ne.gs.model.gameobjects.player.PetCommonData;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.storage.StorageType;
import com.ne.gs.model.templates.pet.PetDopingBag;
import com.ne.gs.model.templates.pet.PetFunction;
import com.ne.gs.model.templates.pet.PetTemplate;
import com.ne.gs.network.aion.serverpackets.SM_PET;
import com.ne.gs.network.aion.serverpackets.SM_WAREHOUSE_INFO;
import com.ne.gs.spawnengine.VisibleObjectSpawner;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
public final class PetSpawnService {

    /**
     * @param player
     * @param petId
     */
    public static void summonPet(Player player, int petId, boolean isManualSpawn) {
        PetCommonData lastPetCommonData;

        if (player.getPet() != null) {
            if (player.getPet().getPetId() == petId) {
                PacketSendUtility.broadcastPacket(player, new SM_PET(3, player.getPet()), true);
                return;
            }

            lastPetCommonData = player.getPet().getCommonData();
            dismissPet(player, isManualSpawn);
        } else {
            lastPetCommonData = player.getPetList().getLastUsedPet();
        }

        if (lastPetCommonData != null) {
            // reset mood if other pet is spawned
            if (petId != lastPetCommonData.getPetId()) {
                lastPetCommonData.clearMoodStatistics();
            }
        }

        player.getController().addTask(
            TaskId.PET_UPDATE,
            ThreadPoolManager.getInstance().scheduleAtFixedRate(new PetController.PetUpdateTask(player), PeriodicSaveConfig.PLAYER_PETS * 1000,
                PeriodicSaveConfig.PLAYER_PETS * 1000));

        Pet pet = VisibleObjectSpawner.spawnPet(player, petId);
        // It means serious error or cheater - why its just nothing say "null"?
        if (pet != null) {
            sendWhInfo(player, petId);

            if (System.currentTimeMillis() - pet.getCommonData().getDespawnTime().getTime() > 10 * 60 * 1000) {
                // reset mood if pet was despawned for longer than 10 mins.
                player.getPet().getCommonData().clearMoodStatistics();
            }

            lastPetCommonData = pet.getCommonData();
            player.getPetList().setLastUsedPetId(petId);
        }
    }

    /**
     * @param player
     * @param petId
     */
    private static void sendWhInfo(Player player, int petId) {
        PetTemplate petTemplate = DataManager.PET_DATA.getPetTemplate(petId);
        PetFunction pf = petTemplate.getWarehouseFunction();
        if ((pf != null) && (pf.getSlots() != 0)) {
            int itemLocation = StorageType.getStorageId(pf.getSlots(), 6);
            if (itemLocation != -1) {
                player.sendPck(new SM_WAREHOUSE_INFO(player.getStorage(itemLocation).getItemsWithKinah(), itemLocation, 0, true, player));

                player.sendPck(new SM_WAREHOUSE_INFO(null, itemLocation, 0, false, player));
            }
        }
    }

    /**
     * @param player
     * @param isManualDespawn
     */
    public static void dismissPet(Player player, boolean isManualDespawn) {
        Pet toyPet = player.getPet();
        if (toyPet != null) {
            PetFeedProgress progress = toyPet.getCommonData().getFeedProgress();
            if (progress != null) {
                toyPet.getCommonData().setCancelFeed(true);
                GDB.get(PlayerPetsDAO.class).saveFeedStatus(player, toyPet.getPetId(), progress.getHungryLevel().getValue(),
                    progress.getDataForPacket(), progress.getTotalPoints(), toyPet.getCommonData().getCurentTime());
            }
            PetDopingBag bag = toyPet.getCommonData().getDopingBag();
            if (bag != null && bag.isDirty()) {
                GDB.get(PlayerPetsDAO.class).saveDopingBag(player, toyPet.getPetId(), bag);
            }

            player.getController().cancelTask(TaskId.PET_UPDATE);

            // TODO needs for pet teleportation
            if (isManualDespawn) {
                toyPet.getCommonData().setDespawnTime(new Timestamp(System.currentTimeMillis()));
            }

            toyPet.getCommonData().savePetMoodData();

            player.setToyPet(null);
            toyPet.getController().delete();
        }

    }
}
