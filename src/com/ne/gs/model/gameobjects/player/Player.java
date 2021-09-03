/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player;

import com.google.common.collect.ImmutableMap;
import com.ne.commons.Sys;
import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.utils.L10N;
import com.ne.gs.configs.administration.AdminConfig;
import com.ne.gs.configs.main.MembershipConfig;
import com.ne.gs.configs.main.SecurityConfig;
import com.ne.gs.controllers.FlyController;
import com.ne.gs.controllers.PlayerController;
import com.ne.gs.controllers.WindstreamController;
import com.ne.gs.controllers.attack.AggroList;
import com.ne.gs.controllers.attack.AttackStatus;
import com.ne.gs.controllers.attack.PlayerAggroList;
import com.ne.gs.controllers.effect.PlayerEffectController;
import com.ne.gs.controllers.movement.PlayerMoveController;
import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.*;
import com.ne.gs.model.account.Account;
import com.ne.gs.model.actions.PlayerActions;
import com.ne.gs.model.actions.PlayerMode;
import com.ne.gs.model.conds.IsAggroIconCond;
import com.ne.gs.model.gameobjects.*;
import com.ne.gs.model.gameobjects.player.AbyssRank.AbyssRankUpdateType;
import com.ne.gs.model.gameobjects.player.emotion.EmotionList;
import com.ne.gs.model.gameobjects.player.motion.MotionList;
import com.ne.gs.model.gameobjects.player.npcFaction.NpcFactions;
import com.ne.gs.model.gameobjects.player.title.TitleList;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.model.gameobjects.state.CreatureVisualState;
import com.ne.gs.model.ingameshop.InGameShop;
import com.ne.gs.model.items.storage.IStorage;
import com.ne.gs.model.items.storage.LegionStorageProxy;
import com.ne.gs.model.items.storage.Storage;
import com.ne.gs.model.items.storage.StorageType;
import com.ne.gs.model.skill.PlayerSkillList;
import com.ne.gs.model.stats.container.PlayerGameStats;
import com.ne.gs.model.stats.container.PlayerLifeStats;
import com.ne.gs.model.team.legion.Legion;
import com.ne.gs.model.team.legion.LegionMember;
import com.ne.gs.model.team2.TeamMember;
import com.ne.gs.model.team2.TemporaryPlayerTeam;
import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.alliance.PlayerAllianceGroup;
import com.ne.gs.model.team2.common.legacy.LootGroupRules;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.model.templates.BoundRadius;
import com.ne.gs.model.templates.flypath.FlyPathEntry;
import com.ne.gs.model.templates.item.ItemAttackType;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.model.templates.item.ItemUseLimits;
import com.ne.gs.model.templates.npc.AbyssNpcType;
import com.ne.gs.model.templates.ride.RideInfo;
import com.ne.gs.model.templates.stats.PlayerStatsTemplate;
import com.ne.gs.model.templates.windstreams.WindstreamPath;
import com.ne.gs.model.templates.zone.ZoneClassName;
import com.ne.gs.model.templates.zone.ZoneType;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.serverpackets.SM_ITEM_COOLDOWN;
import com.ne.gs.network.aion.serverpackets.SM_MESSAGE;
import com.ne.gs.network.aion.serverpackets.SM_SKILL_COOLDOWN;
import com.ne.gs.network.aion.serverpackets.SM_STATS_INFO;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;
import com.ne.gs.services.AccessLevelEnum;
import com.ne.gs.services.instance.InstanceService;
import com.ne.gs.skillengine.condition.ChainCondition;
import com.ne.gs.skillengine.effect.AbnormalState;
import com.ne.gs.skillengine.effect.EffectTemplate;
import com.ne.gs.skillengine.effect.RebirthEffect;
import com.ne.gs.skillengine.effect.ResurrectBaseEffect;
import com.ne.gs.skillengine.model.ChainSkills;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.SkillTemplate;
import com.ne.gs.skillengine.task.CraftingTask;
import com.ne.gs.utils.rates.Rates;
import com.ne.gs.utils.rates.RegularRates;
import com.ne.gs.world.InstanceType;
import com.ne.gs.world.World;
import com.ne.gs.world.WorldPosition;
import com.ne.gs.world.zone.ZoneName;
import gnu.trove.map.hash.THashMap;
import javolution.util.FastList;

import java.sql.Timestamp;
import java.util.*;

/**
 * This class is representing Player object, it contains all needed data.
 *
 * @author -Nemesiss-
 * @author SoulKeeper
 * @author alexa026
 * @author cura
 */
public class Player extends Creature {

    private static final int CUBE_SPACE = 9;
    private static final int WAREHOUSE_SPACE = 8;
    private final PlayerCommonData playerCommonData;
    private final Account playerAccount;
    private final Storage[] petBag = new Storage[4];
    private final Storage[] cabinets = new Storage[20];
    private final Map<AttackStatus, Long> lastCounterSkill = new HashMap<>();
    public RideInfo ride;
    public InRoll inRoll;
    public InGameShop inGameShop;
    public WindstreamPath windstreamPath;
    public int speedHackCounter;
    public int abnormalHackCounter;
    public WorldPosition prevPos = new WorldPosition();
    public long prevPosUT;
    public byte prevMoveType;
    private PlayerAppearance playerAppearance;
    private PlayerAppearance savedPlayerAppearance;
    private LegionMember legionMember;
    private MacroList macroList;
    private PlayerSkillList skillList;
    private BlockList blockList;
    private PetList toyPetList;
    private Mailbox mailbox;
    private PrivateStore store;
    private TitleList titleList;
    private QuestStateList questStateList;
    private QuestStateList customQuestStateList;
    private RecipeList recipeList;
    private ResponseRequester requester;
    private boolean lookingForGroup = false;
    private Storage inventory;
    private Storage regularWarehouse;
    private Storage accountWarehouse;
    private Equipment equipment;
    private PlayerStatsTemplate playerStatsTemplate;
    private PlayerSettings playerSettings;
    private PlayerGroup playerGroup2;
    private PlayerAllianceGroup playerAllianceGroup;
    private AbyssRank abyssRank;
    private NpcFactions npcFactions;
    private Rates rates = new RegularRates();
    private int flyState = 0;
    private int afterFly = 0;
    private boolean isTrading;
    private long prisonTimer = 0;
    private long startPrison;
    private String prisonReason;
    private long gagTimer = 0;
    private long startGag;
    private String gagReason;
    private boolean invul;
    private FlyController flyController;
    private WindstreamController windstreamController;
    private CraftingTask craftingTask;
    private int flightTeleportId;
    private int flightDistance;
    private Summon summon;
    private SummonedObject<?> summonedObj;
    private Pet toyPet;
    private Kisk kisk;
    private boolean isResByPlayer = false;
    private int resurrectionSkill = 0;
    private boolean isFlyingBeforeDeath = false;
    private boolean edit_mode = false;
    private Npc postman = null;
    private boolean isInResurrectPosState = false;
    private float resPosX = 0;
    private float resPosY = 0;
    private float resPosZ = 0;
    private boolean underNoFPConsum = false;
    private boolean isAdminTeleportation = false;
    private boolean cooldownZero = false;
    private boolean isUnderInvulnerableWing = false;
    private boolean isFlying = false;
    private boolean isWispable = true;
    private int abyssRankListUpdateMask = 0;
    private BindPointPosition bindPoint;
    private final Map<Integer, Tuple2<Long, Integer>> itemCoolDowns = new THashMap<>(0);
    private PortalCooldownList portalCooldownList;
    private CraftCooldownList craftCooldownList;
    private long nextSkillUse;
    private long nextSummonSkillUse;
    private final ChainSkills chainSkills = new ChainSkills();
    private int dualEffectValue = 0;
    private int rawKillcount = 0;
    private int spreeLevel = 0;
    private boolean isAttackMode = false;
    private long gatherableTimer = 0;
    private long stopGatherable;
    private String captchaWord;
    private byte[] captchaImage;
    private float instanceStartPosX, instanceStartPosY, instanceStartPosZ;
    private int rebirthResurrectPercent = 1;
    private int rebirthSkill = 0;
    /**
     * Connection of this Player.
     */
    private AionConnection clientConnection;
    private FlyPathEntry flyLocationId;
    private long flyStartTime;
    private EmotionList emotions;
    private MotionList motions;
    private int partnerId;
    private long flyReuseTime;
    private boolean isMentor;
    private long lastMsgTime = 0;
    private int floodMsgCount = 0;
    private long onlineTime = 0;
    private int lootingNpcOid;
    private boolean rebirthRevive;
    private int subtractedSupplementsCount;
    private int subtractedSupplementId;
    private int portAnimation;
    private boolean isInSprintMode;
    private List<ActionObserver> rideObservers;
    private boolean rmloc;
    private String nameFormat = "%s";
    private String newname = null;

    private L10N.Lang _lang = L10N.language; // FIXME temp
    private Coordinates coordinates;
    private Coordinates saveCoordinates;

    public Player(PlayerController controller,
            PlayerCommonData plCommonData,
            PlayerAppearance appereance,
            Account account) {
        super(plCommonData.getPlayerObjId(), controller, null, plCommonData, plCommonData.getPosition());
        playerCommonData = plCommonData;
        playerAppearance = appereance;
        playerAccount = account;

        requester = new ResponseRequester(this);
        questStateList = new QuestStateList();
        titleList = new TitleList();
        portalCooldownList = new PortalCooldownList(this);
        craftCooldownList = new CraftCooldownList(this);
        toyPetList = new PetList(this);
        controller.setOwner(this);
        moveController = new PlayerMoveController(this);
        plCommonData.setBoundingRadius(new BoundRadius(0.5f, 0.5f, getPlayerAppearance().getHeight()));
        inGameShop = new InGameShop();

    }

    public boolean isInPlayerMode(PlayerMode mode) {
        return PlayerActions.isInPlayerMode(this, mode);
    }

    public void setPlayerMode(PlayerMode mode, Object obj) {
        PlayerActions.setPlayerMode(this, mode, obj);
    }

    public void unsetPlayerMode(PlayerMode mode) {
        PlayerActions.unsetPlayerMode(this, mode);
    }

    @Override
    public PlayerMoveController getMoveController() {
        return (PlayerMoveController) super.getMoveController();
    }

    @Override
    protected final AggroList createAggroList() {
        return new PlayerAggroList(this);
    }

    public PlayerCommonData getCommonData() {
        return playerCommonData;
    }

    @Override
    public String getName() {
        return playerCommonData.getName();
    }

    public PlayerAppearance getPlayerAppearance() {
        return playerAppearance;
    }

    public void setPlayerAppearance(PlayerAppearance playerAppearance) {
        this.playerAppearance = playerAppearance;
    }

    /**
     * Only use for the Size admin command
     *
     * @return PlayerAppearance : The saved player's appearance, to rollback his
     * appearance
     */
    public PlayerAppearance getSavedPlayerAppearance() {
        return savedPlayerAppearance;
    }

    /**
     * Only use for the Size admin command
     *
     * @param savedPlayerAppearance PlayerAppearance : The saved player's
     * appearance, to rollback his appearance
     */
    public void setSavedPlayerAppearance(PlayerAppearance savedPlayerAppearance) {
        this.savedPlayerAppearance = savedPlayerAppearance;
    }

    /**
     * Get connection of this player.
     *
     * @return AionConnection of this player.
     */
    public AionConnection getClientConnection() {
        return clientConnection;
    }

    /**
     * Set connection of this player.
     */
    public void setClientConnection(AionConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    public MacroList getMacroList() {
        return macroList;
    }

    public void setMacroList(MacroList macroList) {
        this.macroList = macroList;
    }

    public PlayerSkillList getSkillList() {
        return skillList;
    }

    public void setSkillList(PlayerSkillList skillList) {
        this.skillList = skillList;
    }

    /**
     * @return the toyPet
     */
    public Pet getPet() {
        return toyPet;
    }

    /**
     * @param toyPet the toyPet to set
     */
    public void setToyPet(Pet toyPet) {
        this.toyPet = toyPet;
    }

    /**
     * Gets this players Friend List
     *
     * @return FriendList
     */
    public FriendList getFriendList() {
        return getCommonData().getFriendList();
    }

    /**
     * Sets this players friend list. <br />
     * Remember to send the player the <tt>SM_FRIEND_LIST</tt> packet.
     */
    public void setFriendList(FriendList list) {
        getCommonData().setFriendList(list);
    }

    /**
     * Is this player looking for a group
     *
     * @return true or false
     */
    public boolean isLookingForGroup() {
        return lookingForGroup;
    }

    /**
     * Sets whether or not this player is looking for a group
     */
    public void setLookingForGroup(boolean lookingForGroup) {
        this.lookingForGroup = lookingForGroup;
    }

    public boolean isAttackMode() {
        return isAttackMode;
    }

    public void setAttackMode(boolean isAttackMode) {
        this.isAttackMode = isAttackMode;
    }

    public boolean isNotGatherable() {
        return gatherableTimer != 0;
    }

    public long getGatherableTimer() {
        return gatherableTimer;
    }

    public void setGatherableTimer(long gatherableTimer) {
        if (gatherableTimer < 0) {
            gatherableTimer = 0;
        }

        this.gatherableTimer = gatherableTimer;
    }

    public long getStopGatherable() {
        return stopGatherable;
    }

    public void setStopGatherable(long stopGatherable) {
        this.stopGatherable = stopGatherable;
    }

    public String getCaptchaWord() {
        return captchaWord;
    }

    public void setCaptchaWord(String captchaWord) {
        this.captchaWord = captchaWord;
    }

    public byte[] getCaptchaImage() {
        return captchaImage;
    }

    public void setCaptchaImage(byte[] captchaImage) {
        this.captchaImage = captchaImage;
    }

    public BlockList getBlockList() {
        return blockList;
    }

    public void setBlockList(BlockList list) {
        blockList = list;
    }

    public final PetList getPetList() {
        return toyPetList;
    }

    public QuestStateList getCustomQuestStateList() {
        return customQuestStateList;
    }

    public void setCustomQuestStateList(QuestStateList customQuestStateList) {
        this.customQuestStateList = customQuestStateList;
    }

    @Override
    public PlayerLifeStats getLifeStats() {
        return (PlayerLifeStats) super.getLifeStats();
    }

    @Override
    public PlayerGameStats getGameStats() {
        return (PlayerGameStats) super.getGameStats();
    }

    /**
     * Gets the ResponseRequester for this player
     *
     * @return ResponseRequester
     */
    public ResponseRequester getResponseRequester() {
        return requester;
    }

    public boolean isOnline() {
        return getClientConnection() != null;
    }

    public int getQuestExpands() {
        return playerCommonData.getQuestExpands();
    }

    public void setQuestExpands(int questExpands) {
        playerCommonData.setQuestExpands(questExpands);
        getInventory().setLimit(getInventory().getLimit() + (questExpands + getNpcExpands()) * CUBE_SPACE);
    }

    public int getNpcExpands() {
        return playerCommonData.getNpcExpands();
    }

    public void setNpcExpands(int npcExpands) {
        playerCommonData.setNpcExpands(npcExpands);
        getInventory().setLimit(getInventory().getLimit() + (npcExpands + getQuestExpands()) * CUBE_SPACE);
    }

    public PlayerClass getPlayerClass() {
        return playerCommonData.getPlayerClass();
    }

    public Gender getGender() {
        return playerCommonData.getGender();
    }

    /**
     * Return PlayerController of this Player Object.
     *
     * @return PlayerController.
     */
    @Override
    public PlayerController getController() {
        return (PlayerController) super.getController();
    }

    @Override
    public byte getLevel() {
        return (byte) playerCommonData.getLevel();
    }

    /**
     * @return the inventory
     */
    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    /**
     * @return the player private store
     */
    public PrivateStore getStore() {
        return store;
    }

    /**
     * @param store the store that needs to be set
     */
    public void setStore(PrivateStore store) {
        this.store = store;
    }

    /**
     * @return the questStatesList
     */
    public QuestStateList getQuestStateList() {
        return questStateList;
    }

    /**
     * @param questStateList the QuestStateList to set
     */
    public void setQuestStateList(QuestStateList questStateList) {
        this.questStateList = questStateList;
    }

    /**
     * @return the playerStatsTemplate
     */
    public PlayerStatsTemplate getPlayerStatsTemplate() {
        return playerStatsTemplate;
    }

    /**
     * @param playerStatsTemplate the playerStatsTemplate to set
     */
    public void setPlayerStatsTemplate(PlayerStatsTemplate playerStatsTemplate) {
        this.playerStatsTemplate = playerStatsTemplate;
    }

    public RecipeList getRecipeList() {
        return recipeList;
    }

    public void setRecipeList(RecipeList recipeList) {
        this.recipeList = recipeList;
    }

    public void setStorage(Storage storage, StorageType storageType) {
        if (storageType == StorageType.CUBE) {
            inventory = storage;
        }
        if (storageType.getId() > 31 && storageType.getId() < 36) {
            petBag[storageType.getId() - 32] = storage;
        }
        if (storageType.getId() > 59 && storageType.getId() < 80) {
            cabinets[storageType.getId() - 60] = storage;
        }
        if (storageType == StorageType.REGULAR_WAREHOUSE) {
            regularWarehouse = storage;
        }
        if (storageType == StorageType.ACCOUNT_WAREHOUSE) {
            accountWarehouse = storage;
        }
        storage.setOwner(this);
    }

    /**
     * @param storageType
     *
     * @return
     */
    public IStorage getStorage(int storageType) {
        if (storageType == StorageType.REGULAR_WAREHOUSE.getId()) {
            return regularWarehouse;
        }

        if (storageType == StorageType.ACCOUNT_WAREHOUSE.getId()) {
            return accountWarehouse;
        }

        if (storageType == StorageType.LEGION_WAREHOUSE.getId() && getLegion() != null) {
            return new LegionStorageProxy(getLegion().getLegionWarehouse(), this);
        }

        if (storageType > 31 && storageType < 36) {
            return petBag[storageType - 32];
        }

        if (storageType > 59 && storageType < 80) {
            return cabinets[storageType - 60];
        }

        if (storageType == StorageType.CUBE.getId()) {
            return inventory;
        }
        return null;
    }

    /**
     * Items from UPDATE_REQUIRED storages and equipment
     */
    public List<Item> getDirtyItemsToUpdate() {
        List<Item> dirtyItems = new ArrayList<>();

        IStorage cubeStorage = getStorage(StorageType.CUBE.getId());
        if (cubeStorage.getPersistentState() == PersistentState.UPDATE_REQUIRED) {
            dirtyItems.addAll(cubeStorage.getItemsWithKinah());
            dirtyItems.addAll(cubeStorage.getDeletedItems());
            cubeStorage.setPersistentState(PersistentState.UPDATED);
        }

        IStorage regularWhStorage = getStorage(StorageType.REGULAR_WAREHOUSE.getId());
        if (regularWhStorage.getPersistentState() == PersistentState.UPDATE_REQUIRED) {
            dirtyItems.addAll(regularWhStorage.getItemsWithKinah());
            dirtyItems.addAll(regularWhStorage.getDeletedItems());
            regularWhStorage.setPersistentState(PersistentState.UPDATED);
        }

        IStorage accountWhStorage = getStorage(StorageType.ACCOUNT_WAREHOUSE.getId());
        if (accountWhStorage.getPersistentState() == PersistentState.UPDATE_REQUIRED) {
            dirtyItems.addAll(accountWhStorage.getItemsWithKinah());
            dirtyItems.addAll(accountWhStorage.getDeletedItems());
            accountWhStorage.setPersistentState(PersistentState.UPDATED);
        }

        IStorage legionWhStorage = getStorage(StorageType.LEGION_WAREHOUSE.getId());
        if (legionWhStorage != null) {
            if (legionWhStorage.getPersistentState() == PersistentState.UPDATE_REQUIRED) {
                dirtyItems.addAll(legionWhStorage.getItemsWithKinah());
                dirtyItems.addAll(legionWhStorage.getDeletedItems());
                legionWhStorage.setPersistentState(PersistentState.UPDATED);
            }
        }

        for (int petBagId = 32; petBagId < 36; petBagId++) {
            IStorage petBag = getStorage(petBagId);
            if (petBag != null && petBag.getPersistentState() == PersistentState.UPDATE_REQUIRED) {
                dirtyItems.addAll(petBag.getItemsWithKinah());
                dirtyItems.addAll(petBag.getDeletedItems());
                petBag.setPersistentState(PersistentState.UPDATED);
            }
        }

        for (int houseWhId = 60; houseWhId < 80; houseWhId++) {
            IStorage cabinet = getStorage(houseWhId);
            if ((cabinet != null) && (cabinet.getPersistentState() == PersistentState.UPDATE_REQUIRED)) {
                dirtyItems.addAll(cabinet.getItemsWithKinah());
                dirtyItems.addAll(cabinet.getDeletedItems());
                cabinet.setPersistentState(PersistentState.UPDATED);
            }
        }

        Equipment equipment = getEquipment();
        if (equipment.getPersistentState() == PersistentState.UPDATE_REQUIRED) {
            dirtyItems.addAll(equipment.getEquippedItems());
            equipment.setPersistentState(PersistentState.UPDATED);
        }

        return dirtyItems;
    }

    /**
     * //TODO probably need to optimize here
     */
    public FastList<Item> getAllItems() {
        FastList<Item> items = FastList.newInstance();
        items.addAll(inventory.getItemsWithKinah());
        if (regularWarehouse != null) {
            items.addAll(regularWarehouse.getItemsWithKinah());
        }
        if (accountWarehouse != null) {
            items.addAll(accountWarehouse.getItemsWithKinah());
        }

        for (int petBagId = 32; petBagId < 36; petBagId++) {
            IStorage petBag = getStorage(petBagId);
            if (petBag != null) {
                items.addAll(petBag.getItemsWithKinah());
            }
        }

        for (int houseWhId = 60; houseWhId < 80; houseWhId++) {
            IStorage cabinet = getStorage(houseWhId);
            if (cabinet != null) {
                items.addAll(cabinet.getItemsWithKinah());
            }
        }

        items.addAll(getEquipment().getEquippedItems());
        return items;
    }

    public Storage getInventory() {
        return inventory;
    }

    /**
     * @return the playerSettings
     */
    public PlayerSettings getPlayerSettings() {
        return playerSettings;
    }

    /**
     * @param playerSettings the playerSettings to set
     */
    public void setPlayerSettings(PlayerSettings playerSettings) {
        this.playerSettings = playerSettings;
    }

    public TitleList getTitleList() {
        return titleList;
    }

    public void setTitleList(TitleList titleList) {
        if (havePermission(MembershipConfig.TITLES_ADDITIONAL_ENABLE)) {
            titleList.addEntry(102, 0);
            titleList.addEntry(103, 0);
            titleList.addEntry(104, 0);
            titleList.addEntry(105, 0);
            titleList.addEntry(106, 0);
            titleList.addEntry(146, 0);
            titleList.addEntry(151, 0);
            titleList.addEntry(152, 0);
            titleList.addEntry(160, 0);
            titleList.addEntry(161, 0);
        }
        this.titleList = titleList;
        titleList.setOwner(this);
    }

    public PlayerGroup getPlayerGroup2() {
        return playerGroup2;
    }

    public void setPlayerGroup2(PlayerGroup playerGroup) {
        playerGroup2 = playerGroup;
    }

    /**
     * @return the abyssRank
     */
    public AbyssRank getAbyssRank() {
        return abyssRank;
    }

    /**
     * @param abyssRank the abyssRank to set
     */
    public void setAbyssRank(AbyssRank abyssRank) {
        this.abyssRank = abyssRank;
    }

    @Override
    public PlayerEffectController getEffectController() {
        return (PlayerEffectController) super.getEffectController();
    }

    /**
     * Returns true if has valid LegionMember
     */
    public boolean isLegionMember() {
        return legionMember != null;
    }

    /**
     * @return the legionMember
     */
    public LegionMember getLegionMember() {
        return legionMember;
    }

    /**
     * @param legionMember the legionMember to set
     */
    public void setLegionMember(LegionMember legionMember) {
        this.legionMember = legionMember;
    }

    /**
     * @return the legion
     */
    public Legion getLegion() {
        return legionMember != null ? legionMember.getLegion() : null;
    }

    /**
     * Checks if object id's are the same
     *
     * @return true if the object id is the same
     */
    public boolean sameObjectId(int objectId) {
        return getObjectId() == objectId;
    }

    /**
     * @return true if a player has a store opened
     */
    public boolean hasStore() {
        return getStore() != null;
    }

    /**
     * Removes legion from player
     */
    public void resetLegionMember() {
        setLegionMember(null);
    }

    public boolean isInGroup2() {
        return playerGroup2 != null;
    }

    /**
     * Access level of this player
     *
     * @return byte
     */
    public byte getAccessLevel() {
        return playerAccount.getAccessLevel();
    }

    /**
     * accountName of this player
     *
     * @return int
     */
    public String getAcountName() {
        return playerAccount.getName();
    }

    public Rates getRates() {
        return rates;
    }

    public void setRates(Rates rates) {
        this.rates = rates;
    }

    /**
     * @return warehouse size
     */
    public int getWarehouseSize() {
        return playerCommonData.getWarehouseSize();
    }

    /**
     * @param warehouseSize
     */
    public void setWarehouseSize(int warehouseSize) {
        playerCommonData.setWarehouseSize(warehouseSize);
        getWarehouse().setLimit(getWarehouse().getLimit() + (warehouseSize * WAREHOUSE_SPACE));
    }

    /**
     * @return regularWarehouse
     */
    public Storage getWarehouse() {
        return regularWarehouse;
    }

    /**
     * 0: regular, 1: fly, 2: glide
     */
    public int getFlyState() {
        return flyState;
    }

    public void setFlyState(int flyState) {
        this.flyState = flyState;
        if (flyState == 1) {
            setFlyingMode(true);
        } else if (flyState == 0) {
            setFlyingMode(false);
        }
    }

    /**
     * 0: regular, 1: fly, 2: glide
     */
    public int getAfterFlying() {
        return afterFly;
    }

    public void setAfterFlying(int afterFly) {
        this.afterFly = afterFly;
    }

    /**
     * @return the isTrading
     */
    public boolean isTrading() {
        return isTrading;
    }

    /**
     * @param isTrading the isTrading to set
     */
    public void setTrading(boolean isTrading) {
        this.isTrading = isTrading;
    }

    /**
     * @return the isInPrison
     */
    public boolean isInPrison() {
        return prisonTimer != 0;
    }

    /**
     * @return the prisonTimer
     */
    public long getPrisonTimer() {
        return prisonTimer;
    }

    /**
     * @param prisonTimer the prisonTimer to set
     */
    public void setPrisonTimer(long prisonTimer) {
        if (prisonTimer < 0) {
            prisonTimer = 0;
        }

        this.prisonTimer = prisonTimer;
    }

    /**
     * @return the time in ms of start prison
     */
    public long getStartPrison() {
        return startPrison;
    }

    /**
     * @param start : The time in ms of start prison
     */
    public void setStartPrison(long start) {
        startPrison = start;
    }

    /**
     * @return the prison reason
     */
    public String getPrisonReason() {
        return prisonReason;
    }

    /**
     * @param reason : the prison reason
     */
    public void setPrisonReason(String reason) {
        prisonReason = reason;
    }

    /**
     * @return the isGag
     */
    public boolean isGag() {
        return gagTimer != 0;
    }

    /**
     * @return the gagTimer
     */
    public long getGagTimer() {
        return gagTimer;
    }

    /**
     * @param gagTimer the gagTimer to set
     */
    public void setGagTimer(long gagTimer) {
        if (gagTimer < 0) {
            gagTimer = 0;
        }

        this.gagTimer = gagTimer;
    }

    /**
     * @return the time in ms of start gag
     */
    public long getStartGag() {
        return startGag;
    }

    /**
     * @param start : The time in ms of start gag
     */
    public void setStartGag(long start) {
        startGag = start;
    }

    /**
     * @return the prison reason
     */
    public String getGagReason() {
        return gagReason;
    }

    /**
     * @param reason : the gag reason
     */
    public void setGagReason(String reason) {
        gagReason = reason;
    }

    /**
     * @return
     */
    public boolean isProtectionActive() {
        return isInVisualState(CreatureVisualState.BLINKING);
    }

    /**
     * Check is player is invul
     *
     * @return boolean
     */
    public boolean isInvul() {
        return invul;
    }

    /**
     * Sets invul on player
     *
     * @param invul - boolean
     */
    public void setInvul(boolean invul) {
        this.invul = invul;
    }

    public Mailbox getMailbox() {
        return mailbox;
    }

    public void setMailbox(Mailbox mailbox) {
        this.mailbox = mailbox;
    }

    /**
     * @return the flyController
     */
    public FlyController getFlyController() {
        return flyController;
    }

    /**
     * @param flyController the flyController to set
     */
    public void setFlyController(FlyController flyController) {
        this.flyController = flyController;
    }

    public void setWindstreamController(WindstreamController windstreamController) {
        this.windstreamController = windstreamController;
    }

    public WindstreamController getWindstreamControllder() {
        return windstreamController;
    }

    public int getLastOnline() {
        Timestamp lastOnline = playerCommonData.getLastOnline();
        if (lastOnline == null || isOnline()) {
            return 0;
        }

        return (int) (lastOnline.getTime() / 1000);
    }

    /**
     * @return
     */
    public CraftingTask getCraftingTask() {
        return craftingTask;
    }

    /**
     * @param craftingTask
     */
    public void setCraftingTask(CraftingTask craftingTask) {
        this.craftingTask = craftingTask;
    }

    /**
     * @return flightTeleportId
     */
    public int getFlightTeleportId() {
        return flightTeleportId;
    }

    /**
     * @param flightTeleportId
     */
    public void setFlightTeleportId(int flightTeleportId) {
        this.flightTeleportId = flightTeleportId;
    }

    /**
     * @param path
     */
    public void setCurrentFlypath(FlyPathEntry path) {
        flyLocationId = path;
        if (path != null) {
            flyStartTime = System.currentTimeMillis();
        } else {
            flyStartTime = 0;
        }
    }

    /**
     * @return flightDistance
     */
    public int getFlightDistance() {
        return flightDistance;
    }

    /**
     * @param flightDistance
     */
    public void setFlightDistance(int flightDistance) {
        this.flightDistance = flightDistance;

    }

    /**
     * @return
     */
    public boolean isUsingFlyTeleport() {
        return isInState(CreatureState.FLIGHT_TELEPORT) && flightTeleportId != 0;
    }

    public boolean isGM() {
        return getAccessLevel() >= AdminConfig.GM_LEVEL;
    }

    @Override
    public boolean isEnemy(Creature creature) {
        return creature.isEnemyFrom(this) || isEnemyFrom(creature);
    }

    /**
     * Npc enemies:<br>
     * - monsters<br>
     * - aggressive npcs<br>
     */
    @Override
    public boolean isEnemyFrom(Npc npc) {
        return npc.isAttackableNpc() || isAggroIconTo(npc);
    }

    /**
     * Player enemies:<br>
     * - different race<br>
     * - duel partner<br>
     * @param enemy
     * @return 
     */
    @Override
    public boolean isEnemyFrom(Player enemy) {  // исп когда игроки красные друг для друга
    if (Objects.equals(this.getObjectId(), enemy.getObjectId())) {
      return false;
    } else if (enemy.isInsideZoneClassName(ZoneClassName.PEACE)) {
      return false;
    } else if ((this.getAdminEnmity() > 1 || enemy.getAdminEnmity() > 1)) {
      return false;
    } else if (InstanceType.isInPeace(enemy) && !this.getController().isDueling(enemy)) {
      return false;
    } else if ((InstanceType.isInTVT(enemy) && isInSameTeam(enemy)) || (isInsideZoneClassName(ZoneClassName.TVT) && isInSameTeam(enemy))) {
      return false;
    } else if ((InstanceType.isInTVT(enemy) && !isInSameTeam(enemy)) || (isInsideZoneClassName(ZoneClassName.TVT) && !isInSameTeam(enemy))) {
      return true;
    } else if (InstanceType.isInFFA(enemy) || isInsideZoneClassName(ZoneClassName.FFA)) {
      return true;
    } else if (canPvP(enemy) || this.getController().isDueling(enemy)) {
      return true;
    } else if ((enemy.isInsideZone(ZoneName.get("LC1_PVP_SUB_C"))) || (enemy.isInsideZone(ZoneName.get("DC1_PVP_ZONE")))) {
      return true;// true - разрешает бить врага // false - запрещает бить врага 
    }               
    else {
      return false;
    }
  }

   private boolean canPvP(Player enemy) {
    int worldId = enemy.getWorldId();
    if (!enemy.getRace().equals(getRace())) {
      return this.isInsidePvPZone() && enemy.isInsidePvPZone();
    } else {
      if (worldId != 210020000 && worldId != 210040000 && worldId != 210060000 && worldId != 210070000 && worldId != 220020000
	      && worldId != 220040000 && worldId != 220050000 && worldId != 220080000 && worldId != 400010000 && worldId != 400020000
	      && worldId != 400040000 && worldId != 400050000 && worldId != 400060000 && worldId != 600090000 && worldId != 600100000
	      && worldId != 600110000) {
	return (this.isInsideZoneType(ZoneType.PVP) && enemy.isInsideZoneType(ZoneType.PVP) && !isInSameTeam(enemy));
      }
    }
    return false;
  } 
    
    public boolean isPvP(Player opponent) {
        int worldId = opponent.getWorldId();

        if (!opponent.getRace().equals(getRace())) {
            // TODO clear this shit later (required DP zone edit)
            if (worldId == 600020000 || worldId == 600030000) {
                return isInsideZoneType(ZoneType.PVP) && opponent.isInsideZoneType(ZoneType.PVP);
            }
            return true;
        }
        return false;
    }

    public boolean isAggroIconTo(Player player) {
        return getConditioner().check(IsAggroIconCond.class, Tuple.of(this, player));
    }

    public boolean isInSameTeam(Player player) {
        if (isInGroup2() && player.isInGroup2()) {
            return getPlayerGroup2().getTeamId().equals(player.getPlayerGroup2().getTeamId());
        } else if (isInAlliance2() && player.isInAlliance2()) {
            return getPlayerAlliance2().getObjectId().equals(player.getPlayerAlliance2().getObjectId());
        } else if (isInLeague() && player.isInLeague()) {
            return getPlayerAllianceGroup2().getObjectId().equals(player.getPlayerAllianceGroup2().getObjectId());
        }
        return false;
    }

    @Override
    public boolean canSee(Creature creature) {
        if (creature.isInVisualState(CreatureVisualState.BLINKING)) {
            return true;
        }
        if (((creature instanceof Player)) && (isInSameTeam((Player) creature))) {
            return true;
        }
        if (((creature instanceof Trap)) && (((Trap) creature).getCreator().getObjectId().equals(getObjectId()))) {
            return true;
        }
        return creature.getVisualState() <= getSeeState();
    }

    @Override
    public TribeClass getTribe() {
        TribeClass transformTribe = getTransformModel().getTribe();
        if (transformTribe != null) {
            return transformTribe;
        }
        return getRace() == Race.ELYOS ? TribeClass.PC : TribeClass.PC_DARK;
    }

    @Override
    public boolean isAggroFrom(Npc npc) {
        // npc's that are 10 or more levels lower don't get aggro on players
        return (isAggroIconTo(npc) && (npc.getTribe().isGuard()
                || npc.getObjectTemplate().getAbyssNpcType() != AbyssNpcType.NONE
                || npc.getLevel() + 10 > getLevel()
                || (npc.isInInstance() && InstanceService.isAggro(npc.getWorldId()))));
    }

    /**
     * Used in SM_NPC_INFO to check aggro irrespective to level
     */
    public boolean isAggroIconTo(Npc npc) {
        Race race = npc.getRace();
        TribeClass tribe = npc.getTribe();
        if (getAdminEnmity() == 1 || getAdminEnmity() == 3) {
            return true;
        }

        // Exception by Tribe
        if (tribe == TribeClass.USEALL) {
            return false;
        }
        // AbyssType != NONE -> SiegeNpc
        if (npc.getObjectTemplate().getAbyssNpcType() != AbyssNpcType.NONE) {
            return checkSiegeRelations(npc);
        }

        if (npc.getObjectTemplate().getNpcType().equals(NpcType.PEACE)) {
            return false;
        }

        if (npc.getObjectTemplate().getNpcType().equals(NpcType.INVULNERABLE)) {
            return false;
        }

        if (npc.getObjectTemplate().getNpcType() == NpcType.NON_ATTACKABLE && (npc.getWorldId() == 310010000 || npc
                .getWorldId() == 320010000)) {
            return false;
        }

        if(npc instanceof Kisk && npc.isInsideZoneType(ZoneType.NEUTRAL))
            return false;

        switch (getTribe()) {
            case PC:
                if (race == Race.ASMODIANS || tribe == null || tribe.isDarkGuard()) {
                    return true;
                }
                return DataManager.TRIBE_RELATIONS_DATA.isAggressiveRelation(tribe, TribeClass.PC);
            case PC_DARK:
                if (race == Race.ELYOS || tribe == null || tribe.isLightGuard()) {
                    return true;
                }
                return DataManager.TRIBE_RELATIONS_DATA.isAggressiveRelation(tribe, TribeClass.PC_DARK);
        }
        return false;
    }

    /*
     * Siege npc relations to player
     */
    public boolean checkSiegeRelations(Npc npc) {
        Race race = npc.getRace();
        NpcType npcType = npc.getNpcType();
        TribeClass tribe = npc.getTribe();
        // Artifact can't be Enemy
        if (npc.getObjectTemplate().getAbyssNpcType().equals(AbyssNpcType.ARTIFACT)) {
            return false;
        }
        // Exception friendly Balaur's
        if (race == Race.DRAKAN && npcType == NpcType.NON_ATTACKABLE) {
            return false;
        }
        switch (getRace()) {
            case ELYOS:
                // Elyos Gate
                if (race == Race.PC_LIGHT_CASTLE_DOOR) {
                    return false;
                }
                // Elyos General
                if (race == Race.GCHIEF_LIGHT) {
                    return false;
                }
                // Elyos Teleporter
                if (race == Race.TELEPORTER && tribe == TribeClass.GENERAL) {
                    return false;
                }
                // Elyos Shield generators
                if ((race == Race.CONSTRUCT || race == Race.BARRIER)
                        && (tribe == TribeClass.GENERAL || tribe == TribeClass.F4GUARD_LIGHT)) {
                    return false;
                }
                break;
            case ASMODIANS:
                // Asmo Gate
                if (race == Race.PC_DARK_CASTLE_DOOR) {
                    return false;
                }
                // Asmo General
                if (race == Race.GCHIEF_DARK) {
                    return false;
                }
                // Asmo Teleporter
                if (race == Race.TELEPORTER && tribe == TribeClass.GENERAL_DARK) {
                    return false;
                }
                // Elyos Shield generators
                if ((race == Race.CONSTRUCT || race == Race.BARRIER)
                        && (tribe == TribeClass.GENERAL_DARK || tribe == TribeClass.F4GUARD_DARK)) {
                    return false;
                }
                break;
        }
        return getRace() != race;
    }

    public Summon getSummon() {
        return summon;
    }

    public void setSummon(Summon summon) {
        this.summon = summon;
    }

    public SummonedObject<?> getSummonedObj() {
        return summonedObj;
    }

    public void setSummonedObj(SummonedObject<?> summonedObj) {
        this.summonedObj = summonedObj;
    }

    public Kisk getKisk() {
        return kisk;
    }

    public void setKisk(Kisk newKisk) {
        kisk = newKisk;
    }

    public boolean isItemUseDisabled(ItemUseLimits limits) {
        if (limits == null) {
            return false;
        }

        Integer delayId = limits.getDelayId();
        long now = Sys.millis();
        Tuple2<Long, Integer> itemCooldown;
        synchronized (itemCoolDowns) {
            if ((itemCooldown = itemCoolDowns.get(delayId)) == null) {
                return false;
            }

            if (itemCooldown._1 < now) {
                itemCoolDowns.remove(delayId);
                return false;
            }
        }
        return true;
    }

    public long getItemCoolDown(Integer delayId) {
        Tuple2<Long, Integer> itemCooldown;
        synchronized (itemCoolDowns) {
            if ((itemCooldown = itemCoolDowns.get(delayId)) == null) {
                return 0;
            }
        }
        return itemCooldown._1;
    }

    @NotNull
    public Map<Integer, Tuple2<Long, Integer>> getItemCoolDowns() {
        synchronized (itemCoolDowns) {
            return ImmutableMap.copyOf(itemCoolDowns);
        }
    }

    public void addItemCoolDown(Integer delayId, long nextUseTimeMs, int useDelay) {
        synchronized (itemCoolDowns) {
            itemCoolDowns.put(delayId, Tuple2.of(nextUseTimeMs, useDelay));
        }
    }

    public void removeItemCoolDown(Integer delayId) {
        synchronized (itemCoolDowns) {
            itemCoolDowns.remove(delayId);
        }
    }

    public void flushItemCd() {
        Map<Integer, Tuple2<Long, Integer>> copy;
        synchronized (itemCoolDowns) {
            for (Integer delayId : itemCoolDowns.keySet()) {
                itemCoolDowns.put(delayId, Tuple2.of((long) 0, 0));
            }
            copy = ImmutableMap.copyOf(itemCoolDowns);
        }
        sendPck(new SM_ITEM_COOLDOWN(copy));
    }

    public boolean isGagged() {
        return isGag();
    }

    public boolean getAdminTeleportation() {
        return isAdminTeleportation;
    }

    public void setAdminTeleportation(boolean isAdminTeleportation) {
        this.isAdminTeleportation = isAdminTeleportation;
    }

    public final boolean isCoolDownZero() {
        return cooldownZero;
    }

    public final void setCoolDownZero(boolean cooldownZero) {
        this.cooldownZero = cooldownZero;
    }

    public void setPlayerResActivate(boolean isActivated) {
        isResByPlayer = isActivated;
    }

    public boolean getResStatus() {
        return isResByPlayer;
    }

    public int getResurrectionSkill() {
        return resurrectionSkill;
    }

    public void setResurrectionSkill(int resurrectionSkill) {
        this.resurrectionSkill = resurrectionSkill;
    }

    public boolean getIsFlyingBeforeDeath() {
        return isFlyingBeforeDeath;
    }

    public void setIsFlyingBeforeDeath(boolean isActivated) {
        isFlyingBeforeDeath = isActivated;
    }

    public PlayerAlliance getPlayerAlliance2() {
        return playerAllianceGroup != null ? playerAllianceGroup.getAlliance() : null;
    }

    public PlayerAllianceGroup getPlayerAllianceGroup2() {
        return playerAllianceGroup;
    }

    public void setPlayerAllianceGroup2(PlayerAllianceGroup playerAllianceGroup) {
        this.playerAllianceGroup = playerAllianceGroup;
    }

    public boolean isInAlliance2() {
        return playerAllianceGroup != null;
    }

    public final boolean isInLeague() {
        return isInAlliance2() && getPlayerAlliance2().isInLeague();
    }

    public final boolean isInTeam() {
        return isInGroup2() || isInAlliance2();
    }

    /**
     * @return current {@link PlayerGroup}, {@link PlayerAlliance} or null
     */
    public final TemporaryPlayerTeam<? extends TeamMember<Player>> getCurrentTeam() {
        return isInGroup2() ? getPlayerGroup2() : getPlayerAlliance2();
    }

    /**
     * @return current {@link PlayerGroup}, {@link PlayerAllianceGroup} or null
     */
    public final TemporaryPlayerTeam<? extends TeamMember<Player>> getCurrentGroup() {
        return isInGroup2() ? getPlayerGroup2() : getPlayerAllianceGroup2();
    }

    /**
     * @return current team id
     */
    public final int getCurrentTeamId() {
        return isInTeam() ? getCurrentTeam().getTeamId() : 0;
    }

    public PortalCooldownList getPortalCooldownList() {
        return portalCooldownList;
    }

    public CraftCooldownList getCraftCooldownList() {
        return craftCooldownList;
    }

    /**
     * @author IlBuono
     */
    public void setEditMode(boolean edit_mode) {
        this.edit_mode = edit_mode;
    }

    /**
     * @author IlBuono
     */
    public boolean isInEditMode() {
        return edit_mode;
    }

    public Npc getPostman() {
        return postman;
    }

    public void setPostman(Npc postman) {
        this.postman = postman;
    }

    public Account getPlayerAccount() {
        return playerAccount;
    }

    /**
     * Quest completion
     */
    public boolean isCompleteQuest(int questId) {
        QuestState qs = getQuestStateList().getQuestState(questId);
        return qs != null && qs.getStatus() == QuestStatus.COMPLETE;
    }

    public long getNextSkillUse() {
        return nextSkillUse;
    }

    public void setNextSkillUse(long nextSkillUse) {
        this.nextSkillUse = nextSkillUse;
    }

    public long getNextSummonSkillUse() {
        return nextSummonSkillUse;
    }

    public void setNextSummonSkillUse(long nextSummonSkillUse) {
        this.nextSummonSkillUse = nextSummonSkillUse;
    }

    public ChainSkills getChainSkills() {
        return chainSkills;
    }

    public void setLastCounterSkill(AttackStatus status) {
        long time = System.currentTimeMillis();
        if (AttackStatus.getBaseStatus(status) == AttackStatus.DODGE && PlayerClass
                .getStartingClassFor(getPlayerClass()) == PlayerClass.SCOUT) {
            lastCounterSkill.put(AttackStatus.DODGE, time);
        } else if (AttackStatus.getBaseStatus(status) == AttackStatus.PARRY
                && (getPlayerClass() == PlayerClass.GLADIATOR || getPlayerClass() == PlayerClass.CHANTER)) {
            lastCounterSkill.put(AttackStatus.PARRY, time);
        } else if (AttackStatus.getBaseStatus(status) == AttackStatus.BLOCK && PlayerClass
                .getStartingClassFor(getPlayerClass()) == PlayerClass.WARRIOR) {
            lastCounterSkill.put(AttackStatus.BLOCK, time);
        }
    }

    public long getLastCounterSkill(AttackStatus status) {
        if (lastCounterSkill.get(status) == null) {
            return 0;
        }

        return lastCounterSkill.get(status);
    }

    /**
     * @return the dualEffectValue
     */
    public int getDualEffectValue() {
        return dualEffectValue;
    }

    /**
     * @param dualEffectValue the dualEffectValue to set
     */
    public void setDualEffectValue(int dualEffectValue) {
        this.dualEffectValue = dualEffectValue;
    }

    /**
     * @return the Resurrection Positional State
     */
    public boolean isInResPostState() {
        return isInResurrectPosState;
    }

    /**
     * @param value Resurrection Positional State to set
     */
    public void setResPosState(boolean value) {
        isInResurrectPosState = value;
    }

    /**
     * @return the Resurrection Positional X value
     */
    public float getResPosX() {
        return resPosX;
    }

    /**
     * @param value Resurrection Positional X value to set
     */
    public void setResPosX(float value) {
        resPosX = value;
    }

    /**
     * @return the Resurrection Positional Y value
     */
    public float getResPosY() {
        return resPosY;
    }

    /**
     * @param value Resurrection Positional Y value to set
     */
    public void setResPosY(float value) {
        resPosY = value;
    }

    /**
     * @return the Resurrection Positional Z value
     */
    public float getResPosZ() {
        return resPosZ;
    }

    /**
     * @param value Resurrection Positional Z value to set
     */
    public void setResPosZ(float value) {
        resPosZ = value;
    }

    public boolean isInSiegeWorld() {
        switch (getWorldId()) {
            case 210050000:
            case 220070000:
            case 400010000:
            case 600030000:
                return true;
        }
        return false;
    }

    /**
     * @return true if player is under NoFly Effect
     */
    public boolean isUnderNoFly() {
        return getEffectController().isAbnormalSet(AbnormalState.NOFLY);
    }

    /**
     * @return true if player is under NoFpConsumEffect
     */
    public boolean isUnderNoFPConsum() {
        return underNoFPConsum;
    }

    /**
     * @param value status of NoFpConsum Effect
     */
    public void setUnderNoFPConsum(boolean value) {
        underNoFPConsum = value;
    }

    public void setInstanceStartPos(float instanceStartPosX, float instanceStartPosY, float instanceStartPosZ) {
        this.instanceStartPosX = instanceStartPosX;
        this.instanceStartPosY = instanceStartPosY;
        this.instanceStartPosZ = instanceStartPosZ;
    }

    public float getInstanceStartPosX() {
        return instanceStartPosX;
    }

    public float getInstanceStartPosY() {
        return instanceStartPosY;
    }

    public float getInstanceStartPosZ() {
        return instanceStartPosZ;
    }

    public boolean havePermission(byte perm) {
        return playerAccount.getMembership() >= perm;
    }

    /**
     * @return Returns the emotions.
     */
    public EmotionList getEmotions() {
        return emotions;
    }

    /**
     * @param emotions The emotions to set.
     */
    public void setEmotions(EmotionList emotions) {
        this.emotions = emotions;
    }

    public int getRebirthResurrectPercent() {
        return rebirthResurrectPercent;
    }

    public void setRebirthResurrectPercent(int rebirthResurrectPercent) {
        this.rebirthResurrectPercent = rebirthResurrectPercent;
    }

    public int getRebirthSkill() {
        return rebirthSkill;
    }

    public void setRebirthSkill(int rebirthSkill) {
        this.rebirthSkill = rebirthSkill;
    }

    public BindPointPosition getBindPoint() {
        return bindPoint;
    }

    public void setBindPoint(BindPointPosition bindPoint) {
        this.bindPoint = bindPoint;
    }

    @Override
    public ItemAttackType getAttackType() {
        Item weapon = getEquipment().getMainHandWeapon();
        if (weapon != null) {
            return weapon.getItemTemplate().getAttackType();
        }
        return ItemAttackType.PHYSICAL;
    }

    public long getFlyStartTime() {
        return flyStartTime;
    }

    public FlyPathEntry getCurrentFlyPath() {
        return flyLocationId;
    }

    public void setUnWispable() {
        isWispable = false;
    }

    public void setWispable() {
        isWispable = true;
    }

    public boolean isWispable() {
        return isWispable;
    }

    public boolean isInvulnerableWing() {
        return isUnderInvulnerableWing;
    }

    public void setInvulnerableWing(boolean value) {
        isUnderInvulnerableWing = value;
    }

    public void resetAbyssRankListUpdated() {
        abyssRankListUpdateMask = 0;
    }

    public void setAbyssRankListUpdated(AbyssRankUpdateType type) {
        abyssRankListUpdateMask |= type.value();
    }

    public boolean isAbyssRankListUpdated(AbyssRankUpdateType type) {
        return (abyssRankListUpdateMask & type.value()) == type.value();
    }

    public void addSalvationPoints(long points) {
        playerCommonData.addSalvationPoints(points);
        sendPck(new SM_STATS_INFO(this));
    }

    @Override
    public byte isPlayer() {
        if (isGM()) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * @return the motions
     */
    public MotionList getMotions() {
        return motions;
    }

    /**
     * @param motions the motions to set
     */
    public void setMotions(MotionList motions) {
        this.motions = motions;
    }

    public boolean isTransformed() {
        return getTransformModel().isActive();
    }

    public void setTransformed(boolean value) {
        getTransformModel().setActive(value);
    }

    /**
     * @return the npcFactions
     */
    public NpcFactions getNpcFactions() {
        return npcFactions;
    }

    /**
     * @param npcFactions the npcFactions to set
     */
    public void setNpcFactions(NpcFactions npcFactions) {
        this.npcFactions = npcFactions;
    }

    /**
     * @return the flyReuseTime
     */
    public long getFlyReuseTime() {
        return flyReuseTime;
    }

    /**
     * @param flyReuseTime the flyReuseTime to set
     */
    public void setFlyReuseTime(long flyReuseTime) {
        this.flyReuseTime = flyReuseTime;
    }

    /**
     * @param value flying mode flag to set
     */
    public void setFlyingMode(boolean value) {
        isFlying = value;
    }

    /**
     * @return true if player is in Flying mode
     */
    public boolean isInFlyingMode() {
        return isFlying;
    }

    /**
     * Stone Use Order determined by highest inventory slot. :( If player has
     * two types, wrong one might be used.
     *
     * @return selfRezItem
     */
    public Item getSelfRezStone() {
        Item item;
        item = getReviveStone(161001001);
        if (item == null) {
            item = getReviveStone(161000003);
        }
        if (item == null) {
            item = getReviveStone(161000004);
        }
        if (item == null) {
            item = getReviveStone(161000001);
        }
        return item;
    }

    /**
     * @return stoneItem or null
     */
    private Item getReviveStone(int stoneId) {
        Item item = getInventory().getFirstItemByItemId(stoneId);
        if (item != null && isItemUseDisabled(item.getItemTemplate().getUseLimits())) {
            item = null;
        }
        return item;
    }

    /**
     * Need to find how an item is determined as able to self-rez.
     *
     * @return boolean can self rez with item
     */
    public boolean haveSelfRezItem() {
        return (getSelfRezStone() != null);
    }

    /**
     * Rebirth Effect is id 160.
     */
    public boolean haveSelfRezEffect() {
        if (getAccessLevel() >= AdminConfig.ADMIN_AUTO_RES) {
            return true;
        }

        // Store the effect info.
        List<Effect> effects = getEffectController().getAbnormalEffects();
        for (Effect effect : effects) {
            for (EffectTemplate template : effect.getEffectTemplates()) {
                if (template.getEffectid() == 160 && (template instanceof RebirthEffect)) {
                    RebirthEffect rebirthEffect = (RebirthEffect) template;
                    setRebirthResurrectPercent(rebirthEffect.getResurrectPercent());
                    setRebirthSkill(rebirthEffect.getSkillId());
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasResurrectBase() {
        List<Effect> effects = getEffectController().getAbnormalEffects();
        for (Effect effect : effects) {
            for (EffectTemplate template : effect.getEffectTemplates()) {
                if (template.getEffectid() == 160 && (template instanceof ResurrectBaseEffect)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void unsetResPosState() {
        if (isInResPostState()) {
            setResPosState(false);
            setResPosX(0);
            setResPosY(0);
            setResPosZ(0);
        }
    }

    public LootGroupRules getLootGroupRules() {
        if (isInGroup2()) {
            return getPlayerGroup2().getLootGroupRules();
        }
        if (isInAlliance2()) {
            return getPlayerAlliance2().getLootGroupRules();
        }
        return null;
    }

    public boolean isLooting() {
        return lootingNpcOid != 0;
    }

    public int getLootingNpcOid() {
        return lootingNpcOid;
    }

    public void setLootingNpcOid(int lootingNpcOid) {
        this.lootingNpcOid = lootingNpcOid;
    }

    public final boolean isMentor() {
        return isMentor;
    }

    public final void setMentor(boolean isMentor) {
        this.isMentor = isMentor;
    }

    @Override
    public Race getRace() {
        return playerCommonData.getRace();
    }

    public Player findPartner() {
        return World.getInstance().findPlayer(partnerId);
    }

    public boolean isMarried() {
        return partnerId != 0;
    }

    public int getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(int partnerId) {
        this.partnerId = partnerId;
    }

    @Override
    public int getSkillCooldown(SkillTemplate template) {
        return isCoolDownZero() ? 0 : template.getCooldown();
    }

    @Override
    public int getItemCooldown(ItemTemplate template) {
        return isCoolDownZero() ? 0 : template.getUseLimits().getDelayTime();
    }

    public void setLastMessageTime() {
        if ((System.currentTimeMillis() - lastMsgTime) / 1000 < SecurityConfig.FLOOD_DELAY) {
            floodMsgCount++;
        } else {
            floodMsgCount = 0;
        }
        lastMsgTime = System.currentTimeMillis();
    }

    public int floodMsgCount() {
        return floodMsgCount;
    }

    public void setOnlineTime() {
        onlineTime = System.currentTimeMillis();
    }

    /**
     * @return online time in sec
     */
    public long getOnlineTime() {
        return (System.currentTimeMillis() - onlineTime) / 1000;
    }

    public void setRebirthRevive(boolean result) {
        rebirthRevive = result;
    }

    public boolean canUseRebirthRevive() {
        return rebirthRevive;
    }

    public void subtractSupplements(int count, int supplementId) {
        subtractedSupplementsCount = count;
        subtractedSupplementId = supplementId;
    }

    public void updateSupplements() {
        if ((subtractedSupplementId == 0) || (subtractedSupplementsCount == 0)) {
            return;
        }
        getInventory().decreaseByItemId(subtractedSupplementId, subtractedSupplementsCount);
        subtractedSupplementsCount = 0;
        subtractedSupplementId = 0;
    }

    public int getPortAnimation() {
        return portAnimation;
    }

    public void setPortAnimation(int portAnimation) {
        this.portAnimation = portAnimation;
    }

    @Override
    public boolean isSkillDisabled(SkillTemplate template) {
        ChainCondition cond = template.getChainCondition();
        if (cond != null && cond.getSelfCount() > 0) {
            int chainCount = getChainSkills().getChainCount(this, template, cond.getCategory());
            if (chainCount > 0 && chainCount < cond.getSelfCount() && getChainSkills()
                    .chainSkillEnabled(cond.getCategory(), cond.getTime())) {
                return false;
            }
        }
        return super.isSkillDisabled(template);
    }

    public boolean isInSprintMode() {
        return isInSprintMode;
    }

    public void setSprintMode(boolean isInSprintMode) {
        this.isInSprintMode = isInSprintMode;
    }

    public List<ActionObserver> getRideObservers() {
        return rideObservers;
    }

    public void setRideObservers(ActionObserver observer) {
        if (rideObservers == null) {
            rideObservers = new ArrayList<>(3);
        }
        rideObservers.add(observer);
    }

    public int getRawKillCount() {
        return rawKillcount;
    }

    public void setRawKillCount(int count) {
        rawKillcount = count;
    }

    public int getSpreeLevel() {
        return spreeLevel;
    }

    public void setSpreeLevel(int value) {
        spreeLevel = value;
    }

    public Storage[] getPetBags() {
        return this.petBag;
    }

    @Override
    public void flushSkillCd() {
        super.flushSkillCd();

        getChainSkills().flush();
        sendPck(new SM_SKILL_COOLDOWN(getSkillCoolDowns()));

        if (getSummon() != null) {
            getSummon().flushSkillCd();
            sendPck(new SM_SKILL_COOLDOWN(getSummon().getSkillCoolDowns()));
        }
    }

    @Override
    public void flushSkillCd(int... exceptions) {
        super.flushSkillCd(exceptions);

        getChainSkills().flush();
        sendPck(new SM_SKILL_COOLDOWN(getSkillCoolDowns()));

        if (getSummon() != null) {
            getSummon().flushSkillCd(exceptions);
            sendPck(new SM_SKILL_COOLDOWN(getSummon().getSkillCoolDowns()));
        }
    }

    public void sendPck(AionServerPacket pck) {
        AionConnection con = getClientConnection();
        if (con != null) {
            con.sendPacket(pck);
        }
    }

    public void sendMsg(String msg) {
        sendPck(new SM_MESSAGE(0, null, msg, ChatType.GOLDEN_YELLOW));
    }

    public void sendMsg(String msg, Object... args) {
        sendPck(new SM_MESSAGE(0, null, String.format(msg, args), ChatType.GOLDEN_YELLOW));
    }

    public void sendMsg(L10N.Translatable msg) {
        sendPck(new SM_MESSAGE(0, null, translate(msg), ChatType.GOLDEN_YELLOW));
    }

    public void sendMsg(L10N.Translatable msg, Object... args) {
        sendPck(new SM_MESSAGE(0, null, translate(msg, args), ChatType.GOLDEN_YELLOW));
    }

    public L10N.Lang getLang() {
        return _lang;
    }

    public void setLang(L10N.Lang lang) {
        _lang = lang;
    }

    public String translate(L10N.Translatable msg) {
        return L10N.translate(msg, _lang);
    }

    public String translate(L10N.Translatable msg, Object... args) {
        return String.format(L10N.translate(msg, _lang), args);
    }

    public void setRMLoc(boolean b) {
        this.rmloc = b;
    }

    public boolean isRMLoc() {
        return rmloc;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(float x, float y, float z) {
        Coordinates c = new Coordinates(x, y, z);
        this.coordinates = c;
    }

    public void setCoordinates(Coordinates c) {
        this.coordinates = c;
    }
    
    public Coordinates getSaveCoordinates() {
        return saveCoordinates;
    }

    public void setSaveCoordinates(float x, float y, float z) {
        Coordinates c = new Coordinates(x, y, z);
        this.saveCoordinates = c;
    }

    public void setSaveCoordinates(Coordinates c) {
        this.saveCoordinates = c;
    }
    
    private String customLegName = "";

    public void setLegName(String ln) {
        if (ln == null) {
            if (isGM()) {
                ln = AccessLevelEnum.getAlType(getAccessLevel()).getLegionName();
            } else {
                ln = getLegion() == null ? "" : getLegion().getLegionName();
            }
        }
        customLegName = ln;
    }

    public String getCustomLegionName() {
        return customLegName;
    }

    public void setNewName(String name) {
        if (name == null) {
            if (isGM()) {
                String tag = AccessLevelEnum.getAlType(getAccessLevel()).getTagForName();
                name = tag + getName() + tag;
            }
        }
        this.newname = name;
    }

    public String getNewName() {
        if (newname == null) {
            newname = getName();
        }
        return this.newname;
    }

    //nameFormat = "%s";
    public String getNameFormat() {
        return this.nameFormat;
    }

}
