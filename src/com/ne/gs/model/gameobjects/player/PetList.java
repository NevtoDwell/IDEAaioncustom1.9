/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import com.ne.gs.database.GDB;
import javolution.util.FastMap;

import com.ne.gs.database.dao.PlayerPetsDAO;

/**
 * @author ATracer
 */
public class PetList {

    private final Player player;
    private int lastUsedPetId;

    private final FastMap<Integer, PetCommonData> pets = new FastMap<>();

    PetList(Player player) {
        this.player = player;
        //loadPets();
    }

    public void loadPets() {
        List<PetCommonData> playerPets = GDB.get(PlayerPetsDAO.class).getPlayerPets(player);
        PetCommonData lastUsedPet = null;
        for (PetCommonData pet : playerPets) {
            pets.put(pet.getPetId(), pet);
            if (lastUsedPet == null || pet.getDespawnTime().after(lastUsedPet.getDespawnTime())) {
                lastUsedPet = pet;
            }
        }

        if (lastUsedPet != null) {
            lastUsedPetId = lastUsedPet.getPetId();
        }
    }

    public Collection<PetCommonData> getPets() {
        return pets.values();
    }

    /**
     * @param petId
     *
     * @return
     */
    public PetCommonData getPet(int petId) {
        return pets.get(petId);
    }

    public PetCommonData getLastUsedPet() {
        return getPet(lastUsedPetId);
    }

    public void setLastUsedPetId(int lastUsedPetId) {
        this.lastUsedPetId = lastUsedPetId;
    }

    /**
     * @param player
     * @param petId
     * @param decorationId
     * @param name
     *
     * @return
     */
    public PetCommonData addPet(Player player, int petId, int decorationId, String name, int lifeTime) {
        return addPet(player, petId, decorationId, System.currentTimeMillis(), name, lifeTime);
    }

    public PetCommonData addPet(Player player, int petId, int decorationId, long birthday, String name, int lifeTime) {
        PetCommonData petCommonData = new PetCommonData(petId, player.getObjectId());
        petCommonData.setDecoration(decorationId);
        petCommonData.setName(name);
        petCommonData.setBirthday(new Timestamp(birthday));
        petCommonData.setLifeTime(lifeTime);
        petCommonData.setDespawnTime(new Timestamp(System.currentTimeMillis()));
        GDB.get(PlayerPetsDAO.class).insertPlayerPet(petCommonData);
        pets.put(petId, petCommonData);
        return petCommonData;
    }

    /**
     * @param petId
     *
     * @return
     */
    public boolean hasPet(int petId) {
        return pets.containsKey(petId);
    }

    /**
     * @param petId
     */
    public void deletePet(int petId) {
        if (hasPet(petId)) {
            pets.remove(petId);
            GDB.get(PlayerPetsDAO.class).removePlayerPet(player, petId);
        }
    }
}
