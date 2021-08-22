/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.Collection;
import org.slf4j.LoggerFactory;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Pet;
import com.ne.gs.model.gameobjects.PetAction;
import com.ne.gs.model.gameobjects.player.PetCommonData;
import com.ne.gs.model.templates.pet.PetDopingEntry;
import com.ne.gs.model.templates.pet.PetFunctionType;
import com.ne.gs.model.templates.pet.PetTemplate;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author M@xx, xTz, Rolandas
 */
public class SM_PET extends AionServerPacket {

    private final int actionId;
    private Pet pet;
    private PetCommonData commonData;
    private int itemObjectId;
    private Collection<PetCommonData> pets;
    private int count;
    private int subType;
    private int shuggleEmotion;

    private boolean isActing;
    private int lootNpcId;
    private int dopeAction;
    private int dopeSlot;

    public SM_PET(int subType, int actionId, int objectId, int count, Pet pet) {
        this.subType = subType;
        this.actionId = actionId;
        this.count = count;
        itemObjectId = objectId;
        this.pet = pet;
        commonData = pet.getCommonData();
    }

    public SM_PET(int actionId) {
        this.actionId = actionId;
    }

    public SM_PET(int actionId, Pet pet) {
        this(0, actionId, 0, 0, pet);
    }

    public SM_PET(boolean isLooting) {
        actionId = 13;
        isActing = isLooting;
        subType = 3;
    }

    public SM_PET(boolean isLooting, int npcId) {
        this(isLooting);
        lootNpcId = npcId;
    }

    public SM_PET(int dopeAction, boolean isBuffing) {
        actionId = 13;
        this.dopeAction = dopeAction;
        isActing = isBuffing;
        subType = 2;
    }

    public SM_PET(int dopeAction, int itemId, int slot) {
        this(dopeAction, true);
        itemObjectId = itemId;
        dopeSlot = slot;
    }

    /**
     * For mood only
     *
     * @param pet
     * @param shuggleEmotion
     */
    public SM_PET(Pet pet, int subType, int shuggleEmotion) {
        this(0, PetAction.MOOD.getActionId(), 0, 0, pet);
        this.shuggleEmotion = shuggleEmotion;
        this.subType = subType;
    }

    /**
     * For adopt only
     *
     * @param actionId
     * @param commonData
     */
    public SM_PET(int actionId, PetCommonData commonData) {
        this.actionId = actionId;
        this.commonData = commonData;
    }

    /**
     * For listing all pets on this character
     *
     * @param actionId
     * @param pets
     */
    public SM_PET(int actionId, Collection<PetCommonData> pets) {
        this.actionId = actionId;
        this.pets = pets;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        PetTemplate petTemplate = null;
        writeH(actionId);
        switch (actionId) {
            case 0:
                // load list on login
                writeC(0); // unk
                writeH(pets.size());
                for (PetCommonData petCommonData : pets) {
                    petTemplate = DataManager.PET_DATA.getPetTemplate(petCommonData.getPetId());

                    if (petTemplate == null) {
                        LoggerFactory.getLogger(SM_PET.class).error("unexisting pet template: " + petCommonData.getMasterObjectId() + " " + petCommonData.getPetId());
                        continue;
                    }

                    writeS(petCommonData.getName());
                    writeD(petCommonData.getPetId());
                    writeD(petCommonData.getObjectId());
                    writeD(petCommonData.getMasterObjectId());
                    writeD(0);
                    writeD(0);
                    writeD(petCommonData.getBirthday());
                    writeD(petCommonData.getRemainingLifeTime());

                    int specialtyCount = 0;
                    if (petTemplate.ContainsFunction(PetFunctionType.WAREHOUSE)) {
                        writeH(PetFunctionType.WAREHOUSE.getId());
                        specialtyCount++;
                    }
                    if (petTemplate.ContainsFunction(PetFunctionType.LOOT)) {
                        writeH(PetFunctionType.LOOT.getId());
                        writeC(0);
                        specialtyCount++;
                    }
                    if (petTemplate.ContainsFunction(PetFunctionType.DOPING)) {
                        writeH(PetFunctionType.DOPING.getId());
                        short dopeId = (short) petTemplate.getPetFunction(PetFunctionType.DOPING).getId();
                        PetDopingEntry dope = DataManager.PET_DOPING_DATA.getDopingTemplate(dopeId);
                        writeD(dope.isUseFood() ? petCommonData.getDopingBag().getFoodItem() : 0);
                        writeD(dope.isUseDrink() ? petCommonData.getDopingBag().getDrinkItem() : 0);
                        int[] scrollBag = petCommonData.getDopingBag().getScrollsUsed();
                        if (scrollBag.length == 0) {
                            writeQ(0);
                            writeQ(0);
                            writeQ(0);
                        } else {
                            writeD(scrollBag[0]);
                            writeD(scrollBag.length > 1 ? scrollBag[1] : 0);
                            writeD(scrollBag.length > 2 ? scrollBag[2] : 0);
                            writeD(scrollBag.length > 3 ? scrollBag[3] : 0);
                            writeD(scrollBag.length > 4 ? scrollBag[4] : 0);
                            writeD(scrollBag.length > 5 ? scrollBag[5] : 0);
                        }

                        specialtyCount++;

                    }

                    if (petTemplate.ContainsFunction(PetFunctionType.FOOD)) {
                        writeH(PetFunctionType.FOOD.getId());
                        writeD(petCommonData.getFeedProgress().getDataForPacket());
                        writeD((int) petCommonData.getTime() / 1000);
                        specialtyCount++;
                    }

                    // Pets have only 2 functions max. If absent filled with NONE
                    if (specialtyCount == 0) {
                        writeH(PetFunctionType.NONE.getId());
                        writeH(PetFunctionType.NONE.getId());
                    }
                    else if (specialtyCount == 1)
                        writeH(PetFunctionType.NONE.getId());

                    writeH(PetFunctionType.APPEARANCE.getId());
                    writeC(0); // not implemented color R ?
                    writeC(0); // not implemented color G ?
                    writeC(0); // not implemented color B ?
                    writeD(petCommonData.getDecoration());

                    // epilog
                    writeD(0); // unk
                    writeD(0); // unk
                }
                break;
            case 1:
                // adopt
                writeS(commonData.getName());
                writeD(commonData.getPetId());
                writeD(commonData.getObjectId());
                writeD(commonData.getMasterObjectId());
                writeD(0);
                writeD(0);
                writeD(commonData.getBirthday());
                writeD(commonData.getRemainingLifeTime());
                petTemplate = DataManager.PET_DATA.getPetTemplate(commonData.getPetId());
                if (petTemplate.ContainsFunction(PetFunctionType.WAREHOUSE)) {
                    writeH(PetFunctionType.WAREHOUSE.getId());
                }
                if (petTemplate.ContainsFunction(PetFunctionType.LOOT)) {
                    writeH(PetFunctionType.LOOT.getId());
                    writeC(0);
                }
                if (petTemplate.ContainsFunction(PetFunctionType.DOPING)) {
                    writeH(PetFunctionType.DOPING.getId());
                    writeQ(0);
                    writeQ(0);
                    writeQ(0);
                    writeQ(0);
                } else if (petTemplate.ContainsFunction(PetFunctionType.NONE)) {
                    writeH(PetFunctionType.NONE.getId());
                }

                if (petTemplate.ContainsFunction(PetFunctionType.FOOD)) {
                    writeH(PetFunctionType.FOOD.getId());
                    writeQ(0);
                }
                writeH(PetFunctionType.NONE.getId());

                writeH(PetFunctionType.APPEARANCE.getId());
                writeC(0); // not implemented color R ?
                writeC(0); // not implemented color G ?
                writeC(0); // not implemented color B ?
                writeD(commonData.getDecoration());

                // epilog
                writeD(0); // unk
                writeD(0); // unk
                break;
            case 2:
                // surrender
                writeD(commonData.getPetId());
                writeD(commonData.getObjectId());
                writeD(0); // unk
                writeD(0); // unk
                break;
            case 3:
                // spawn
                writeS(pet.getName());
                writeD(pet.getPetId());
                writeD(pet.getObjectId());

                if (pet.getPosition().getX() == 0 && pet.getPosition().getY() == 0 && pet.getPosition().getZ() == 0) {
                    writeF(pet.getMaster().getX());
                    writeF(pet.getMaster().getY());
                    writeF(pet.getMaster().getZ());

                    writeF(pet.getMaster().getX());
                    writeF(pet.getMaster().getY());
                    writeF(pet.getMaster().getZ());

                    writeC(pet.getMaster().getHeading());
                } else {
                    writeF(pet.getPosition().getX());
                    writeF(pet.getPosition().getY());
                    writeF(pet.getPosition().getZ());
                    writeF(pet.getMoveController().getTargetX2());
                    writeF(pet.getMoveController().getTargetY2());
                    writeF(pet.getMoveController().getTargetZ2());
                    writeC(pet.getHeading());
                }

                writeD(pet.getMaster().getObjectId()); // unk

                writeC(1); // unk
                writeD(0); // accompanying time ??
                writeD(pet.getCommonData().getDecoration());
                writeD(0); // wings ID if customize_attach = 1
                writeD(0); // unk
                break;
            case 4:
                // dismiss
                writeD(pet.getObjectId());
                writeC(0x01);
                break;
            case 9:
                writeH(1);
                writeC(1);
                writeC(subType);
                switch (subType) {
                    case 1:
                        writeD(commonData.getFeedProgress().getDataForPacket());
                        writeD(0);
                        writeD(itemObjectId);
                        writeD(count);
                        break;
                    case 2:
                        writeD(commonData.getFeedProgress().getDataForPacket());
                        writeD(0);
                        writeD(itemObjectId);
                        writeD(count);
                        writeC(0);
                        break;
                    case 3:
                    case 4:
                    case 5:
                        writeD(commonData.getFeedProgress().getDataForPacket());
                        writeD(0);
                        break;
                    case 6:
                        writeD(commonData.getFeedProgress().getDataForPacket());
                        writeD(0);
                        writeD(itemObjectId);
                        writeC(0);
                        break;
                    case 7:
                        writeD(commonData.getFeedProgress().getDataForPacket());
                        writeD((int) commonData.getTime() / 1000);
                        writeD(itemObjectId);
                        writeD(0);
                        break;
                    case 8:
                        writeD(commonData.getFeedProgress().getDataForPacket());
                        writeD((int) commonData.getTime() / 1000);
                        writeD(itemObjectId);
                        writeD(count);
                        break;
                }
                break;
            case 10:
                // rename
                writeD(pet.getObjectId());
                writeS(pet.getName());
                break;
            case 12:
                switch (subType) {
                    case 0: // check pet status
                        writeC(subType);
                        // desynced feedback data, need to send delta in percents
                        if (commonData.getLastSentPoints() < commonData.getMoodPoints(true)) {
                            writeD(commonData.getMoodPoints(true) - commonData.getLastSentPoints());
                        } else {
                            writeD(0);
                            commonData.setLastSentPoints(commonData.getMoodPoints(true));
                        }
                        break;
                    case 2: // emotion sent
                        writeC(subType);
                        writeD(0);
                        writeD(pet.getCommonData().getMoodPoints(true));
                        writeD(shuggleEmotion);
                        commonData.setLastSentPoints(pet.getCommonData().getMoodPoints(true));
                        commonData.setMoodCdStarted(System.currentTimeMillis());
                        break;
                    case 3: // give gift
                        writeC(subType);
                        writeD(pet.getPetTemplate().getConditionReward());
                        commonData.setGiftCdStarted(System.currentTimeMillis());
                        break;
                    case 4: // periodic update
                        writeC(subType);
                        writeD(commonData.getMoodPoints(true));
                        writeD(commonData.getMoodRemainingTime());
                        writeD(commonData.getGiftRemainingTime());
                        commonData.setLastSentPoints(pet.getCommonData().getMoodPoints(true));
                    case 1:
                }
                break;
            case 13:
                writeC(subType);
                if (subType == 2) {
                    writeC(dopeAction);
                    switch (dopeAction) {
                        case 0:
                            writeD(itemObjectId);
                            writeD(dopeSlot);
                            break;
                        case 1:
                            writeD(0);
                            break;
                        case 2:
                            break;
                        case 3:
                            writeD(itemObjectId);
                    }

                } else if (subType == 3) {
                    if (lootNpcId > 0) {
                        writeC(isActing ? 1 : 2);
                        writeD(lootNpcId);
                    } else {
                        writeC(0);
                        writeC(isActing ? 1 : 0);
                    }
                }
                break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 11:
                break;
        }
    }
}
