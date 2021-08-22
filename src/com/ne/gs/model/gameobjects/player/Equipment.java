/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.ne.gs.model.templates.item.*;
import com.ne.gs.modules.ffaloc.FFALoc;
import javolution.util.FastList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.database.GDB;
import com.ne.gs.configs.administration.AdminConfig;
import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.ObserverType;
import com.ne.gs.database.dao.InventoryDAO;
import com.ne.gs.model.DescId;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.Race;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.actions.CreatureActions;
import com.ne.gs.model.actions.PlayerMode;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.PersistentState;
import com.ne.gs.model.gameobjects.Summon;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.items.ItemSlot;
import com.ne.gs.model.stats.listeners.ItemEquipmentListener;
import com.ne.gs.model.templates.itemset.ItemPart;
import com.ne.gs.model.templates.itemset.ItemSetTemplate;
import com.ne.gs.network.aion.serverpackets.SM_DELETE_ITEM;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;
import com.ne.gs.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.ne.gs.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.network.aion.serverpackets.SM_UPDATE_PLAYER_APPEARANCE;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.services.StigmaService;
import com.ne.gs.services.item.ItemPacketService;
import com.ne.gs.services.item.ItemPacketService.ItemUpdateType;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.utils.stats.AbyssRankEnum;

/**
 * @author Avol, ATracer, kosyachok
 * @modified cura
 */
public class Equipment {

    private static final Logger log = LoggerFactory.getLogger(Equipment.class);

    private final SortedMap<Integer, Item> equipment = new TreeMap<>();
    private Player owner;

    private final Set<Integer> markedFreeSlots = new HashSet<>();
    private PersistentState persistentState = PersistentState.UPDATED;

    private static final int[] ARMOR_SLOTS = new int[]{ItemSlot.BOOTS.id(), ItemSlot.GLOVES.id(), ItemSlot.PANTS.id(),
                                                       ItemSlot.SHOULDER.id(), ItemSlot.TORSO.id()};

    public Equipment(Player player) {
        owner = player;
    }

    /**
     * @param itemUniqueId
     * @param slot
     *
     * @return item or null in case of failure
     */
    public Item equipItem(int itemUniqueId, int slot) {
        if (getOwner() == null) {
            return null;
        }

        Item item = owner.getInventory().getItemByObjId(itemUniqueId);

        if (item == null) {
            return null;
        }

        ItemTemplate itemTemplate = item.getItemTemplate();

        // don't allow to wear items of higher level
        if (itemTemplate.getLevel() > owner.getCommonData().getLevel()) {
            owner.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_TOO_LOW_LEVEL_MUST_BE_THIS_LEVEL(item.getNameID(), itemTemplate.getLevel()));
            return null;
        }

        if (owner.getAccessLevel() < AdminConfig.GM_LEVEL) {
            if (itemTemplate.getRace() != Race.PC_ALL && itemTemplate.getRace() != owner.getRace()) {
                owner.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_RACE);
                return null;
            }

            ItemUseLimits limits = itemTemplate.getUseLimits();
            if (limits.getGenderPermitted() != null && limits.getGenderPermitted() != owner.getGender()) {
                owner.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_GENDER);
                return null;
            }

            if (!verifyRankLimits(item)) {
                owner.sendPck(SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_RANK(AbyssRankEnum.getRankById(limits.getMinRank()).getDescriptionId()));
                return null;
            }

            int requiredLevel = item.getItemTemplate().getRequiredLevel(owner.getCommonData().getPlayerClass());
            if (requiredLevel == -1 || requiredLevel > owner.getLevel()) {
                return null;
            }
        }

        int itemSlotToEquip = 0;

        synchronized (equipment) {
            markedFreeSlots.clear();

            // validate item against current equipment and mark free slots
            switch (item.getEquipmentType()) {
                case ARMOR:
                    if (!validateEquippedArmor(item, true)) {
                        return null;
                    }
                    break;
                case WEAPON:
                    if (!validateEquippedWeapon(item, true)) {
                        return null;
                    }
                    break;
            }

            // check whether there is already item in specified slot
            int itemSlotMask = 0;
            switch (item.getEquipmentType()) {
                case STIGMA:
                    itemSlotMask = slot;
                    break;
                default:
                    itemSlotMask = itemTemplate.getItemSlot();
                    break;
            }

            // find correct slot
            ItemSlot[] possibleSlots = ItemSlot.getSlotsFor(itemSlotMask);
            for (int i = 0; i < possibleSlots.length; i++) {
                ItemSlot possibleSlot = possibleSlots[i];
                int slotId = possibleSlot.id();
                if (equipment.get(slotId) == null || markedFreeSlots.contains(slotId)) {
                    itemSlotToEquip = slotId;
                    break;
                }
            }

            if (itemSlotToEquip == 0) {
                itemSlotToEquip = possibleSlots[0].id();
            }
        }

        if (itemSlotToEquip == 0) {
            return null;
        }

        if (itemTemplate.isSoulBound() && !item.isSoulBound()) {
            soulBindItem(owner, item, itemSlotToEquip);
            return null;
        }
        return equip(itemSlotToEquip, item, slot);
    }

    public List<Item> getEquippedForAppearance() {
        return owner.getImplementator().result(FFALoc.VisualEquipment.class, owner);
    }

    /**
     *
     * @param itemSlotToEquip
     * @param item
     */
    private Item equip(int itemSlotToEquip, Item item){
        return equip(itemSlotToEquip,item, 0);
    }

    /**
     * @param itemSlotToEquip
     * @param item
     */
    private Item equip(int itemSlotToEquip, Item item, int slotMask) {
        synchronized (equipment) {
            // remove item first from inventory to have at least one slot free
            owner.getInventory().remove(item);

            // do unequip of necessary items
            Item equippedItem = equipment.get(itemSlotToEquip);
            if (equippedItem != null) {

                if(equippedItem.getEquipmentType() == EquipType.STIGMA)
                {
                    log.error("Stigma duplication detected. Slot: {}", itemSlotToEquip);
                    return null;
                }

                unEquip(itemSlotToEquip);
            }

            switch (item.getEquipmentType()) {
                case ARMOR:
                    validateEquippedArmor(item, false);
                    break;
                case WEAPON:
                    validateEquippedWeapon(item, false);
                    break;
                case STIGMA:
                    if(!StigmaService.notifyEquipAction(owner, item, slotMask))
                        return null;
                    break;
            }

            if (equipment.get(itemSlotToEquip) != null) {
                log.error("CHECKPOINT : putting item to already equiped slot. Info slot: " + itemSlotToEquip + " new item: "
                    + item.getItemTemplate().getTemplateId() + " old item: " + equipment.get(itemSlotToEquip).getItemTemplate().getTemplateId());
                return null;
            }

            // equip target item
            equipment.put(itemSlotToEquip, item);
            item.setEquipped(true);
            item.setEquipmentSlot(itemSlotToEquip);
            ItemPacketService.updateItemAfterEquip(owner, item);

            // update stats
            notifyItemEquipped(item);
            owner.getLifeStats().updateCurrentStats();
            setPersistentState(PersistentState.UPDATE_REQUIRED);
            QuestEngine.getInstance().onEquipItem(new QuestEnv(null, owner, 0, 0), item.getItemId());
            return item;
        }
    }

    private void notifyItemEquipped(Item item) {
        ItemEquipmentListener.onItemEquipment(item, owner);
        owner.getObserveController().notifyItemEquip(item, owner);
        tryUpdateSummonStats();
    }

    private void notifyItemUnequip(Item item) {
        ItemEquipmentListener.onItemUnequipment(item, owner);
        owner.getObserveController().notifyItemUnEquip(item, owner);
        tryUpdateSummonStats();
    }

    private void tryUpdateSummonStats() {
        Summon summon = owner.getSummon();
        if (summon != null) {
            summon.getGameStats().updateStatsAndSpeedVisually();
        }
    }

    public Item unEquipItem(int itemUniqueId, int slot){
        return unEquipItem(itemUniqueId, slot, false);
    }
    /**
     * Called when CM_EQUIP_ITEM packet arrives with action 1
     *
     * @param itemUniqueId
     * @param slot
     *
     * @return item or null in case of failure
     */
    public Item unEquipItem(int itemUniqueId, int slot, boolean force) {
        if (getOwner() == null) {
            return null;
        }

        // if inventory is full unequip action is disabled
        if (owner.getInventory().isFull() && !force) {
            return null;
        }

        synchronized (equipment) {
            Item itemToUnequip = null;

            for (Item item : equipment.values()) {
                if (item.getObjectId() == itemUniqueId) {
                    itemToUnequip = item;
                }
            }

            if (itemToUnequip == null || !itemToUnequip.isEquipped()) {
                return null;
            }

            // if unequip bow - unequip arrows also
            if (itemToUnequip.getItemTemplate().getWeaponType() == WeaponType.BOW) {
                Item possibleArrows = equipment.get(ItemSlot.SUB_HAND.id());
                if (possibleArrows != null && possibleArrows.getItemTemplate().getArmorType() == ArmorType.ARROW) {
                    // TODO more wise check here is needed
                    if (owner.getInventory().getFreeSlots() < 1) {
                        return null;
                    }
                    unEquip(ItemSlot.SUB_HAND.id());
                }
            }

            // Looks very odd - but its retail like
            if (itemToUnequip.getEquipmentSlot() == ItemSlot.MAIN_HAND.id()) {
                Item ohWeapon = equipment.get(ItemSlot.SUB_HAND.id());
                if (ohWeapon != null && ohWeapon.getItemTemplate().isWeapon()) {
                    if (owner.getInventory().getFreeSlots() < 2) {
                        return null;
                    }
                    unEquip(ItemSlot.SUB_HAND.id());
                }
            }

            // if unequip power shard
            if (itemToUnequip.getItemTemplate().isArmor() && itemToUnequip.getItemTemplate().getArmorType() == ArmorType.SHARD) {
                owner.unsetState(CreatureState.POWERSHARD);
                owner.sendPck(new SM_EMOTION(owner, EmotionType.POWERSHARD_OFF, 0, 0));
            }

            if (!StigmaService.notifyUnequipAction(owner, itemToUnequip)) {
                return null;
            }

            unEquip(itemToUnequip.getEquipmentSlot());
            return itemToUnequip;
        }
    }

    private void unEquip(int slot) {
        Item item = equipment.remove(slot);
        if (item == null) {
            return;
        }

        item.setEquipped(false);
        item.setEquipmentSlot(0);
        notifyItemUnequip(item);
        owner.getLifeStats().updateCurrentStats();
        owner.getGameStats().updateStatsAndSpeedVisually();
        owner.getInventory().put(item);
    }

    /**
     * Used during equip process and analyzes equipped slots
     *
     * @param item
     * @return
     */
    private boolean validateEquippedWeapon(Item item, boolean validateOnly) {
        // check present skill
        int[] requiredSkills = item.getItemTemplate().getWeaponType().getRequiredSkills();

        if (!checkAvaialbeEquipSkills(requiredSkills)) {
            return false;
        }

        Item itemInMainHand = equipment.get(ItemSlot.MAIN_HAND.id());
        Item itemInSubHand = equipment.get(ItemSlot.SUB_HAND.id());

        int requiredSlots = 0;
        switch (item.getItemTemplate().getWeaponType().getRequiredSlots()) {
            case 2:
                switch (item.getItemTemplate().getWeaponType()) {
                    // if bow and arrows are equipped + new item is bow - dont uneqiup arrows
                    case BOW:
                        if (itemInSubHand != null && itemInSubHand.getItemTemplate().getArmorType() != ArmorType.ARROW) {
                            if (validateOnly) {
                                requiredSlots++;
                                markedFreeSlots.add(ItemSlot.SUB_HAND.id());
                            } else {
                                unEquip(ItemSlot.SUB_HAND.id());
                            }
                        }
                        break;
                    // if new item is not bow - unequip arrows
                    default:
                        if (itemInSubHand != null) {
                            if (validateOnly) {
                                requiredSlots++;
                                markedFreeSlots.add(ItemSlot.SUB_HAND.id());
                            } else {
                                unEquip(ItemSlot.SUB_HAND.id());
                            }
                        }
                }// no break
            case 1:
                // check dual skill
                if (itemInMainHand != null
                    && (!owner.getSkillList().isSkillPresent(19) && !owner.getSkillList().isSkillPresent(360) && !owner.getSkillList().isSkillPresent(127)
                    && !owner.getSkillList().isSkillPresent(128) && !owner.getSkillList().isSkillPresent(924))) {
                    if (validateOnly) {
                        requiredSlots++;
                        markedFreeSlots.add(ItemSlot.MAIN_HAND.id());
                    } else {
                        unEquip(ItemSlot.MAIN_HAND.id());
                    }
                }
                // check 2h weapon in main hand
                else if (itemInMainHand != null && itemInMainHand.getItemTemplate().getWeaponType().getRequiredSlots() == 2) {
                    if (validateOnly) {
                        requiredSlots++;
                        markedFreeSlots.add(ItemSlot.MAIN_HAND.id());
                    } else {
                        unEquip(ItemSlot.MAIN_HAND.id());
                    }
                }

                // unequip arrows if bow+arrows were equipeed
                Item possibleArrows = equipment.get(ItemSlot.SUB_HAND.id());
                if (possibleArrows != null && possibleArrows.getItemTemplate().getArmorType() == ArmorType.ARROW) {
                    if (validateOnly) {
                        requiredSlots++;
                        markedFreeSlots.add(ItemSlot.SUB_HAND.id());
                    } else {
                        unEquip(ItemSlot.SUB_HAND.id());
                    }
                }
                break;
        }

        // check agains = required slots - 1(equipping item)
        return owner.getInventory().getFreeSlots() >= requiredSlots - 1;
    }

    /**
     * @param requiredSkills
     *
     * @return
     */
    private boolean checkAvaialbeEquipSkills(int[] requiredSkills) {
        boolean isSkillPresent = false;

        // if no skills required - validate as true
        if (requiredSkills.length == 0) {
            return true;
        }

        for (int skill : requiredSkills) {
            if (owner.getSkillList().isSkillPresent(skill)) {
                isSkillPresent = true;
                break;
            }
        }
        return isSkillPresent;
    }

    /**
     * Used during equip process and analyzes equipped slots
     *
     * @param item
     * @return
     */
    private boolean validateEquippedArmor(Item item, boolean validateOnly) {
        // allow wearing of jewelry etc stuff
        ArmorType armorType = item.getItemTemplate().getArmorType();
        if (armorType == null) {
            return true;
        }

        // check present skill
        int[] requiredSkills = armorType.getRequiredSkills();
        if (!checkAvaialbeEquipSkills(requiredSkills)) {
            return false;
        }

        Item itemInMainHand = equipment.get(ItemSlot.MAIN_HAND.id());
        switch (item.getItemTemplate().getArmorType()) {
            case ARROW:
                if (itemInMainHand == null || itemInMainHand.getItemTemplate().getWeaponType() != WeaponType.BOW) {
                    if (validateOnly) {
                        return false;
                    }
                }
                break;
            case SHIELD:
                if (itemInMainHand != null && itemInMainHand.getItemTemplate().getWeaponType().getRequiredSlots() == 2) {
                    if (validateOnly) {
                        if (owner.getInventory().isFull()) {
                            return false;
                        }
                        markedFreeSlots.add(ItemSlot.MAIN_HAND.id());
                    } else {
                        // remove 2H weapon
                        unEquip(ItemSlot.MAIN_HAND.id());
                    }
                }
                break;
        }
        return true;
    }

    /**
     * Will look item in equipment item set
     *
     * @param value
     *
     * @return Item
     */
    public Item getEquippedItemByObjId(int value) {
        synchronized (equipment) {
            for (Item item : equipment.values()) {
                if (item.getObjectId() == value) {
                    return item;
                }
            }
        }

        return null;
    }

    /**
     * @param value
     *
     * @return List<Item>
     */
    public List<Item> getEquippedItemsByItemId(int value) {
        List<Item> equippedItemsById = new ArrayList<>(2);
        synchronized (equipment) {
            for (Item item : equipment.values()) {
                if (item.getItemTemplate().getTemplateId() == value) {
                    equippedItemsById.add(item);
                }
            }
        }

        return equippedItemsById;
    }

    /**
     * @return List<Item>
     */
    public List<Item> getEquippedItems() {
        List<Item> equippedItems = new ArrayList<>();
        equippedItems.addAll(equipment.values());

        return equippedItems;
    }

    public List<Integer> getEquippedItemIds() {
        List<Integer> equippedIds = new ArrayList<>();
        for (Item i : equipment.values()) {
            equippedIds.add(i.getItemId());
        }
        return equippedIds;
    }

    /**
     * @return List<Item>
     */
    public FastList<Item> getEquippedItemsWithoutStigma() {
        FastList<Item> equippedItems = FastList.newInstance();
        for (Item item : equipment.values()) {
            if (!ItemSlot.isStigma(item.getEquipmentSlot())) {
                equippedItems.add(item);
            }
        }
        return equippedItems;
    }

    /**
     * @return List<Item>
     */
    public List<Item> getEquippedItemsAllStigma() {
        List<Item> equippedItems = new ArrayList<>();
        for (Item item : equipment.values()) {
            if (ItemSlot.isStigma(item.getEquipmentSlot())) {
                equippedItems.add(item);
            }
        }
        return equippedItems;
    }

    /**
     * @return List<Item>
     */
    public List<Item> getEquippedItemsRegularStigma() {
        List<Item> equippedItems = new ArrayList<>();
        for (Item item : equipment.values()) {
            if (ItemSlot.isRegularStigma(item.getEquipmentSlot())) {
                equippedItems.add(item);
            }
        }
        return equippedItems;
    }

    /**
     * @return List<Item>
     */
    public List<Item> getEquippedItemsAdvencedStigma() {
        List<Item> equippedItems = new ArrayList<>();
        for (Item item : equipment.values()) {
            if (ItemSlot.isAdvancedStigma(item.getEquipmentSlot())) {
                equippedItems.add(item);
            }
        }
        return equippedItems;
    }

    /**
     * @return Number of parts equipped belonging to requested itemset
     */
    public int itemSetPartsEquipped(int itemSetTemplateId) {
        int number = 0;

        for (Item item : equipment.values()) {
            if (item.getEquipmentSlot() == ItemSlot.MAIN_OFF_HAND.id() || item.getEquipmentSlot() == ItemSlot.SUB_OFF_HAND.id()) {
                continue;
            }
            ItemSetTemplate setTemplate = item.getItemTemplate().getItemSet();
            if (setTemplate != null && setTemplate.getId() == itemSetTemplateId) {
                ++number;
            }
        }

        return number;
    }

    /**
     * Should be called only when loading from GDB for items isEquipped=1
     *
     * @param item
     */
    public void onLoadHandler(Item item) {
        if (equipment.containsKey(item.getEquipmentSlot())) {
            log.warn("Duplicate equipped item in slot : " + item.getEquipmentSlot() + " " + owner.getObjectId());
            return;
        }
        equipment.put(item.getEquipmentSlot(), item);
    }

    /**
     * Should be called only when equipment object totaly constructed on player loading Applies every equipped item stats modificators
     */
    public void onLoadApplyEquipmentStats() {
        for (Item item : equipment.values()) {
            if (item.getEquipmentSlot() != ItemSlot.MAIN_OFF_HAND.id() && item.getEquipmentSlot() != ItemSlot.SUB_OFF_HAND.id()) {
                ItemEquipmentListener.onItemEquipment(item, owner);
                owner.getLifeStats().synchronizeWithMaxStats();
            }
        }
    }

    /**
     * @return true or false
     */
    public boolean isShieldEquipped() {
        Item subHandItem = equipment.get(ItemSlot.SUB_HAND.id());
        return subHandItem != null && subHandItem.getItemTemplate().getArmorType() == ArmorType.SHIELD;
    }

    public Item getEquippedShield() {
        Item subHandItem = equipment.get(ItemSlot.SUB_HAND.id());
        return (subHandItem != null && subHandItem.getItemTemplate().getArmorType() == ArmorType.SHIELD) ? subHandItem : null;
    }

    /**
     * @return true if player is equipping the requested ArmorType
     */
    public boolean isArmorTypeEquipped(ArmorType type) {
        for (Item item : equipment.values()) {
            if ((item != null) && (item.getItemTemplate().getArmorType() == type) && (item.isEquipped())
                && (item.getEquipmentSlot() != ItemSlot.SUB_OFF_HAND.id())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return <tt>WeaponType</tt> of current weapon in main hand or null
     */
    public WeaponType getMainHandWeaponType() {
        Item mainHandItem = equipment.get(ItemSlot.MAIN_HAND.id());
        if (mainHandItem == null) {
            return null;
        }

        return mainHandItem.getItemTemplate().getWeaponType();
    }

    /**
     * check two-handed weapon equipped
     *
     * @return
     */
    public boolean hasTwoHandWeapon() {
        WeaponType mainHandWeaponType = getMainHandWeaponType();
        if (mainHandWeaponType == null) {
            return false;
        }

        switch (mainHandWeaponType) {
            case BOOK_2H:
            case POLEARM_2H:
            case STAFF_2H:
            case SWORD_2H:
                return true;
        }
        return false;
    }

    /**
     * @return <tt>WeaponType</tt> of current weapon in off hand or null
     */
    public WeaponType getOffHandWeaponType() {
        Item offHandItem = equipment.get(ItemSlot.SUB_HAND.id());
        if (offHandItem != null && offHandItem.getItemTemplate().isWeapon()) {
            return offHandItem.getItemTemplate().getWeaponType();
        }

        return null;
    }

    public boolean isArrowEquipped() {
        Item arrow = equipment.get(ItemSlot.SUB_HAND.id());
        if (arrow != null && arrow.getItemTemplate().getArmorType() == ArmorType.ARROW) {
            return true;
        }

        return false;
    }

    public boolean isPowerShardEquipped() {
        Item leftPowershard = equipment.get(ItemSlot.POWER_SHARD_LEFT.id());
        if (leftPowershard != null) {
            return true;
        }

        Item rightPowershard = equipment.get(ItemSlot.POWER_SHARD_RIGHT.id());
        if (rightPowershard != null) {
            return true;
        }

        return false;
    }

    public Item getMainHandPowerShard() {
        Item mainHandPowerShard = equipment.get(ItemSlot.POWER_SHARD_RIGHT.id());
        if (mainHandPowerShard != null) {
            return mainHandPowerShard;
        }

        return null;
    }

    public Item getOffHandPowerShard() {
        Item offHandPowerShard = equipment.get(ItemSlot.POWER_SHARD_LEFT.id());
        if (offHandPowerShard != null) {
            return offHandPowerShard;
        }

        return null;
    }

    /**
     * @param powerShardItem
     * @param count
     */
    public void usePowerShard(Item powerShardItem, int count) {
        decreaseEquippedItemCount(powerShardItem.getObjectId(), count);

        if (powerShardItem.getItemCount() <= 0) {// Search for next same power shards stack
            List<Item> powerShardStacks = owner.getInventory().getItemsByItemId(powerShardItem.getItemTemplate().getTemplateId());
            if (powerShardStacks.size() != 0) {
                equipItem(powerShardStacks.get(0).getObjectId(), powerShardItem.getEquipmentSlot());
            } else {
                owner.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_WEAPON_BOOST_MODE_BURN_OUT);
                owner.unsetState(CreatureState.POWERSHARD);
            }
        }
    }

    public void useArrow() {
        Item arrow = equipment.get(ItemSlot.SUB_HAND.id());

        if (arrow == null || !arrow.getItemTemplate().isArmor() && arrow.getItemTemplate().getArmorType() != ArmorType.ARROW) {
            return;
        }

        decreaseEquippedItemCount(arrow.getObjectId(), 1);
    }

    /**
     * increase item count and return left count
     */
    public long increaseEquippedItemCount(Item item, long count) {
        // Only Arrows and Shards can be increased
        if (item.getItemTemplate().getArmorType() != ArmorType.SHARD && item.getItemTemplate().getArmorType() != ArmorType.ARROW) {
            return count;
        }

        long leftCount = item.increaseItemCount(count);
        ItemPacketService.updateItemAfterInfoChange(owner, item);// TODO validate mask here
        setPersistentState(PersistentState.UPDATE_REQUIRED);
        return leftCount;
    }

    private void decreaseEquippedItemCount(int itemObjId, int count) {
        Item equippedItem = getEquippedItemByObjId(itemObjId);

        // Only Arrows and Shards can be decreased
        if (equippedItem.getItemTemplate().getArmorType() != ArmorType.SHARD && equippedItem.getItemTemplate().getArmorType() != ArmorType.ARROW) {
            return;
        }

        if (equippedItem.getItemCount() >= count) {
            equippedItem.decreaseItemCount(count);
        } else {
            equippedItem.decreaseItemCount(equippedItem.getItemCount());
        }

        if (equippedItem.getItemCount() == 0) {
            equipment.remove(equippedItem.getEquipmentSlot());
            owner.sendPck(new SM_DELETE_ITEM(equippedItem.getObjectId()));
            GDB.get(InventoryDAO.class).store(equippedItem, owner);
        }

        ItemPacketService.updateItemAfterInfoChange(owner, equippedItem);// TODO validate mask here
        setPersistentState(PersistentState.UPDATE_REQUIRED);
    }

    /**
     * Switch OFF and MAIN hands
     */
    public void switchHands() {
        Item mainHandItem = equipment.get(ItemSlot.MAIN_HAND.id());
        Item subHandItem = equipment.get(ItemSlot.SUB_HAND.id());
        Item mainOffHandItem = equipment.get(ItemSlot.MAIN_OFF_HAND.id());
        Item subOffHandItem = equipment.get(ItemSlot.SUB_OFF_HAND.id());

        List<Item> equippedWeapon = new ArrayList<>();

        if (mainHandItem != null) {
            equippedWeapon.add(mainHandItem);
        }
        if (subHandItem != null) {
            equippedWeapon.add(subHandItem);
        }
        if (mainOffHandItem != null) {
            equippedWeapon.add(mainOffHandItem);
        }
        if (subOffHandItem != null) {
            equippedWeapon.add(subOffHandItem);
        }

        for (Item item : equippedWeapon) {
            equipment.remove(item.getEquipmentSlot());
            item.setEquipped(false);
            owner.sendPck(new SM_INVENTORY_UPDATE_ITEM(owner, item, ItemUpdateType.EQUIP_UNEQUIP));
            if (owner.getGameStats() != null) {
                if (item.getEquipmentSlot() == ItemSlot.MAIN_HAND.id() || item.getEquipmentSlot() == ItemSlot.SUB_HAND.id()) {
                    notifyItemUnequip(item);
                }
            }

        }

        for (Item item : equippedWeapon) {
            if (item.getEquipmentSlot() == ItemSlot.MAIN_HAND.id()) {
                item.setEquipmentSlot(ItemSlot.MAIN_OFF_HAND.id());
            } else if (item.getEquipmentSlot() == ItemSlot.SUB_HAND.id()) {
                item.setEquipmentSlot(ItemSlot.SUB_OFF_HAND.id());
            } else if (item.getEquipmentSlot() == ItemSlot.MAIN_OFF_HAND.id()) {
                item.setEquipmentSlot(ItemSlot.MAIN_HAND.id());
            } else if (item.getEquipmentSlot() == ItemSlot.SUB_OFF_HAND.id()) {
                item.setEquipmentSlot(ItemSlot.SUB_HAND.id());
            }
        }

        for (Item item : equippedWeapon) {
            equipment.put(item.getEquipmentSlot(), item);
            item.setEquipped(true);
            ItemPacketService.updateItemAfterEquip(owner, item);
        }

        if (owner.getGameStats() != null) {
            for (Item item : equippedWeapon) {
                if (item.getEquipmentSlot() == ItemSlot.MAIN_HAND.id() || item.getEquipmentSlot() == ItemSlot.SUB_HAND.id()) {
                    notifyItemEquipped(item);
                }
            }
        }

        owner.getLifeStats().updateCurrentStats();
        owner.getGameStats().updateStatsAndSpeedVisually();
        setPersistentState(PersistentState.UPDATE_REQUIRED);
    }

    /**
     * @param weaponType
     */
    public boolean isWeaponEquipped(WeaponType weaponType) {
        if (equipment.get(ItemSlot.MAIN_HAND.id()) != null
            && equipment.get(ItemSlot.MAIN_HAND.id()).getItemTemplate().getWeaponType() == weaponType) {
            return true;
        }
        if (equipment.get(ItemSlot.SUB_HAND.id()) != null
            && equipment.get(ItemSlot.SUB_HAND.id()).getItemTemplate().getWeaponType() == weaponType) {
            return true;
        }
        return false;
    }

    /**
     * @return true if player wear dual weapon
     */
    public boolean isDualWeaponEquipped() {
        Item mainWeapon = equipment.get(ItemSlot.MAIN_HAND.id());
        Item subWeapon = equipment.get(ItemSlot.SUB_HAND.id());
        if ((mainWeapon == null) || (subWeapon == null)) {
            return false;
        }

        if ((mainWeapon.getItemTemplate() == null) || (subWeapon.getItemTemplate() == null)) {
            return false;
        }

        if ((mainWeapon.getItemTemplate().getWeaponType() == null) || (subWeapon.getItemTemplate().getWeaponType() == null)) {
            return false;
        }

        if ((mainWeapon.getItemTemplate().getWeaponType().getRequiredSlots() == 1) && (subWeapon.getItemTemplate().getWeaponType().getRequiredSlots() == 1)) {
            return true;
        }

        return false;
    }

    /**
     * @param armorType
     */
    public boolean isArmorEquipped(ArmorType armorType) {
        for (int slot : ARMOR_SLOTS) {
            if (equipment.get(slot) != null && equipment.get(slot).getItemTemplate().getArmorType() != armorType) {
                return false;
            }
        }
        return true;
    }

    public boolean isSlotEquipped(int slot) {
        return !(equipment.get(slot) == null);
    }

    public Item getMainHandWeapon() {
        return equipment.get(ItemSlot.MAIN_HAND.id());
    }

    public Item getOffHandWeapon() {
        return equipment.get(ItemSlot.SUB_HAND.id());
    }
    
    public Item getItemBySlot(ItemSlot slot) {
        return equipment.get(slot.id());
    }
    
    public List<Item> getItemSetBySlots(List<ItemPart> setitems, ItemSlot... slots) {
    	List<Item> list = new ArrayList<>(0);
    	for(ItemSlot slot: slots) {
    		Item item = this.equipment.get(slot.id());
    		if(item != null && setitems.contains(item.getItemId())) {
    			list.add(item);
    		}
    	}
    	
        return list;
    }

    /**
     * @return the persistentState
     */
    public PersistentState getPersistentState() {
        return persistentState;
    }

    /**
     * @param persistentState
     *     the persistentState to set
     */
    public void setPersistentState(PersistentState persistentState) {
        this.persistentState = persistentState;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player player) {
        owner = player;
    }

    /**
     * @param player
     * @param item
     *
     * @return
     */
    private boolean soulBindItem(final Player player, final Item item, final int slot) {
        if (player.getInventory().getItemByObjId(item.getObjectId()) == null || player.isInState(CreatureState.GLIDING)) {
            return false;
        }
        if (CreatureActions.isAlreadyDead(player)) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_SOUL_BOUND_INVALID_STANCE(2800119));
            return false;
        }
        if (player.isInPlayerMode(PlayerMode.RIDE)) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_SOUL_BOUND_INVALID_STANCE(2800114));
            return false;
        }
        if (player.isInState(CreatureState.CHAIR)) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_SOUL_BOUND_INVALID_STANCE(2800117));
            return false;
        }
        if (player.isInState(CreatureState.RESTING)) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_SOUL_BOUND_INVALID_STANCE(2800115));
            return false;
        }
        if (player.isInState(CreatureState.FLYING)) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_SOUL_BOUND_INVALID_STANCE(2800111));
            return false;
        }
        if (player.isInState(CreatureState.WEAPON_EQUIPPED)) {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_SOUL_BOUND_INVALID_STANCE(2800159));
            return false;
        }

        RequestResponseHandler responseHandler = new RequestResponseHandler(player) {

            @Override
            public void acceptRequest(Creature requester, Player responder) {
                player.getController().cancelUseItem();

                PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), item.getObjectId(), item.getItemId(), 5000, 4),
                    true);
                player.getController().cancelTask(TaskId.ITEM_USE);

                final ActionObserver moveObserver = new ActionObserver(ObserverType.MOVE) {
                    @Override
                    public void moved() {
                        player.getController().cancelTask(TaskId.ITEM_USE);
                        player.sendPck(SM_SYSTEM_MESSAGE.STR_SOUL_BOUND_ITEM_CANCELED(item.getNameID()));
                        PacketSendUtility.broadcastPacket(player,
                            new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), item.getObjectId(), item.getItemId(), 0, 8), true);
                    }
                };
                player.getObserveController().attach(moveObserver);

                // item usage animation
                player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

                    @Override
                    public void run() {
                        player.getObserveController().removeObserver(moveObserver);

                        PacketSendUtility.broadcastPacket(player,
                            new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), item.getObjectId(), item.getItemId(), 0, 6), true);
                        player.sendPck(SM_SYSTEM_MESSAGE.STR_SOUL_BOUND_ITEM_SUCCEED(item.getNameID()));

                        item.setSoulBound(true);
                        ItemPacketService.updateItemAfterInfoChange(owner, item);

                        equip(slot, item);
                        PacketSendUtility.broadcastPacket(player, new SM_UPDATE_PLAYER_APPEARANCE(player.getObjectId(), getEquippedForAppearance()), true);
                    }
                }, 5000));
            }

            @Override
            public void denyRequest(Creature requester, Player responder) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_SOUL_BOUND_ITEM_CANCELED(item.getNameID()));
            }
        };

        boolean requested = player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_SOUL_BOUND_ITEM_DO_YOU_WANT_SOUL_BOUND, responseHandler);
        if (requested) {
            player.sendPck(new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_SOUL_BOUND_ITEM_DO_YOU_WANT_SOUL_BOUND, 0, 0, DescId.of(
                item.getNameID())));
        } else {
            player.sendPck(SM_SYSTEM_MESSAGE.STR_SOUL_BOUND_CLOSE_OTHER_MSG_BOX_AND_RETRY);
        }
        return false;
    }

    private boolean verifyRankLimits(Item item) {

        if(owner.getAbyssRank() == null || owner.getAbyssRank().getRank()== null){
            log.warn("Player '" + (owner.getAbyssRank() == null ? "AbyssRank" : " Rank of AbyssRank") + " is null! Player id: " + owner.getObjectId());
            return false;
        }

        return verifyRankLimits(item, owner.getAbyssRank().getRank());
    }

    private boolean verifyRankLimits(Item item, AbyssRankEnum arank) {

        int rank = arank.getId();
        if (!item.getItemTemplate().getUseLimits().verifyRank(rank)) {
            return false;
        }
        return item.getFusionedItemTemplate() == null || item.getFusionedItemTemplate().getUseLimits().verifyRank(rank);
    }

    public void checkRankLimitItems() {
        for (Item item : getEquippedItems()) {
            if (!verifyRankLimits(item)) {
                unEquipItem(item.getObjectId(), item.getEquipmentSlot(), true);
                owner.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_UNEQUIP_RANKITEM(item.getNameID()));
            }
        }
    }

    public int getRankLimitedItemsCounter(AbyssRankEnum rank) {

        int result = 0;
        for (Item item : getEquippedItems()) {
            if (!verifyRankLimits(item, rank))
                result++;
        }

        return result;
    }
}
