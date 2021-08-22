/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.player;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ne.gs.database.GDB;
import com.ne.commons.utils.GenericValidator;
import com.ne.gs.controllers.FlyController;
import com.ne.gs.controllers.PlayerController;
import com.ne.gs.controllers.WindstreamController;
import com.ne.gs.controllers.effect.PlayerEffectController;
import com.ne.gs.database.dao.*;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.dataholders.PlayerInitialData;
import com.ne.gs.dataholders.PlayerInitialData.LocationData;
import com.ne.gs.dataholders.PlayerInitialData.PlayerCreationData;
import com.ne.gs.dataholders.PlayerInitialData.PlayerCreationData.ItemType;
import com.ne.gs.model.account.Account;
import com.ne.gs.model.account.PlayerAccountData;
import com.ne.gs.model.conds.*;
import com.ne.gs.model.events.ExceptBuffHandler;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.PersistentState;
import com.ne.gs.model.gameobjects.player.Equipment;
import com.ne.gs.model.gameobjects.player.MacroList;
import com.ne.gs.model.gameobjects.player.Mailbox;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerAppearance;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.model.items.ItemSlot;
import com.ne.gs.model.items.storage.PlayerStorage;
import com.ne.gs.model.items.storage.Storage;
import com.ne.gs.model.items.storage.StorageType;
import com.ne.gs.model.skill.PlayerSkillList;
import com.ne.gs.model.stats.calc.functions.PlayerStatFunctions;
import com.ne.gs.model.stats.container.PlayerGameStats;
import com.ne.gs.model.stats.container.PlayerLifeStats;
import com.ne.gs.model.stats.listeners.TitleChangeListener;
import com.ne.gs.model.team.legion.LegionMember;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.modules.ffaloc.FFALoc;
import com.ne.gs.services.LegionService;
import com.ne.gs.services.PunishmentService.PunishmentType;
import com.ne.gs.services.SkillLearnService;
import com.ne.gs.services.item.ItemFactory;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.world.World;
import com.ne.gs.world.WorldPosition;
import com.ne.gs.world.knownlist.KnownList;
import com.ne.gs.world.knownlist.Visitor;

/**
 * This class is designed to do all the work related with loading/storing players.<br>
 * Same with storing, {@link #storePlayer(com.ne.gs.model.gameobjects.player.Player)} stores all player data like appearance, items, etc...
 *
 * @author SoulKeeper, Saelya, Cura
 */
public final class PlayerService {

    /**
     * Checks if name is already taken or not
     *
     * @param name
     *     character name
     *
     * @return true if is free, false in other case
     */
    public static boolean isFreeName(String name) {
        return !GDB.get(PlayerDAO.class).isNameUsed(name);
    }

    public static boolean isOldName(String name) {
        return GDB.get(OldNamesDAO.class).isOldName(name);
    }

    /**
     * Stores newly created player
     *
     * @param player
     *     player to store
     *
     * @return true if character was successful saved.
     */
    public static boolean storeNewPlayer(Player player, String accountName, int accountId) {
        return GDB.get(PlayerDAO.class).saveNewPlayer(player.getCommonData(), accountId, accountName)
            && GDB.get(PlayerAppearanceDAO.class).store(player) && GDB.get(PlayerSkillListDAO.class).storeSkills(player)
            && GDB.get(InventoryDAO.class).store(player);
    }

    /**
     * Stores player data into db
     *
     * @param player
     */
    public static void storePlayer(Player player) {
        GDB.get(PlayerDAO.class).storePlayer(player);
        GDB.get(PlayerSkillListDAO.class).storeSkills(player);
        GDB.get(PlayerSettingsDAO.class).saveSettings(player);
        PlayerQuestListTable.store(player);
        CustomPlayerQuestListTable.store(player);
        GDB.get(AbyssRankDAO.class).storeAbyssRank(player);
        GDB.get(PlayerPunishmentsDAO.class).storePlayerPunishments(player, PunishmentType.PRISON);
        GDB.get(PlayerPunishmentsDAO.class).storePlayerPunishments(player, PunishmentType.GAG);
        GDB.get(PlayerPunishmentsDAO.class).storePlayerPunishments(player, PunishmentType.GATHER);
        GDB.get(InventoryDAO.class).store(player);
        GDB.get(ItemStoneListDAO.class).save(player);
        GDB.get(MailDAO.class).storeMailbox(player);
        GDB.get(PortalCooldownsDAO.class).storePortalCooldowns(player);
        GDB.get(CraftCooldownsDAO.class).storeCraftCooldowns(player);
        GDB.get(PlayerNpcFactionsDAO.class).storeNpcFactions(player);
    }

    /**
     * Returns the player with given objId (if such player exists)
     *
     * @param playerObjId
     * @param account
     *
     * @return Player
     */
    public static Player getPlayer(int playerObjId, Account account) {
        /**
         * Player common data and appearance should be already loaded in account
         */
        PlayerAccountData playerAccountData = account.getPlayerAccountData(playerObjId);
        PlayerCommonData pcd = playerAccountData.getPlayerCommonData();
        PlayerAppearance appearance = playerAccountData.getAppereance();

        Player player = new Player(new PlayerController(), pcd, appearance, account);
        player.getPetList().loadPets();
        player.setPlayerStatsTemplate(DataManager.PLAYER_STATS_DATA.getTemplate(player));
        player.setGameStats(new PlayerGameStats(player));
        player.setLifeStats(new PlayerLifeStats(player));

        player.getConditioner().attach(CanChat.STATIC);
        player.getConditioner().attach(CanEmote.TRUE);
        player.getConditioner().attach(CanBeInvitedToGroup.TRUE);
        player.getConditioner().attach(CanBeInvitedToAlliance.TRUE);
        player.getConditioner().attach(CanInviteToAlliance.STATIC);
        player.getConditioner().attach(CanInviteToGroup.STATIC);
        player.getConditioner().attach(CanReadChatMessageCond.STATIC);
        player.getConditioner().attach(CanSummonPet.TRUE);
        player.getConditioner().attach(IsAggroIconCond.STATIC);
        //player.getConditioner().attach(IsEnemyCond.STATIC);
        player.getConditioner().attach(SkillUseCond.STATIC);
        player.getConditioner().attach(SpawnObjCond.STATIC);

        player.getConditioner().attach(FFALoc.IsLegionVisible.STATIC);

        player.getImplementator().attach(FFALoc.VisualEquipment.STATIC);
        player.getImplementator().attach(FFALoc.VisualRank.STATIC);
        player.getImplementator().attach(FFALoc.VisiblePlayerName.STATIC);

        player.getChainer().attach(ExceptBuffHandler.STATIC);

        LegionMember legionMember = LegionService.getInstance().getLegionMember(player.getObjectId());
        if (legionMember != null) {
            player.setLegionMember(legionMember);
        }

        MacroList macroses = GDB.get(PlayerMacrossesDAO.class).restoreMacrosses(playerObjId);
        player.setMacroList(macroses);

        player.setSkillList(GDB.get(PlayerSkillListDAO.class).loadSkillList(playerObjId));
        player.setKnownlist(new KnownList(player));
        player.setFriendList(GDB.get(FriendListDAO.class).load(player));
        player.setBlockList(GDB.get(BlockListDAO.class).load(player));
        player.setTitleList(GDB.get(PlayerTitleListDAO.class).loadTitleList(playerObjId));
        GDB.get(PlayerSettingsDAO.class).loadSettings(player);
        GDB.get(AbyssRankDAO.class).loadAbyssRank(player);
        GDB.get(PlayerNpcFactionsDAO.class).loadNpcFactions(player);
        GDB.get(MotionDAO.class).loadMotionList(player);
        Equipment equipment = GDB.get(InventoryDAO.class).loadEquipment(player);
        ItemService.loadItemStones(equipment.getEquippedItemsWithoutStigma());
        equipment.setOwner(player);
        player.setEquipment(equipment);
        player.setEffectController(new PlayerEffectController(player));
        player.setFlyController(new FlyController(player));
	    player.setWindstreamController(new WindstreamController(player));
        PlayerStatFunctions.addPredefinedStatFunctions(player);

        player.setQuestStateList(PlayerQuestListTable.load(player));
        player.setCustomQuestStateList(CustomPlayerQuestListTable.load(player));
        player.setRecipeList(GDB.get(PlayerRecipesDAO.class).load(player.getObjectId()));

        /**
         * Account warehouse should be already loaded in account
         */
        Storage accWarehouse = account.getAccountWarehouse();
        player.setStorage(accWarehouse, StorageType.ACCOUNT_WAREHOUSE);

        Storage inventory = GDB.get(InventoryDAO.class).loadStorage(playerObjId, StorageType.CUBE);
        ItemService.loadItemStones(inventory.getItems());

        player.setStorage(inventory, StorageType.CUBE);

        for (int petBagId = 32; petBagId < 36; petBagId++) {
            Storage petBag = GDB.get(InventoryDAO.class).loadStorage(playerObjId, StorageType.getStorageTypeById(petBagId));
            ItemService.loadItemStones(petBag.getItems());

            player.setStorage(petBag, StorageType.getStorageTypeById(petBagId));
        }

        for (int houseWhId = 60; houseWhId < 80; houseWhId++) {
            StorageType whType = StorageType.getStorageTypeById(houseWhId);
            if (whType != null) {
                Storage cabinet = GDB.get(InventoryDAO.class).loadStorage(playerObjId, StorageType.getStorageTypeById(houseWhId));

                ItemService.loadItemStones(cabinet.getItems());
                player.setStorage(cabinet, StorageType.getStorageTypeById(houseWhId));
            }
        }
        Storage warehouse = GDB.get(InventoryDAO.class).loadStorage(playerObjId, StorageType.REGULAR_WAREHOUSE);
        ItemService.loadItemStones(warehouse.getItems());

        player.setStorage(warehouse, StorageType.REGULAR_WAREHOUSE);

        player.getEquipment().onLoadApplyEquipmentStats();

        GDB.get(PlayerPunishmentsDAO.class).loadPlayerPunishments(player, PunishmentType.PRISON);
        GDB.get(PlayerPunishmentsDAO.class).loadPlayerPunishments(player, PunishmentType.GAG);
        GDB.get(PlayerPunishmentsDAO.class).loadPlayerPunishments(player, PunishmentType.GATHER);

        // update passive stats after effect controller, stats and equipment are initialized
        player.getController().updatePassiveStats();
        // load saved effects
        GDB.get(PlayerEffectsDAO.class).loadPlayerEffects(player);
        // load saved player cooldowns
        GDB.get(PlayerCooldownsDAO.class).loadPlayerCooldowns(player);
        // load item cooldowns
        GDB.get(ItemCooldownsDAO.class).loadItemCooldowns(player);
        // load portal cooldowns
        GDB.get(PortalCooldownsDAO.class).loadPortalCooldowns(player);
        // load bind point
        GDB.get(PlayerBindPointDAO.class).loadBindPoint(player);
        // load craft cooldowns
        GDB.get(CraftCooldownsDAO.class).loadCraftCooldowns(player);

        if (player.getCommonData().getTitleId() > 0) {
            TitleChangeListener.onTitleChange(player.getGameStats(), player.getCommonData().getTitleId(), true);
        }

        GDB.get(PlayerLifeStatsDAO.class).loadPlayerLifeStat(player);
        GDB.get(PlayerEmotionListDAO.class).loadEmotions(player);

        return player;
    }

    /**
     * This method is used for creating new players
     *
     * @param playerCommonData
     * @param playerAppearance
     * @param account
     *
     * @return Player
     */
    public static Player newPlayer(PlayerCommonData playerCommonData, PlayerAppearance playerAppearance, Account account) {
        PlayerInitialData playerInitialData = DataManager.PLAYER_INITIAL_DATA;
        LocationData ld = playerInitialData.getSpawnLocation(playerCommonData.getRace());

        WorldPosition position = World.getInstance().createPosition(ld.getMapId(), ld.getX(), ld.getY(), ld.getZ(), ld.getHeading(), 0);
        playerCommonData.setPosition(position);

        Player newPlayer = new Player(new PlayerController(), playerCommonData, playerAppearance, account);

        // Starting skills
        newPlayer.setSkillList(new PlayerSkillList());
        SkillLearnService.addNewSkills(newPlayer);

        // Starting items
        PlayerCreationData playerCreationData = playerInitialData.getPlayerCreationData(playerCommonData.getRace(), playerCommonData.getPlayerClass());
        Storage playerInventory = new PlayerStorage(StorageType.CUBE);
        Storage regularWarehouse = new PlayerStorage(StorageType.REGULAR_WAREHOUSE);
        Storage accountWarehouse = new PlayerStorage(StorageType.ACCOUNT_WAREHOUSE);
        Equipment equipment = new Equipment(newPlayer);
        if (playerCreationData != null) { // player transfer
            List<ItemType> items = playerCreationData.getItems();
            for (ItemType itemType : items) {
                int itemId = itemType.getTemplate().getTemplateId();
                Item item = ItemFactory.newItem(itemId, itemType.getCount());
                if (item == null) {
                    continue;
                }

                // When creating new player - all equipment that has slot values will be equipped
                // Make sure you will not put into xml file more items than possible to equip.
                ItemTemplate itemTemplate = item.getItemTemplate();

                if ((itemTemplate.isArmor() || itemTemplate.isWeapon()) && !equipment.isSlotEquipped(itemTemplate.getItemSlot())) {
                    item.setEquipped(true);
                    ItemSlot itemSlot = ItemSlot.getSlotFor(itemTemplate.getItemSlot());
                    item.setEquipmentSlot(itemSlot.id());
                    equipment.onLoadHandler(item);
                } else {
                    playerInventory.onLoadHandler(item);
                }
            }
        }
        newPlayer.setStorage(playerInventory, StorageType.CUBE);
        newPlayer.setStorage(regularWarehouse, StorageType.REGULAR_WAREHOUSE);
        newPlayer.setStorage(accountWarehouse, StorageType.ACCOUNT_WAREHOUSE);
        newPlayer.setEquipment(equipment);
        newPlayer.setMailbox(new Mailbox(newPlayer));

        for (int petBagId = 32; petBagId < 36; petBagId++) {
            Storage petBag = new PlayerStorage(StorageType.getStorageTypeById(petBagId));
            newPlayer.setStorage(petBag, StorageType.getStorageTypeById(petBagId));
        }

        for (int houseWhId = 60; houseWhId < 80; houseWhId++) {
            StorageType whType = StorageType.getStorageTypeById(houseWhId);
            if (whType != null) {
                Storage cabinet = new PlayerStorage(whType);
                newPlayer.setStorage(cabinet, StorageType.getStorageTypeById(houseWhId));
            }

        }

        playerInventory.setPersistentState(PersistentState.UPDATE_REQUIRED);
        equipment.setPersistentState(PersistentState.UPDATE_REQUIRED);
        return newPlayer;
    }

    /**
     * Cancel Player deletion process if its possible.
     *
     * @param accData
     *     PlayerAccountData
     *
     * @return True if deletion was successful canceled.
     */
    public static boolean cancelPlayerDeletion(PlayerAccountData accData) {
        if (accData.getDeletionDate() == null) {
            return true;
        }

        if (accData.getDeletionDate().getTime() > System.currentTimeMillis()) {
            accData.setDeletionDate(null);
            storeDeletionTime(accData);
            return true;
        }
        return false;
    }

    /**
     * Starts player deletion process if its possible. If deletion is possible character should be deleted after 5 minutes.
     *
     * @param accData
     *     PlayerAccountData
     */
    public static void deletePlayer(PlayerAccountData accData) {
        if (accData.getDeletionDate() != null) {
            return;
        }

        accData.setDeletionDate(new Timestamp(System.currentTimeMillis() + 5 * 60 * 1000));
        storeDeletionTime(accData);
    }

    /**
     * Completely removes player from database
     *
     * @param playerId
     *     id of player to delete from db
     */
    public static void deletePlayerFromDB(int playerId) {
        GDB.get(InventoryDAO.class).deletePlayerItems(playerId);
        GDB.get(PlayerDAO.class).deletePlayer(playerId);
    }

    /**
     * Updates deletion time in database
     *
     * @param accData
     *     PlayerAccountData
     */
    private static void storeDeletionTime(PlayerAccountData accData) {
        GDB.get(PlayerDAO.class).updateDeletionTime(accData.getPlayerCommonData().getPlayerObjId(), accData.getDeletionDate());
    }

    /**
     * @param objectId
     * @param creationDate
     */
    public static void storeCreationTime(int objectId, Timestamp creationDate) {
        GDB.get(PlayerDAO.class).storeCreationTime(objectId, creationDate);
    }

    /**
     * Add macro for player
     *
     * @param player
     *     Player
     * @param macroOrder
     *     Macro order
     * @param macroXML
     *     Macro XML
     */
    public static void addMacro(Player player, int macroOrder, String macroXML) {
        if (player.getMacroList().addMacro(macroOrder, macroXML)) {
            GDB.get(PlayerMacrossesDAO.class).addMacro(player.getObjectId(), macroOrder, macroXML);
        } else {
            GDB.get(PlayerMacrossesDAO.class).updateMacro(player.getObjectId(), macroOrder, macroXML);
        }
    }

    /**
     * Remove macro with specified index from specified player
     *
     * @param player
     *     Player
     * @param macroOrder
     *     Macro order index
     */
    public static void removeMacro(Player player, int macroOrder) {
        if (player.getMacroList().removeMacro(macroOrder)) {
            GDB.get(PlayerMacrossesDAO.class).deleteMacro(player.getObjectId(), macroOrder);
        }
    }

    public static String getPlayerName(Integer objectId) {
        return getPlayerNames(Collections.singleton(objectId)).get(objectId);
    }

    public static Map<Integer, String> getPlayerNames(Collection<Integer> playerObjIds) {

        // if there is no ids - return just empty map
        if (GenericValidator.isBlankOrNull(playerObjIds)) {
            return Collections.emptyMap();
        }

        final Map<Integer, String> result = Maps.newHashMap();

        // Copy ids to separate set
        // It's dangerous to modify input collection, can have side results
        final Set<Integer> playerObjIdsCopy = Sets.newHashSet(playerObjIds);

        // Get names of all online players
        // Certain names can be changed in runtime
        // this should prevent errors
        World.getInstance().doOnAllPlayers(new Visitor<Player>() {
            @Override
            public void visit(Player object) {
                if (playerObjIdsCopy.contains(object.getObjectId())) {
                    result.put(object.getObjectId(), object.getName());
                    playerObjIdsCopy.remove(object.getObjectId());
                }
            }
        });

        result.putAll(GDB.get(PlayerDAO.class).getPlayerNames(playerObjIdsCopy));
        return result;
    }
}
