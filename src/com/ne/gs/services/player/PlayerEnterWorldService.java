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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ne.gs.database.GDB;
import com.ne.gs.services.custom.CustomQuestsService;
import javolution.util.FastList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.utils.EventNotifier;
import com.ne.commons.utils.L10N;
import com.ne.gs.cache.HTMLCache;
import com.ne.gs.configs.administration.AdminConfig;
import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.configs.main.EventsConfig;
import com.ne.gs.configs.main.GSConfig;
import com.ne.gs.configs.main.HTMLConfig;
import com.ne.gs.configs.main.PeriodicSaveConfig;
import com.ne.gs.configs.main.RateConfig;
import com.ne.gs.configs.main.SecurityConfig;
import com.ne.gs.database.dao.PlayerDAO;
import com.ne.gs.database.dao.PlayerPasskeyDAO;
import com.ne.gs.database.dao.PlayerPunishmentsDAO;
import com.ne.gs.database.dao.WeddingDAO;
import com.ne.gs.model.ChatType;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.account.Account;
import com.ne.gs.model.account.CharacterBanInfo;
import com.ne.gs.model.account.CharacterPasskey.ConnectType;
import com.ne.gs.model.account.PlayerAccountData;
import com.ne.gs.model.events.PlayerEnteredGame;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.player.FriendList;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.model.gameobjects.player.emotion.Emotion;
import com.ne.gs.model.gameobjects.player.motion.Motion;
import com.ne.gs.model.gameobjects.player.title.Title;
import com.ne.gs.model.gameobjects.state.CreatureSeeState;
import com.ne.gs.model.gameobjects.state.CreatureVisualState;
import com.ne.gs.model.items.storage.IStorage;
import com.ne.gs.model.items.storage.Storage;
import com.ne.gs.model.items.storage.StorageType;
import com.ne.gs.model.team2.alliance.PlayerAllianceService;
import com.ne.gs.model.team2.group.PlayerGroupService;
import com.ne.gs.modules.anniversary.Anniversary;
import com.ne.gs.modules.housing.House;
import com.ne.gs.modules.housing.HouseInfo;
import com.ne.gs.modules.housing.Housing;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.serverpackets.*;
import com.ne.gs.network.loginserver.LoginServer;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;
import com.ne.gs.services.*;
import com.ne.gs.services.PunishmentService.PunishmentType;
import com.ne.gs.services.abyss.AbyssRankingCache;
import com.ne.gs.services.abyss.AbyssSkillService;
import com.ne.gs.services.custom.ChatServerLogService;
import com.ne.gs.services.instance.InstanceService;
import com.ne.gs.services.mail.MailService;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.services.toypet.PetService;
import com.ne.gs.skillengine.effect.AbnormalState;
import com.ne.gs.taskmanager.tasks.ExpireTimerTask;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.utils.audit.AuditLogger;
import com.ne.gs.utils.audit.GMService;
import com.ne.gs.utils.rates.Rates;
import com.ne.gs.world.World;
import com.ne.gs.world.WorldMapType;

/**
 * @author ATracer
 */
public final class PlayerEnterWorldService {

    private static final Logger log = LoggerFactory.getLogger("GAMECONNECTION_LOG");
    private static final String serverInfo;
    private static final Set<Integer> pendingEnterWorld = new HashSet<>();

    static {
        String infoBuffer = "\n"+"Добро пожаловать на сервер " + GSConfig.SERVER_NAME + " !\n";

        infoBuffer = infoBuffer + "====================================";

        serverInfo = infoBuffer;
    }

    /**
     * @param objectId
     * @param client
     */
    public static void startEnterWorld(int objectId, AionConnection client) {
        // check if char is banned
        PlayerAccountData playerAccData = client.getAccount().getPlayerAccountData(objectId);
        if (playerAccData == null) {
            log.error("FIXME playerAccData == null");
            return;
        }

        PlayerCommonData pcd = playerAccData.getPlayerCommonData();
        if (pcd == null) {
            log.error("FIXME pcd == null");
            return;
        }

        Timestamp lastOnline = pcd.getLastOnline();
        if (lastOnline != null && client.getAccount().getAccessLevel() < AdminConfig.GM_LEVEL) {
            if (System.currentTimeMillis() - lastOnline.getTime() < GSConfig.CHARACTER_REENTRY_TIME * 1000) {
                client.sendPacket(new SM_ENTER_WORLD_CHECK((byte) 6)); // 20 sec
                return;
            }
        }
        CharacterBanInfo cbi = client.getAccount().getPlayerAccountData(objectId).getCharBanInfo();
        if (cbi != null) {
            if (cbi.getEnd() > System.currentTimeMillis() / 1000) {
                client.close(new SM_QUIT_RESPONSE(), false);
                return;
            } else {
                GDB.get(PlayerPunishmentsDAO.class).unpunishPlayer(objectId, PunishmentType.CHARBAN);
            }
        }
        // passkey check
        if (SecurityConfig.PASSKEY_ENABLE && !client.getAccount().getCharacterPasskey().isPass()) {
            showPasskey(objectId, client);
        } else {
            validateAndEnterWorld(objectId, client);
        }
    }

    /**
     * @param objectId
     * @param client
     */
    private static void showPasskey(int objectId, AionConnection client) {
        client.getAccount().getCharacterPasskey().setConnectType(ConnectType.ENTER);
        client.getAccount().getCharacterPasskey().setObjectId(objectId);
        boolean isExistPasskey = GDB.get(PlayerPasskeyDAO.class)
                                           .existCheckPlayerPasskey(client.getAccount().getId());

        if (!isExistPasskey) {
            client.sendPacket(new SM_CHARACTER_SELECT(0));
        } else {
            client.sendPacket(new SM_CHARACTER_SELECT(1));
        }
    }

    /**
     * @param objectId
     * @param client
     */
    private static void validateAndEnterWorld(final int objectId, final AionConnection client) {
        synchronized (pendingEnterWorld) {
            if (pendingEnterWorld.contains(objectId)) {
                log.warn("Skipping enter world " + objectId);
                return;
            }
            pendingEnterWorld.add(objectId);
        }
        int delay = 0;
        // double checked enter world
        if (World.getInstance().findPlayer(objectId) != null) {
            delay = 15000;
            log.warn("Postponed enter world " + objectId);
        }
        ThreadPoolManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                try {
                    Player player = World.getInstance().findPlayer(objectId);
                    if (player != null) {
                        AuditLogger.info(player, "Duplicate player in world");
                        client.close(new SM_QUIT_RESPONSE(), false);
                        return;
                    }
                    enterWorld(client, objectId);
                } catch (Throwable ex) {
                    log.error("Error during enter world " + objectId, ex);
                } finally {
                    synchronized (pendingEnterWorld) {
                        pendingEnterWorld.remove(objectId);
                    }
                }
            }

        }, delay);
    }

    /**
     * @param client
     * @param objectId
     */
    public static void enterWorld(AionConnection client, int objectId) {
        Account account = client.getAccount();
        PlayerAccountData playerAccData = account.getPlayerAccountData(objectId);

        if (playerAccData == null) {
            // Somebody wanted to login on character that is not at his account
            return;
        }
        final Player player = PlayerService.getPlayer(objectId, account);

        if (player != null && client.setActivePlayer(player)) {
            player.setClientConnection(client);

            // account vip / premium has expired
            if (account.getMembership() > 0 && account.getExpire() > 0 && account.getExpire() <= System.currentTimeMillis()) {
                log.info(String.format("[PREMIUM/VIP] Account %s expired!", account.getName()));
                LoginServer.getInstance()
                           .sendLsControlPacket(account.getName(), player.getName(), "Premium/VIP Service", 0, 2);
                account.setMembership((byte) 0);
                account.setExpire(0);
            }

            log.info("[MAC_AUDIT] Player " + player.getName() + " (account " + account.getName() + ") has entered world with " + client
                .getMacAddress() + " MAC.");
            World.getInstance().storeObject(player);

            StigmaService.onPlayerLogin(player);

            /**
             * Energy of Repose must be calculated before sending SM_STATS_INFO
             */
            if (playerAccData.getPlayerCommonData().getLastOnline() != null) {
                long lastOnline = playerAccData.getPlayerCommonData().getLastOnline().getTime();
                PlayerCommonData pcd = player.getCommonData();
                long secondsOffline = System.currentTimeMillis() / 1000 - lastOnline / 1000;

                if (pcd.isReadyForSalvationPoints()) {
                    // 10 mins offline = 0 salvation points.
                    if (secondsOffline > 10 * 60) {
                        player.getCommonData().resetSalvationPoints();
                    }
                }
                if (pcd.isReadyForReposteEnergy()) {
                    pcd.updateMaxReposte();
                    // more than 4 hours offline = start counting Reposte Energy
                    // addition.
                    if (secondsOffline > 4 * 3600) {
                        double hours = secondsOffline / 3600d;
                        long maxRespose = player.getCommonData().getMaxReposteEnergy();
                        if (hours > 24) {
                            hours = 24;
                        }
                        // 24 hours offline = 100% Reposte Energy
                        long addResposeEnergy = (long) (hours / 24 * maxRespose);
                        HouseInfo info = HouseInfo.of(player);
                        if (info.mapIs(player.getWorldId())) {
                            if (info.typeIs(House.HouseType.FLAT)) {
                                addResposeEnergy = (long) (addResposeEnergy * 1.05F);
                            } else {
                                addResposeEnergy = (long) (addResposeEnergy * 1.1F);
                            }
                        }
                        pcd.addReposteEnergy(addResposeEnergy);
                    }
                }

                if (secondsOffline > CustomConfig.DP_RESET_TIME) {
                    player.getLifeStats().setCurrentDp(0);
                }

            }
            InstanceService.onPlayerLogin(player);
            client.sendPacket(new SM_SKILL_LIST(player));
            AbyssSkillService.onEnterWorld(player);
            AccessGmSkills.onEnterWorldGm(player);


            //client.sendPacket(new SM_SKILL_COOLDOWN(player.getSkillCoolDowns()));
            //client.sendPacket(new SM_ITEM_COOLDOWN(player.getItemCoolDowns()));

            FastList<QuestState> questList = FastList.newInstance();
            FastList<QuestState> completeQuestList = FastList.newInstance();
            for (QuestState qs : player.getQuestStateList().getAllQuestState()) {
                if (qs.getStatus() == QuestStatus.NONE) {
                    continue;
                }
                if (qs.getStatus() == QuestStatus.COMPLETE) {
                    completeQuestList.add(qs);
                } else {
                    questList.add(qs);
                }
            }
            client.sendPacket(new SM_QUEST_COMPLETED_LIST(completeQuestList));
            client.sendPacket(new SM_QUEST_LIST(questList));
            client.sendPacket(new SM_TITLE_INFO(player.getCommonData().getTitleId()));
            client.sendPacket(new SM_MOTION(player.getMotions().getMotions().values()));
            client.sendPacket(new SM_ENTER_WORLD_CHECK());

            byte[] uiSettings = player.getPlayerSettings().getUiSettings();
            byte[] shortcuts = player.getPlayerSettings().getShortcuts();

            if (uiSettings != null) {
                client.sendPacket(new SM_UI_SETTINGS(uiSettings, 0));
            }

            if (shortcuts != null) {
                client.sendPacket(new SM_UI_SETTINGS(shortcuts, 1));
            }

            sendItemInfos(client, player);
            playerLoggedIn(player);

            player.setNewName(null);
            player.setLegName(null);

            client.sendPacket(new SM_INSTANCE_INFO(player, false, player.getCurrentTeam()));

            client.sendPacket(new SM_CHANNEL_INFO(player.getPosition()));

            KiskService.getInstance().onLogin(player);
            TeleportService.sendSetBindPoint(player);

            World.getInstance().preSpawn(player);
            SiegeService.getInstance().validateLoginZone(player);

            client.sendPacket(new SM_PLAYER_SPAWN(player));

            client.sendPacket(new SM_GAME_TIME());
            if (player.isLegionMember()) {
                LegionService.getInstance().onLogin(player);
            }
            client.sendPacket(new SM_TITLE_INFO(player));

            client.sendPacket(new SM_EMOTION_LIST((byte) 0, player.getEmotions().getEmotions()));

            SiegeService.getInstance().onPlayerLogin(player);

            //            TODO: Send Rift Announce Here
            client.sendPacket(new SM_PRICES());
            client.sendPacket(new SM_ABYSS_RANK(player.getAbyssRank()));

            client.sendPacket(new SM_SKILL_COOLDOWN(player.getSkillCoolDowns()));
            client.sendPacket(new SM_ITEM_COOLDOWN(player.getItemCoolDowns()));

            //Intro message
            PacketSendUtility.sendBrightYellowMessage(player, serverInfo);

            player.setRates(Rates.getRatesFor(account.getMembership()));
            if (CustomConfig.PREMIUM_NOTIFY) {
                showPremiumAccountInfo(player);
            }

            if (player.isGM()) {
                if (AdminConfig.INVULNERABLE_GM_CONNECTION
                    || AdminConfig.INVISIBLE_GM_CONNECTION                
                    || AdminConfig.VISION_GM_CONNECTION
                    || AdminConfig.WHISPER_GM_CONNECTION) {
                    player.sendMsg("=============================");
                    if (AdminConfig.INVULNERABLE_GM_CONNECTION) {
                        player.setInvul(true);
                        player.sendMsg(">> Неуязвимость : Включено <<");
                    }
                    if (AdminConfig.INVISIBLE_GM_CONNECTION) {
                        player.getEffectController().setAbnormal(AbnormalState.HIDE.getId());
                        player.setVisualState(CreatureVisualState.HIDE10);
                        PacketSendUtility.broadcastPacket(player, new SM_PLAYER_STATE(player), true);
                        player.sendMsg(">> Невидимость : Включено <<");
                    }                 
                    if (AdminConfig.VISION_GM_CONNECTION) {
                        player.setSeeState(CreatureSeeState.SEARCH10);
                        PacketSendUtility.broadcastPacket(player, new SM_PLAYER_STATE(player), true);
                        player.sendMsg(">> Вы видите невидимые цели : Включено <<");
                    }
                    if (AdminConfig.WHISPER_GM_CONNECTION) {
                        player.setUnWispable();
                        player.sendMsg(">> Личные сообщения : Отключено <<");
                    }
                    player.sendMsg("=============================");
                }

                ChatServerLogService.getInstance().evtLoggedIn(player);
            }

            // Alliance Packet after SetBindPoint
            PlayerAllianceService.onPlayerLogin(player);
            PacketSendUtility.sendBrightYellowMessageOnCenter(player, AccessLevelEnum.getAlType(player.getAccessLevel()).getNotice(player.getName()) + "!");

            if (player.isInPrison()) {
                PunishmentService.updatePrisonStatus(player);
            } else {
                if (player.getWorldId() == WorldMapType.DF_PRISON.getId()
                    || player.getWorldId() == WorldMapType.DE_PRISON.getId()) {
                    player.sendMsg("Вы будете перемещены к кибелиску в течении 1ой минуты!");
                    ThreadPoolManager.getInstance().schedule(new Runnable() {

                        @Override
                        public void run() {
                            TeleportService.moveToBindLocation(player, true);
                        }
                    }, 60000);
                }
            }

            if (player.isGag()) {
                PunishmentService.updateGagStatus(player);
            }

            if (player.isNotGatherable()) {
                PunishmentService.updateGatherableStatus(player);
            }

            PlayerGroupService.onPlayerLogin(player);
            PetService.getInstance().onPlayerLogin(player);
            MailService.getInstance().onPlayerLogin(player);
            BrokerService.getInstance().onPlayerLogin(player);
            sendMacroList(client, player);
            client.sendPacket(new SM_ONLINE_STATUS((byte) 1));
            SM_RECIPE_LIST.sendTo(player);
            PetitionService.getInstance().onPlayerLogin(player);
            AutoGroupService2.getInstance().onPlayerLogin(player);
            ClassChangeService.showClassChangeDialog(player);

            GMService.getInstance().onPlayerLogin(player);
            /**
             * Trigger restore services on login.
             */
            player.getLifeStats().updateCurrentStats();

            if (HTMLConfig.ENABLE_HTML_WELCOME) {
                HTMLService.showHTML(player, HTMLCache.getInstance().getHTML("welcome.xhtml"));
            }

            player.getNpcFactions().sendDailyQuest();

            if (HTMLConfig.ENABLE_GUIDES) {
                HTMLService.onPlayerLogin(player);
            }

            for (StorageType st : StorageType.values()) {
                if (st == StorageType.LEGION_WAREHOUSE) {
                    continue;
                }
                IStorage storage = player.getStorage(st.getId());
                if (storage != null) {
                    for (Item item : storage.getItemsWithKinah()) {
                        if (item.getExpireTime() > 0) {
                            ExpireTimerTask.getInstance().addTask(item, player);
                        }
                    }
                }
            }

            for (Item item : player.getEquipment().getEquippedItems()) {
                if (item.getExpireTime() > 0) {
                    ExpireTimerTask.getInstance().addTask(item, player);
                }
            }
            player.getEquipment().checkRankLimitItems();

            for (Motion motion : player.getMotions().getMotions().values()) {
                if (motion.getExpireTime() != 0) {
                    ExpireTimerTask.getInstance().addTask(motion, player);
                }
            }

            for (Emotion emotion : player.getEmotions().getEmotions()) {
                if (emotion.getExpireTime() != 0) {
                    ExpireTimerTask.getInstance().addTask(emotion, player);
                }
            }

            for (Title title : player.getTitleList().getTitles()) {
                if (title.getExpireTime() != 0) {
                    ExpireTimerTask.getInstance().addTask(title, player);
                }
            }

            player.getController().addTask(
                TaskId.PLAYER_UPDATE,
                ThreadPoolManager.getInstance().scheduleAtFixedRate(
                    new GeneralUpdateTask(player.getObjectId()),
                    PeriodicSaveConfig.PLAYER_GENERAL * 1000,
                    PeriodicSaveConfig.PLAYER_GENERAL * 1000));

            player.getController().addTask(
                TaskId.INVENTORY_UPDATE,
                ThreadPoolManager.getInstance().scheduleAtFixedRate(
                    new ItemUpdateTask(player.getObjectId()),
                    PeriodicSaveConfig.PLAYER_ITEMS * 1000,
                    PeriodicSaveConfig.PLAYER_ITEMS * 1000));

            SurveyService.getInstance().showAvailable(player);

            if (EventsConfig.ENABLE_EVENT_SERVICE) {
                EventService.getInstance().onPlayerLogin(player);
            }

            player.setPartnerId(GDB.get(WeddingDAO.class).loadPartnerId(player));

            EventNotifier.GLOBAL.fire(PlayerEnteredGame.class, player);

            // TODO move to EventNotifier.GLOBAL
            // --
            Housing.housing().tell(new Housing.PlayerEnterWorld(player));
            Anniversary.getInstance().tell(new Anniversary.PlayerEnterWorld(player));
            AbyssRankingCache.getInstance().onPlayerEnterWorld(player);
            InstanceService.onPlayerLogin(player);
            CustomQuestsService.getInstance().onPlayerLogin(player);
            // --

        } else {
            log.info("[DEBUG] enter world" + objectId + ", Player: " + player);
        }
    }

    /**
     * @param client
     * @param player
     */
    // TODO! this method code is really odd [Nemesiss]
    private static void sendItemInfos(AionConnection client, Player player) {
        // Cubesize limit set in inventory.
        int questExpands = player.getQuestExpands();
        int npcExpands = player.getNpcExpands();
        player.getInventory().setLimit(StorageType.CUBE.getLimit() + (questExpands + npcExpands) * 9);
        player.getWarehouse().setLimit(StorageType.REGULAR_WAREHOUSE.getLimit() + player.getWarehouseSize() * 8);

        // items
        Storage inventory = player.getInventory();
        List<Item> equipedItems = player.getEquipment().getEquippedItems();
        if (equipedItems.size() != 0) {
            client.sendPacket(new SM_INVENTORY_INFO(
                player.getEquipment().getEquippedItems(), npcExpands, questExpands, player));
        }

        List<Item> unequipedItems = inventory.getItemsWithKinah();
        int itemsSize = unequipedItems.size();
        if (itemsSize != 0) {
            int index = 0;
            while (index + 10 < itemsSize) {
                client.sendPacket(new SM_INVENTORY_INFO(unequipedItems.subList(index, index + 10), npcExpands, questExpands, player));
                index += 10;
            }
            client.sendPacket(new SM_INVENTORY_INFO(unequipedItems.subList(index, itemsSize), npcExpands, questExpands, player));
        }
        client.sendPacket(new SM_INVENTORY_INFO());
        client.sendPacket(new SM_STATS_INFO(player));
    }

    private static void sendMacroList(AionConnection client, Player player) {
        client.sendPacket(new SM_MACRO_LIST(player, false));
        if (player.getMacroList().getSize() > 7) {
            client.sendPacket(new SM_MACRO_LIST(player, true));
        }
    }

    /**
     * @param player
     */
    private static void playerLoggedIn(Player player) {
        log.info(String.format("Player logged in: %s Account: %s",
            player.getName(),
            player.getClientConnection().getAccount().getName()));
        player.getCommonData().setOnline(true);
        GDB.get(PlayerDAO.class).onlinePlayer(player, true);
        player.getFriendList().setStatus(FriendList.Status.ONLINE);
        player.setOnlineTime();
    }

    private static void showPremiumAccountInfo(Player player) {
        AionConnection client = player.getClientConnection();
        Account account = player.getPlayerAccount();

        byte membership = account.getMembership();
        if (membership < 3) {
            String accountType = "";
            float EXP = RateConfig.XP_RATE;
            float Drop = RateConfig.DROP_RATE;
            float QuestEXP = RateConfig.QUEST_XP_RATE;
            float QuestKinah = RateConfig.QUEST_KINAH_RATE;
            float GatheringExp = RateConfig.GATHERING_XP_RATE;
            float CraftExp = RateConfig.CRAFTING_XP_RATE;
            float PvPAp = RateConfig.AP_PLAYER_GAIN_RATE;

            switch (account.getMembership()) {
                case 0:
                    accountType = player.translate(Messages.REGULAR);
                    break;
                case 1:
                    accountType = player.translate(Messages.PREMIUM);
                    EXP = RateConfig.PREMIUM_XP_RATE;
                    Drop = RateConfig.PREMIUM_DROP_RATE;
                    QuestEXP = RateConfig.PREMIUM_QUEST_XP_RATE;
                    QuestKinah = RateConfig.PREMIUM_QUEST_KINAH_RATE;
                    GatheringExp = RateConfig.PREMIUM_GATHERING_XP_RATE;
                    CraftExp = RateConfig.PREMIUM_CRAFTING_XP_RATE;
                    PvPAp = RateConfig.PREMIUM_AP_PLAYER_GAIN_RATE;
                    break;
                case 2:
                    accountType = player.translate(Messages.VIP);
                    EXP = RateConfig.VIP_XP_RATE;
                    Drop = RateConfig.VIP_DROP_RATE;
                    QuestEXP = RateConfig.VIP_QUEST_XP_RATE;
                    QuestKinah = RateConfig.VIP_QUEST_KINAH_RATE;
                    GatheringExp = RateConfig.VIP_GATHERING_XP_RATE;
                    CraftExp = RateConfig.VIP_CRAFTING_XP_RATE;
                    PvPAp = RateConfig.VIP_AP_PLAYER_GAIN_RATE;
                    break;
            }
            client.sendPacket(new SM_MESSAGE(0, null, String
                .format(player.translate(Messages.SUMMARY),
                    accountType,
                    EXP,
                    Drop,
                    QuestEXP,
                    QuestKinah,
                    GatheringExp,
                    CraftExp,
                    PvPAp)
                , ChatType.GOLDEN_YELLOW));
        }
    }

    public static enum Messages implements L10N.Translatable {
        REGULAR("regular"),
        PREMIUM("premium"),
        VIP("VIP"),
        SUMMARY(" ---------------\n" +
            " Your account is %s\n" +
            " EXP: %s\n" +
            " Drop: %s\n" +
            " Quest EXP: %s\n" +
            " Quest Kinah: %s\n" +
            " Gathering Exp: %s\n" +
            " Craft Exp: %s\n" +
            " PvP Ap: %s\n" +
            " ---------------:");

        private final String _defaultValue;

        private Messages(String defaultValue) {
            _defaultValue = defaultValue;
        }

        @Override
        public String id() {
            return toString();
        }

        @Override
        public String defaultValue() {
            return _defaultValue;
        }
    }
}
