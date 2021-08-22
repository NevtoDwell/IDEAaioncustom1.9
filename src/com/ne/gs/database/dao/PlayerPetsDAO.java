/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.dao;

import java.util.List;

import com.ne.commons.database.dao.DAO;
import com.ne.gs.model.gameobjects.player.PetCommonData;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.pet.PetDopingBag;

/**
 * @author Xitanium, Kamui, Rolandas
 */
public abstract class PlayerPetsDAO implements DAO {

    @Override
    public final String getClassName() {
        return PlayerPetsDAO.class.getName();
    }

    public abstract void insertPlayerPet(PetCommonData petCommonData);

    public abstract void removePlayerPet(Player player, int petId);

    public abstract void updatePetName(PetCommonData petCommonData);

    public abstract List<PetCommonData> getPlayerPets(Player player);

    public abstract void setTime(Player player, int petId, long time);

    public abstract void saveFeedStatus(Player paramPlayer, int paramInt1, int paramInt2, int paramInt3, int paramInt4, long paramLong);

    public abstract boolean savePetMoodData(PetCommonData petCommonData);

    public abstract void saveDopingBag(Player paramPlayer, int paramInt, PetDopingBag paramPetDopingBag);
}
