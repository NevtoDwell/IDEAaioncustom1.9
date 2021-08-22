/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.conds.CanSummonPet;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.items.storage.StorageType;
import com.ne.gs.services.item.ItemPacketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.EmotionType;
import com.ne.gs.model.gameobjects.Pet;
import com.ne.gs.model.gameobjects.PetAction;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.network.aion.serverpackets.SM_PET;
import com.ne.gs.services.NameRestrictionService;
import com.ne.gs.services.toypet.PetAdoptionService;
import com.ne.gs.services.toypet.PetMoodService;
import com.ne.gs.services.toypet.PetService;
import com.ne.gs.services.toypet.PetSpawnService;

/**
 * @author M@xx, xTz
 */
public class CM_PET extends AionClientPacket {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(CM_PET.class);

    private int actionId;
    private PetAction action;
    private int petId;
    private String petName;
    private int decorationId;
    private int eggObjId;
    private int objectId;
    private int count;
    private int subType;
    private int emotionId;
    private int actionType;
    private int dopingItemId;
    private int dopingAction;
    private int dopingSlot1;
    private int dopingSlot2;
    private int activateLoot;
    private int unk2;
    private int unk3;
    @SuppressWarnings("unused")
    private int unk5;
    @SuppressWarnings("unused")
    private int unk6;

    @Override
    protected void readImpl() {
        actionId = readH();
        action = PetAction.getActionById(actionId);
        switch (action) {
            case ADOPT:
                eggObjId = readD();
                petId = readD();
                unk2 = readC();
                unk3 = readD();
                decorationId = readD();
                unk5 = readD();
                unk6 = readD();
                petName = readS();
                break;
            case SURRENDER:
            case SPAWN:
            case DISMISS:
                petId = readD();
                break;
            case FOOD:
                actionType = readD();
                if (actionType == 3) {
                    activateLoot = readD();
                } else if (actionType == 2) {
                    dopingAction = readD();
                    if (dopingAction == 0) {
                        dopingItemId = readD();
                        dopingSlot1 = readD();
                    } else if (dopingAction == 1) {
                        dopingSlot1 = readD();
                        dopingItemId = readD();
                    } else if (dopingAction == 2) {
                        dopingSlot1 = readD();
                        dopingSlot2 = readD();
                    } else if (dopingAction == 3) {
                        dopingItemId = readD();
                        dopingSlot1 = readD();
                    }
                } else {
                    objectId = readD();
                    count = readD();
                }
                break;
            case RENAME:
                petId = readD();
                petName = readS();
                break;
            case MOOD:
                subType = readD();
                emotionId = readD();
                break;
            default:
                break;
        }
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        if (player == null) {
            return;
        }
        Pet pet = player.getPet();
        switch (action) {
            case ADOPT:
                if (NameRestrictionService.isForbiddenWord(petName)) {
                    player.sendMsg("You are trying to use a forbidden name. Choose another one!");
                } else {
                    PetAdoptionService.adoptPet(player, eggObjId, petId, petName, decorationId);
                }
                break;
            case SURRENDER:
                PetAdoptionService.surrenderPet(player, petId);
                break;
            case SPAWN:
                if (player.getConditioner().check(CanSummonPet.class, player)) {
                    PetSpawnService.summonPet(player, petId, true);
                }
                break;
            case DISMISS:
                PetSpawnService.dismissPet(player, true);
                break;
            case FOOD:
                if (actionType == 2) {
                    // Pet doping
                    if (dopingAction == 2) {
                        PetService.getInstance().relocateDoping(player, dopingSlot1, dopingSlot2);
                    } else {
                        PetService.getInstance().useDoping(player, dopingAction, dopingItemId, dopingSlot1);
                    }
                } else if (actionType == 3) {
                    // Pet looting
                    PetService.getInstance().activateLoot(player, activateLoot != 0);
                } else if (pet != null && !pet.getCommonData().isFeedingTime()) {
                    player.sendPck(new SM_PET(8, actionId, objectId, count, player.getPet()));

                    Item item = player.getInventory().getItemByObjId(objectId);
                    if(objectId != 0 && item != null){
                        ItemPacketService.sendStorageUpdatePacket(player, StorageType.CUBE, item);
                    }
                } else if (pet != null && objectId == 0 && pet.getCommonData().isFeedingTime()) {
                    pet.getCommonData().setCancelFeed(true);
                    player.sendPck(new SM_PET(4, actionId, 0, 0, player.getPet()));
                    player.sendPck(new SM_EMOTION(player, EmotionType.END_FEEDING, 0, player.getObjectId()));
                } else {
                    PetService.getInstance().removeObject(objectId, count, actionId, player);
                }
                break;
            case RENAME:
                if (NameRestrictionService.isForbiddenWord(petName)) {
                    player.sendMsg("You are trying to use a forbidden name. Choose another one!");
                } else {
                    PetService.getInstance().renamePet(player, petName);
                }
                break;
            case MOOD:
                if (pet != null
                    && ((subType == 0 && pet.getCommonData().getMoodRemainingTime() == 0)
                    || (subType == 3 && pet.getCommonData().getGiftRemainingTime() == 0) || emotionId != 0)) {
                    PetMoodService.checkMood(pet, subType, emotionId);
                }
            default:
                break;
        }
    }

}
