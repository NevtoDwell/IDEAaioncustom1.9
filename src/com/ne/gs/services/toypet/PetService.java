/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.toypet;

import java.util.Collection;
import java.util.List;

import com.ne.gs.database.GDB;
import com.ne.gs.database.dao.PlayerPetsDAO;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.Pet;
import com.ne.gs.model.gameobjects.player.PetCommonData;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.items.storage.StorageType;
import com.ne.gs.model.team2.common.legacy.LootRuleType;
import com.ne.gs.model.templates.item.ItemUseLimits;
import com.ne.gs.model.templates.item.actions.AbstractItemAction;
import com.ne.gs.model.templates.item.actions.ItemActions;
import com.ne.gs.model.templates.pet.FoodType;
import com.ne.gs.model.templates.pet.PetFeedResult;
import com.ne.gs.model.templates.pet.PetFlavour;
import com.ne.gs.model.templates.pet.PetFunction;
import com.ne.gs.model.templates.pet.PetFunctionType;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.network.aion.serverpackets.SM_PET;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.restrictions.RestrictionsManager;
import com.ne.gs.services.item.ItemPacketService;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author M@xx, IlBuono, xTz
 */
public class PetService {

    public static PetService getInstance() {
        return SingletonHolder.instance;
    }

    public void renamePet(Player player, String name) {
        Pet pet = player.getPet();
        if (pet != null) {
            pet.getCommonData().setName(name);
            GDB.get(PlayerPetsDAO.class).updatePetName(pet.getCommonData());
            PacketSendUtility.broadcastPacket(player, new SM_PET(10, pet), true);
        }
    }

    public void onPlayerLogin(Player player) {
        Collection<PetCommonData> playerPets = player.getPetList().getPets();
        if (playerPets != null && playerPets.size() > 0) {
            player.sendPck(new SM_PET(0, playerPets));
        }
    }

    public void removeObject(int objectId, int count, int action, Player player) {
        Item item = player.getInventory().getItemByObjId(objectId);
        if (item == null || player.getPet() == null || count > item.getItemCount()) {
            return;
        }

        Pet pet = player.getPet();
        pet.getCommonData().setCancelFeed(false);
        player.sendPck(new SM_PET(1, action, item.getObjectId(), count, pet));
        player.sendPck(new SM_EMOTION(player, EmotionType.START_FEEDING, 0, player.getObjectId()));

        schedule(pet, player, item, count, action);
    }

    private void schedule(final Pet pet, final Player player, final Item item, final int count, final int action) {
        ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                if (!pet.getCommonData().getCancelFeed()) {
                    checkFeeding(pet, player, item, count, action);
                }
            }
        }, 2500);
    }

    private void checkFeeding(Pet pet, Player player, Item item, int count, int action) {

        PetCommonData commonData = pet.getCommonData();
        PetFeedProgress progress = commonData.getFeedProgress();
        if (!commonData.getCancelFeed()) {
            PetFunction func = pet.getPetTemplate().getPetFunction(PetFunctionType.FOOD);
            PetFlavour flavour = DataManager.PET_FEED_DATA.getFlavourById(func.getId());
            FoodType foodType = flavour.getFoodType(item.getItemId());
            PetFeedResult reward = null;

            //			if ((flavour.isLovedFood(foodType, item.getItemId())) && (progress.getLovedFoodRemaining() == 0)) {
            //				foodType = null;
            //			}
            if (foodType != null) {
                player.getInventory().decreaseItemCount(item, 1L);

                int rate = Math.max(1, player.getRates().getPetFeedingRate());

                reward = flavour.processFeedResult(progress, foodType, item.getItemTemplate().getLevel(), player.getCommonData().getLevel(), rate);

                if ((progress.getHungryLevel() == PetHungryLevel.FULL) && (reward != null)) {
                    player.sendPck(new SM_PET(2, action, item.getObjectId(), 0, pet));
                } else {
                    AionServerPacket packet = new SM_PET(2, action, item.getObjectId(), --count, pet);
                    player.sendPck(packet);
                }
            } else {
                player.sendPck(new SM_PET(5, action, 0, 0, pet));
                player.sendPck(new SM_EMOTION(player, EmotionType.END_FEEDING, 0, player.getObjectId()));
                player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_TOYPET_FEED_FOOD_NOT_LOVEFLAVOR(pet.getName(), item.getItemTemplate().getNameId()));
                if (item.getItemCount() > 0L) {
                    ItemPacketService.sendStorageUpdatePacket(player, StorageType.CUBE, item);
                }
                return;
            }

            if ((progress.getHungryLevel() == PetHungryLevel.FULL) && (reward != null)) {
                player.sendPck(new SM_PET(6, action, reward.getItem(), 0, pet));
                player.sendPck(new SM_PET(5, action, 0, 0, pet));
                player.sendPck(new SM_EMOTION(player, EmotionType.END_FEEDING, 0, player.getObjectId()));
                player.sendPck(new SM_PET(7, action, 0, 0, pet));
                ItemService.addItem(player, reward.getItem(), 1L);
                commonData.setReFoodTime(flavour.getCooldDown() * 60000);
                commonData.setCurentTime(System.currentTimeMillis());
                GDB.get(PlayerPetsDAO.class).setTime(player, pet.getPetId(), System.currentTimeMillis());
                progress.reset();

                if (item.getItemCount() > 0L) {
                    ItemPacketService.sendStorageUpdatePacket(player, StorageType.CUBE, item);
                }
            } else if (count > 0) {
                schedule(pet, player, item, count, action);
            } else {
                player.sendPck(new SM_PET(5, action, 0, 0, pet));
                player.sendPck(new SM_EMOTION(player, EmotionType.END_FEEDING, 0, player.getObjectId()));
                if (item.getItemCount() > 0L) {
                    ItemPacketService.sendStorageUpdatePacket(player, StorageType.CUBE, item);
                }
            }
        }
    }

    public void relocateDoping(Player player, int targetSlot, int destinationSlot) {
        Pet pet = player.getPet();
        if ((pet == null) || (pet.getCommonData().getDopingBag() == null)) {
            return;
        }
        int[] scrollBag = pet.getCommonData().getDopingBag().getScrollsUsed();
        int targetItem = scrollBag[(targetSlot - 2)];
        if (destinationSlot - 2 > scrollBag.length - 1) {
            pet.getCommonData().getDopingBag().setItem(targetItem, destinationSlot);
            player.sendPck(new SM_PET(0, targetItem, destinationSlot));
            pet.getCommonData().getDopingBag().setItem(0, targetSlot);
            player.sendPck(new SM_PET(0, 0, targetSlot));
        } else {
            pet.getCommonData().getDopingBag().setItem(scrollBag[(destinationSlot - 2)], targetSlot);
            player.sendPck(new SM_PET(0, scrollBag[(destinationSlot - 2)], targetSlot));
            pet.getCommonData().getDopingBag().setItem(targetItem, destinationSlot);
            player.sendPck(new SM_PET(0, targetItem, destinationSlot));
        }
    }

    public void useDoping(final Player player, int action, int itemId, int slot) {
        Pet pet = player.getPet();
        if ((pet == null) || (pet.getCommonData().getDopingBag() == null)) {
            return;
        }
        if (action < 2) {
            pet.getCommonData().getDopingBag().setItem(itemId, slot);
            action = 0;
        } else {
            Item useItem;
            if (action == 3) {
                List<Item> items = player.getInventory().getItemsByItemId(itemId);

                useItem = items.get(0); // FIXME possible NPE
                ItemActions itemActions = useItem.getItemTemplate().getActions();
                ItemUseLimits limit = new ItemUseLimits();
                int useDelay = player.getItemCooldown(useItem.getItemTemplate()) / 3;
                if (useDelay < 3000) {
                    useDelay = 3000;
                }
                limit.setDelayId(useItem.getItemTemplate().getUseLimits().getDelayId());
                limit.setDelayTime(useDelay);

                if (player.isItemUseDisabled(limit)) {
                    final int useAction = action;
                    final int useItemId = itemId;
                    final int useSlot = slot;

                    ThreadPoolManager.getInstance().schedule(new Runnable() {

                        @Override
                        public void run() {
                            player.sendPck(new SM_PET(useAction, useItemId, useSlot));
                        }
                    }, useDelay);

                    return;
                }
                if (!RestrictionsManager.canUseItem(player, useItem)) {
                    player.addItemCoolDown(limit.getDelayId(), System.currentTimeMillis() + useDelay, useDelay / 1000);
                } else {
                    for (AbstractItemAction itemAction : itemActions.getItemActions()) {
                        if (itemAction.canAct(player, useItem, null)) {
                        	itemAction.setPetDopingAction(true);
                            itemAction.act(player, useItem, null);
                        }
                    }
                }
            }
        }
        player.sendPck(new SM_PET(action, itemId, slot));

        itemId = pet.getCommonData().getDopingBag().getFoodItem();
        long totalDopes = player.getInventory().getItemCountByItemId(itemId);

        itemId = pet.getCommonData().getDopingBag().getDrinkItem();
        totalDopes += player.getInventory().getItemCountByItemId(itemId);

        int[] scrollBag = pet.getCommonData().getDopingBag().getScrollsUsed();
        for (int i = 0; i < scrollBag.length; i++) {
            if (scrollBag[i] != 0) {
                totalDopes += player.getInventory().getItemCountByItemId(scrollBag[i]);
            }
        }
        if (totalDopes == 0L) {
            pet.getCommonData().setIsBuffing(false);
            player.sendPck(new SM_PET(1, false));
        }
    }

    public void activateLoot(Player player, boolean activate) {
        if (player.getPet() == null) {
            return;
        }
        if (activate) {
            if (player.isInTeam()) {
                LootRuleType lootType = player.getLootGroupRules().getLootRule();
                if (lootType == LootRuleType.FREEFORALL) {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_LOOTING_PET_MESSAGE03);
                    return;
                }
            }
            player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_LOOTING_PET_MESSAGE01);
        }
        player.getPet().getCommonData().setIsLooting(activate);
        player.sendPck(new SM_PET(activate));
    }

    @SuppressWarnings("synthetic-access")
    private static final class SingletonHolder {

        protected static final PetService instance = new PetService();
    }
}
