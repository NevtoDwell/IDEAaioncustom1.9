/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.housing;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.FileReader;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import gnu.trove.map.hash.THashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.commons.Sys;
import com.ne.commons.annotations.NotNull;
import com.ne.commons.annotations.Nullable;
import com.ne.commons.database.SQL;
import com.ne.commons.database.TableRow;
import com.ne.commons.utils.Actor;
import com.ne.gs.configs.main.HousingConfig;
import com.ne.gs.controllers.NpcController;
import com.ne.gs.controllers.effect.EffectController;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.DescId;
import com.ne.gs.model.DialogPage;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.model.templates.item.actions.AbstractItemAction;
import com.ne.gs.model.templates.item.actions.DecorateAction;
import com.ne.gs.model.templates.item.actions.ItemActions;
import com.ne.gs.model.templates.item.actions.SummonHouseObjectAction;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.Packets;
import com.ne.gs.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.ne.gs.network.aion.serverpackets.SM_FRIEND_LIST;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.network.aion.serverpackets.SM_WAREHOUSE_INFO;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.services.instance.InstanceService;
import com.ne.gs.services.item.ItemPacketService;
import com.ne.gs.services.item.ItemService;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.utils.DelayedGameAction;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.idfactory.IDFactory;
import com.ne.gs.world.WorldMapInstance;
import com.ne.gs.world.WorldMapType;
import com.ne.gs.world.knownlist.PlayerAwareKnownList;
import com.ne.gs.world.knownlist.Visitor;

import static com.ne.gs.modules.housing.House.FlatTrigger;
import static com.ne.gs.modules.housing.House.State;
import static com.ne.gs.modules.housing.HouseAuction.HouseLotId;
import static com.ne.gs.modules.housing.Network.*;
import static com.ne.gs.modules.housing.Network.CpCustomizeHouse.Action;
import static com.ne.gs.network.aion.AionConnection.State.IN_GAME;

/**
 * @author hex1r0
 */
public class Housing extends Actor {

    private static final Logger _log = LoggerFactory.getLogger("housing");

    private static final Housing _housing = new Housing();
    private static final HouseAuction _auction = new HouseAuction();

    static Logger log() {
        return _log;
    }

    public static Housing housing() {
        return _housing;
    }

    public static HouseAuction auction() {
        return _auction;
    }

    public static void load() {
        try {
            Packets.addLoaderAndRun(new Runnable() {
                @Override
                public void run() {
                    Packets.regCP(CpGetLots.class, 0x167, IN_GAME);
                    Packets.regCP(CpChangeHouseSettings.class, 0x117, IN_GAME);
                    Packets.regCP(CpCustomizeHouse.class, 0x2E0, IN_GAME);
                    Packets.regCP(CpKickVisitors.class, 0x116, IN_GAME);
                    Packets.regCP(CpUseHouseDoor.class, 0x1AF, IN_GAME);
                    Packets.regCP(CpPlaceBid.class, 0x16A, IN_GAME);
                    Packets.regCP(CpPutForSale.class, 0x168, IN_GAME);
                    Packets.regCP(CpCustomizeAppearance.class, 0x119, IN_GAME);
                    Packets.regCP(CpUseHouseItem.class, 0x1AD, IN_GAME);
                    Packets.regCP(CpVisitHouse.class, 0x16B, IN_GAME);

                    Packets.regSP(SpHouseInfo.class, 0x10F);
                    Packets.regSP(SpHouseInfoUpdate.class, 0x3D);
                    Packets.regSP(SpDeleteHouse.class, 0x110);
                    Packets.regSP(SpHouseItemInfo.class, 0x10C);
                    Packets.regSP(SpDeleteHouseItem.class, 0x10D);
                    Packets.regSP(SpHouseInventory.class, 0x74);
                    Packets.regSP(SpHouseOwnerInfo.class, 0x107);
                    Packets.regSP(SpAuctionLotInfo.class, 0x100);
                    Packets.regSP(SpEditorReply.class, 0x52);
                    Packets.regSP(SpHouseAcquire.class, 0x113);
                    Packets.regSP(SpUseHouseItem.class, 0x108);
                }
            });

            // [1] load templates
            housing().loadTemplates();

            // [2] spawn all houses
            Spawner.spawnHouses();

            // [3] load data from GDB and apply to houses
            housing().loadDatabase();

            // [4] run auction
            HouseAuction.schedule(HouseAuction.State.START, 0);

            DEFAULT_WALL = HouseItem.of(0, ITEM_TEMPLATES.get(3524000));
            DEFAULT_FLOOR = HouseItem.of(0, ITEM_TEMPLATES.get(3525000));
        } catch (Exception e) {
            log().error("", e);
        }
    }

    private Housing() {
    }

    public static Race findHouseRace(House.HouseTemplate tpl) {
        switch (WorldMapType.of(tpl.getMapId())) {
            case PERNON:
            case GELKMAROS:
            case BELUSLAN:
            case FLAT_ASMOS:
                return Race.ASMODIANS;

            case ORIEL:
            case INGGISON:
            case HEIRON:
            case FLAT_ELYOS:
                return Race.ELYOS;

            default:
                log().warn("Map {} is not mapped to race!", tpl.getMapId());
        }

        return Race.NONE;
    }

    public static int findSignNpcId(House.HouseTemplate tpl, State state) {
        switch (findHouseRace(tpl)) {
            case ELYOS:
                switch (state) {
                    case INACTIVE:
                        return 810004;
                    case SELLING:
                        return 810005;
                    case WAITING:
                        return 810006;
                    case HOME:
                        return 810007;
                }
            case ASMODIANS:
                switch (state) {
                    case INACTIVE:
                        return 810027;
                    case SELLING:
                        return 810028;
                    case WAITING:
                        return 810029;
                    case HOME:
                        return 810030;
                }
        }

        return 0;
    }

    public static int findManagerId(House.HouseTemplate tpl) {
        switch (findHouseRace(tpl)) {
            case ELYOS:
                switch (tpl.getType()) {
                    case FLAT:
                        return 810021;
                    case HOUSE:
                        return 810020;
                    case MANSION:
                        return 810019;
                    case ESTATE:
                        return 810018;
                    case PALACE:
                        return 810017;
                }
            case ASMODIANS:
                switch (tpl.getType()) {
                    case FLAT:
                        return 810026;
                    case HOUSE:
                        return 810025;
                    case MANSION:
                        return 810024;
                    case ESTATE:
                        return 810023;
                    case PALACE:
                        return 810022;
                }
        }

        return 0;
    }

    public static int findPorlalId(House.HouseTemplate tpl) {
        switch (findHouseRace(tpl)) {
            case ELYOS:
                return 810003;
            case ASMODIANS:
                return 810031;
        }

        return 0;
    }

    private static boolean hasAccess(Player player, House house) {
        Integer ownerId = house.getOwnerUid();
        boolean allow = ownerId.equals(player.getObjectId()); // owner is allowed :)

        if (!allow) // allow for admins
        {
            allow = player.getAccessLevel() >= HousingConfig.ENTER_ACCESSLEVEL;
        }

        if (!allow) { // check access
            switch (house.getAccess()) {
                case PRIVATE:
                    allow = false;
                    break;
                case PUBLIC:
                    allow = true;
                    break;
                case PROTECTED:
                    allow =
                        player.getFriendList().getFriend(ownerId) != null
                            || (player.getLegion() != null && player.getLegion().isMember(ownerId));
                    break;
            }
        }

        return allow;
    }

    public static Iterable<Integer> getUsedIDs() {
        return Iterables
            .transform(SQL.select(HouseItemsTable.class).submit(),
                new Function<TableRow<HouseItemsTable>, Integer>() {
                    @Override
                    public Integer apply(TableRow<HouseItemsTable> row) {
                        return row.get(HouseItemsTable.item_uid);
                    }
                });
    }

    // ------------------------------------------------------------------------
    // -- registry
    // ------------------------------------------------------------------------

    static class HouseList implements Iterable<House> {

        private final Map<House.HouseTemplate, House> _houses = new THashMap<>(1);

        public void add(House house) {
            _houses.put(house.getTemplate(), house);
        }

        @Nullable
        public House remove(House.HouseTemplate tpl) {
            return _houses.remove(tpl);
        }

        @Override
        public Iterator<House> iterator() {
            return _houses.values().iterator();
        }

        public int size() {
            return _houses.size();
        }
    }

    static class ItemList {

        private static final Map<Integer, ItemList> _playerItems = new THashMap<>(0);

        private final Map<Integer, HouseItem> _items = new THashMap<>(0);

        public Iterable<HouseItem> getAppearance() {
            return Iterables.filter(_items.values(), new Predicate<HouseItem>() {
                @Override
                public boolean apply(HouseItem i) {
                    return i.isAppearance();
                }
            });
        }

        public Iterable<HouseItem> getInstallable() {
            return Iterables.filter(_items.values(), new Predicate<HouseItem>() {
                @Override
                public boolean apply(HouseItem i) {
                    return i.isInstallable() && !i.isInstalled();
                }
            });
        }

        public Iterable<HouseItem> getInstalled() {
            return Iterables.filter(_items.values(), new Predicate<HouseItem>() {
                @Override
                public boolean apply(HouseItem i) {
                    return i.isInstallable() && i.isInstalled();
                }
            });
        }

        public HouseItem get(Integer itemUid) {
            return _items.get(itemUid);
        }

        public int getInstalledCountOf(HouseItemTemplate tpl) {
            int i = 0;
            for (HouseItem houseItem : getInstalled()) {
                if (houseItem.getTemplate().equals(tpl)) {
                    i++;
                }
            }

            return i;
        }

        public void put(HouseItem item) {
            _items.put(item.getObjectId(), item);
        }

        public HouseItem remove(Integer itemUid) {
            return _items.remove(itemUid);
        }

        public static ItemList of(Integer playerUid) {
            ItemList items = _playerItems.get(playerUid);
            if (items == null) {
                items = new ItemList();
                _playerItems.put(playerUid, items);
            }

            return items;
        }
    }

    public static final Map<Integer, HouseItemTemplate> ITEM_TEMPLATES = new THashMap<>();
    public static final Map<Integer, House.HouseTemplate> HOUSE_TEMPLATES = new THashMap<>();

    private final Map<Integer, HouseList> _ownedHouseLists = new THashMap<>();
    private final Map<Integer, House> _housesByHouseId = new THashMap<>();
    private final Map<Integer, House.Trigger> _triggersByHouseId = new THashMap<>();

    void loadTemplates() {
        try {
            JAXBContext jc = JAXBContext.newInstance(HouseItemTemplate.List.class);
            Unmarshaller un = jc.createUnmarshaller();
            un.setSchema(null);

            HouseItemTemplate.List templateList = (HouseItemTemplate.List) un.unmarshal(
                new FileReader("./data/static_data/housing/house_items.xml"));

            for (HouseItemTemplate t : templateList.getTemplates()) {
                ITEM_TEMPLATES.put(t.id, t);
            }

            jc = JAXBContext.newInstance(House.RegionList.class);
            un = jc.createUnmarshaller();
            //un.setEventHandler(new XmlValidationHandler());
            un.setSchema(null);
            House.RegionList regionList = (House.RegionList) un.unmarshal(
                new FileReader("./data/static_data/housing/housing.xml"));

            for (House.RegionTemplate r : regionList.getRegionTemplates()) {
                for (House.HouseTemplate tpl : r.getHouseTemplates()) {
                    HOUSE_TEMPLATES.put(tpl.getHouseId(), tpl);
                }
            }

        } catch (Exception e) {
            log().error("", e);
        }
    }

    void loadDatabase() {
        for (TableRow<PlayerHouse> row : SQL.select(PlayerHouse.class).submit()) {
            Integer houseId = row.get(PlayerHouse.HOUSE_ID);
            House.HouseTemplate tpl = HOUSE_TEMPLATES.get(houseId);
            if (tpl == null) {
                log().warn("No template for id: " + houseId + " found!");
                continue;
            }

            Integer playerId = row.get(PlayerHouse.PLAYER_ID);
            PlayerCommonData pcd = PlayerCommonData.get(playerId);
            if (pcd == null) {
                log().warn("No player for id: " + playerId + " found!");

                // cleanup invalid records
                SQL.delete(PlayerHouse.class)
                    .where(PlayerHouse.PLAYER_ID, playerId)
                    .submit();

                log().warn("Cleanup: player " + playerId + " cleared from database!");
                continue;
            }

            invoke(new RestoreHouse(pcd, tpl, row));
        }

        for (TableRow<HouseItemsTable> row : SQL.select(HouseItemsTable.class).submit()) {
            Integer itemUid = row.get(HouseItemsTable.item_uid);
            Integer playerId = row.get(HouseItemsTable.player_id);
            Integer itemId = row.get(HouseItemsTable.item_id);

            House home = getHome(playerId);
            if (home == null) {
                log().warn("No home for player: " + playerId + " found!");

                // cleanup invalid records
                SQL.delete(HouseItemsTable.class)
                    .where(HouseItemsTable.item_uid, itemUid)
                    .submit();

                log().warn("Cleanup: item " + itemUid + " cleared from database!");
                continue;
            }

            HouseItemTemplate tpl = ITEM_TEMPLATES.get(itemId);
            if (tpl == null) {
                log().warn(String.format("No template for id: %d found", itemId));
                return;
            }

            Boolean installed = row.get(HouseItemsTable.installed);
            Short usages = row.get(HouseItemsTable.usages);
            Integer installedTime = row.get(HouseItemsTable.installedtime);
            Integer cooldown = row.get(HouseItemsTable.cooldown);

            HouseItem item = HouseItem.of(itemUid, tpl);
            item.setUsages(usages);
            item.setInstalledTime(installedTime);
            item.setCooldown(cooldown);

            if (installed) {
                Float x = row.get(HouseItemsTable.x);
                Float y = row.get(HouseItemsTable.y);
                Float z = row.get(HouseItemsTable.z);
                Short h = row.get(HouseItemsTable.h);

                item.install(home.getTemplate().getMapId(), 1, x, y, z, h); // TODO channel id for flats?
            }

            ItemList.of(playerId).put(item);
        }

        // TODO validate house states?
    }

    static void save(House house) {
        TableRow<PlayerHouse> row = TableRow.of(PlayerHouse.class);
        row.set(PlayerHouse.PLAYER_ID, house.getOwnerUid())
            .set(PlayerHouse.HOUSE_ID, house.getHouseId())
            .set(PlayerHouse.EXTERIOR_ID, house.getExteriorId())
            .set(PlayerHouse.STATE, house.getState().id())
            .set(PlayerHouse.ACCESS, house.getAccess().id())
            .set(PlayerHouse.SIGN_OPT, house.getSignOpt().id())
            .set(PlayerHouse.DOOR_FLAG, house.getDoorFlag().id());

        for (House.AppearanceType a : House.AppearanceType.values()) {
            row.set(PlayerHouse.valueOf(a.name()), house.getAppearance(a));
        }

        SQL.insertOrUpdate(PlayerHouse.class, row).submit();
    }

    static void delete(Integer ownerUid, House.HouseTemplate template) {
        SQL.delete(PlayerHouse.class)
            .where(PlayerHouse.PLAYER_ID, ownerUid)
            .and(PlayerHouse.HOUSE_ID, template.getHouseId())
            .submit();
    }

    @NotNull
    House getHouseByTpl(House.HouseTemplate tpl) {
        return _housesByHouseId.get(tpl.getHouseId());
    }

    @NotNull
    Map<Integer, House> getHousesByHouseId() {
        return _housesByHouseId;
    }

    @NotNull
    public Collection<House> getAllHouses() {
        return Collections.unmodifiableCollection(_housesByHouseId.values());
    }

    @NotNull
    House.Trigger getTrigger(House.HouseTemplate tpl) {
        return _triggersByHouseId.get(tpl.getHouseId());
    }

    @NotNull
    Map<Integer, House.Trigger> getTriggers() {
        return _triggersByHouseId;
    }

    @NotNull
    public HouseList getPlayerHouses(Integer ownerUid) {
        HouseList houses = _ownedHouseLists.get(ownerUid);
        if (houses == null) {
            houses = new HouseList();
            _ownedHouseLists.put(ownerUid, houses);
        }

        return houses;
    }

    @Nullable
    House getHome(Integer ownerUid) {
        for (House h : getPlayerHouses(ownerUid)) {
            if (h.isHome()) {
                return h;
            }
        }

        return null;
    }

    @Nullable
    House getFlat(Integer ownerUid) {
        for (House h : housing().getPlayerHouses(ownerUid)) {
            if (h.getTemplate().getType() == House.HouseType.FLAT) {
                return h;
            }
        }

        return null;
    }

    void addPlayerHouse(Integer ownerUid, House house) {
        house.setOwnerUid(ownerUid);
        HouseInfo.update(ownerUid, house);
        getPlayerHouses(ownerUid).add(house);
    }

    @Nullable
    House removePlayerHouse(Integer playerId, House.HouseTemplate tpl) {
        House house = getPlayerHouses(playerId).remove(tpl);
        if (house != null) {
            house.setFree();
            for (HouseItem item : ItemList.of(playerId).getInstalled()) {
                item.recycle();
                item.store(playerId);
            }

            house.setState(State.INACTIVE);

            delete(playerId, tpl);
            return house;
        }

        return null;
    }

    // ------------------------------------------------------------------------
    // -- SQL Tables
    // ------------------------------------------------------------------------

    @com.ne.commons.database.SqlTable(name = "player_houses")
    static enum PlayerHouse implements com.ne.commons.database.TableColumn {
        PLAYER_ID("player_id", Integer.class),
        HOUSE_ID("house_id", Integer.class),
        EXTERIOR_ID("exterior_id", Integer.class),
        STATE("state", Byte.class),
        ACCESS("access", Byte.class),
        SIGN_OPT("sign_opt", Byte.class),
        DOOR_FLAG("door_flag", Byte.class),
        ROOF("roof", Integer.class),
        OUTWALL("outwall", Integer.class),
        FRAME("frame", Integer.class),
        DOOR("door", Integer.class),
        GARDEN("garden", Integer.class),
        FENCE("fence", Integer.class),
        INWALL_1("inwall_1", Integer.class),
        INWALL_2("inwall_2", Integer.class),
        INWALL_3("inwall_3", Integer.class),
        INWALL_4("inwall_4", Integer.class),
        INWALL_5("inwall_5", Integer.class),
        INWALL_6("inwall_6", Integer.class),
        FLOOR_1("floor_1", Integer.class),
        FLOOR_2("floor_2", Integer.class),
        FLOOR_3("floor_3", Integer.class),
        FLOOR_4("floor_4", Integer.class),
        FLOOR_5("floor_5", Integer.class),
        FLOOR_6("floor_6", Integer.class),
        CHIMNEY("chimney", Integer.class);

        private final String _name;
        private final Class<?> _type;

        private PlayerHouse(String name, Class<?> type) {
            _name = name;
            _type = type;
        }

        @Override
        public String getName() {
            return _name;
        }

        @Override
        public Class<?> getType() {
            return _type;
        }
    }

    // ------------------------------------------------------------------------
    // -- actions
    // ------------------------------------------------------------------------

    private static final String SUCCESS = "Successfully %s house %s for %s";
    private static final String ERROR = "Unable to %s house %s for %s. Reason: %s";

    static final class RestoreHouse implements Runnable {

        private final PlayerCommonData _pcd;
        private final House.HouseTemplate _template;
        private final TableRow<PlayerHouse> _row;

        RestoreHouse(@NotNull PlayerCommonData pcd,
                     @NotNull House.HouseTemplate template,
                     @NotNull TableRow<PlayerHouse> row) {
            _pcd = pcd;
            _template = template;
            _row = row;
        }

        @Override
        public void run() {
            Integer ownerUid = _pcd.getPlayerObjId();

            HouseList houses = housing().getPlayerHouses(ownerUid);
            if (houses.size() >= 2) {
                log().warn(String.format(ERROR, "restore", _template, _pcd.getName(), "too many houses"));
                return;
            }

            House house = null;
            if (_template.getType() != House.HouseType.FLAT) {
                house = housing().getHouseByTpl(_template);
                if (!house.isFree()) {
                    log().warn(String.format(ERROR, "restore", _template, _pcd.getName(), "dublicate house"));
                    return;
                }
            }

            if (house == null) // flat
            {
                house = new House(_template);
            }

            housing().addPlayerHouse(ownerUid, house);

            // apply properties to house
            house.setExteriorId((Integer) _row.get(PlayerHouse.EXTERIOR_ID));
            house.setState(State.of((Byte) _row.get(PlayerHouse.STATE)));
            house.setAccess(House.Access.of((Byte) _row.get(PlayerHouse.ACCESS)));
            house.setSignOpt(House.SignOpt.of((Byte) _row.get(PlayerHouse.SIGN_OPT)));
            house.setDoorFlag(House.DoorFlag.of((Byte) _row.get(PlayerHouse.DOOR_FLAG)));

            for (House.AppearanceType a : House.AppearanceType.values()) {
                Integer v = _row.get(PlayerHouse.valueOf(a.name()));
                house.setAppearance(a, v);
            }

            HouseInfo.update(ownerUid, house);

            broadcast(_pcd.getPlayer(), _template);

            log().debug(String.format(SUCCESS, "restored", _template, _pcd.getName()));
        }
    }

    public static void giveFlat(Player player) {
        House.HouseTemplate tpl = null;
        switch (player.getRace()) {
            case ELYOS:
                tpl = HOUSE_TEMPLATES.get(2001);
                break;
            case ASMODIANS:
                tpl = HOUSE_TEMPLATES.get(3001);
                break;
        }

        if (tpl != null) {
            housing().tell(new GiveHouse(player.getCommonData(), tpl));
        } else {
            log().warn("Unable to find flat template for race: " + player.getRace());
        }
    }

    public static final class GiveHouse implements Runnable {

        private final PlayerCommonData _pcd;
        private final House.HouseTemplate _tpl;

        public GiveHouse(@NotNull PlayerCommonData pcd, @NotNull House.HouseTemplate tpl) {
            _pcd = pcd;
            _tpl = tpl;
        }

        @Override
        public void run() {
            if (_pcd.getRace() != findHouseRace(_tpl)) {
                return; // player race == house race
            }

            Integer ownerUid = _pcd.getPlayerObjId();

            HouseList houses = housing().getPlayerHouses(ownerUid);
            if (houses.size() >= 2) {
                log().warn(String.format(ERROR, "give", _tpl, _pcd.getName(), "too many houses"));
                return;
            }

            House newHouse = null;
            if (_tpl.getType() != House.HouseType.FLAT) {
                newHouse = housing().getHouseByTpl(_tpl);
                if (!newHouse.isFree()) {
                    log().warn(String.format(ERROR, "give", _tpl, _pcd.getName(), "dublicate house"));
                    return;
                }
            }

            if (newHouse == null) // flat
            {
                newHouse = new House(_tpl);
            }

            // make sure house is not in auction
            HouseAuction.HouseLot lot = auction().removeLot(HouseLotId.of(_tpl));
            if (lot != null) {
                HouseAuction.deleteLot(lot);
            }

            housing().addPlayerHouse(ownerUid, newHouse);

            // get previous home
            House home = housing().getHome(ownerUid);

            // make this house home if player has no homes
            if (home == null) {
                newHouse.setState(State.HOME);
                save(newHouse);
            } else if (home.getTemplate().getType() == House.HouseType.FLAT) { // remove flat if it exists
                House.HouseTemplate tpl = home.getTemplate();

                housing().removePlayerHouse(ownerUid, tpl);
                log().info(String.format("Removed flat %s from %s", tpl.getName(), _pcd.getName()));

                newHouse.setState(State.HOME);
                save(newHouse);
            } else {
                // TODO set up home auction sell timer instead of deleting
                for (House house : ImmutableList.copyOf(houses)) {
                    if (!house.equals(newHouse)) {
                        House.HouseTemplate tpl = home.getTemplate();
                        housing().removePlayerHouse(ownerUid, tpl);
                        log().info(String.format("Removed house %s from %s", tpl.getName(), _pcd.getName()));
                    }
                }

                newHouse.setState(State.HOME);
                save(newHouse);
            }

            Player player = _pcd.getPlayer();

            broadcast(player, _tpl);

            if (player != null) {
                player.sendPck(new SpHouseAcquire(player.getObjectId(), _tpl.getHouseId(), true));
                if (_tpl.getType() == House.HouseType.FLAT) {
                    player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_INS_OWN_SUCCESS);
                }
            }

            log().info(String.format(SUCCESS, "gave", _tpl, _pcd.getName()));
        }
    }

    public final static class TakeHouse implements Runnable {

        private final PlayerCommonData _pcd;
        private final House.HouseTemplate _template;

        public TakeHouse(@NotNull PlayerCommonData pcd, @NotNull House.HouseTemplate template) {
            _pcd = pcd;
            _template = template;
        }

        @Override
        public void run() {
            Integer ownerUid = _pcd.getPlayerObjId();

            if (_template.getType() != House.HouseType.FLAT) {
                House house = housing().getHouseByTpl(_template);
                if (!house.getOwnerUid().equals(ownerUid)) // player is not owner of this house
                {
                    return;
                }
            }

            HouseInfo.update(ownerUid, null);
            housing().removePlayerHouse(ownerUid, _template);

            delete(ownerUid, _template);

            // delete items
            SQL.delete(HouseItemsTable.class)
                .where(HouseItemsTable.player_id, ownerUid)
                .submit();

            broadcast(_pcd.getPlayer(), _template);
        }
    }

    public static final class PlayerEnterWorld implements Runnable {

        private final Player _player;

        public PlayerEnterWorld(@NotNull Player player) {
            _player = player;
        }

        @Override
        public void run() {
            House home = housing().getHome(_player.getObjectId());
            if (home != null) {
                sendOwnerInfo(_player, home);
            }
        }
    }

    static final class SendHouseInfo implements Runnable {

        private final Player _player;
        private final House.HouseTemplate _template;

        SendHouseInfo(@NotNull Player player, @NotNull House.HouseTemplate template) {
            _player = player;
            _template = template;
        }

        @Override
        public void run() {
            House house = housing().getHouseByTpl(_template);
            Network.sendHouseInfo(_player, house);
        }
    }

    static final class DeleteHouse implements Runnable {

        private final Player _player;
        private final House.HouseTemplate _template;

        DeleteHouse(@NotNull Player player, @NotNull House.HouseTemplate template) {
            _player = player;
            _template = template;
        }

        @Override
        public void run() {
            House house = housing().getHouseByTpl(_template);
            _player.sendPck(new SpDeleteHouse(_template.getHouseId()));
            for (HouseItem i : ItemList.of(house.getOwnerUid()).getInstalled()) {
                _player.sendPck(new SpDeleteHouseItem(i.getObjectId()));
            }
        }
    }

    private static House findFlat(Integer ownerId) {
        for (House house : housing().getPlayerHouses(ownerId)) {
            if (house.getTemplate().getType() == House.HouseType.FLAT) {
                return house;
            }
        }
        return null;
    }

    public static final class SendFlatInfo implements Runnable {

        private final Player _player;
        private final Integer _ownerUid;

        public SendFlatInfo(@NotNull Player player, @NotNull Integer ownerUid) {
            _player = player;
            _ownerUid = ownerUid;
        }

        @Override
        public void run() {
            House h = findFlat(_ownerUid);
            if (h != null) {
                Network.sendHouseInfo(_player, h);
            }
        }
    }

    //    static final class UpdatePlayerHousesInfo implements Runnable {
    //        private final Player _player;
    //
    //        UpdatePlayerHousesInfo(@NotNull final Player player) { _player = player; }
    //
    //        @Override
    //        public void run() {
    //            for (final House house : housing().getPlayerHouses(_player.getObjectId()))
    //                Network.sendHouseInfo(_player, house);
    //        }
    //    }

    static final class ChangeHouseSettings implements Runnable {

        private final Player _player;
        private final int _access;
        private final int _signOpt;
        private final String _signText;

        ChangeHouseSettings(@NotNull Player player, int access, int signOpt, @NotNull String signText) {
            _player = player;
            _access = access;
            _signOpt = signOpt;
            _signText = signText;
        }

        @Override
        public void run() {
            Integer ownerId = _player.getObjectId();
            House home = housing().getHome(ownerId);
            if (home == null) {
                return;
            }

            House.Access access = House.Access.of(_access);

            home.setAccess(access);
            home.setSignOpt(House.SignOpt.of(_signOpt));
            home.setSignText(_signText);

            save(home);

            HouseInfo.update(_player.getObjectId(), home);

            switch (access) {
                case PUBLIC:
                    send(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_ORDER_OPEN_DOOR);
                    break;
                case PROTECTED:
                    send(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_ORDER_CLOSE_DOOR_WITHOUT_FRIENDS);
                    break;
                case PRIVATE:
                    send(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_ORDER_CLOSE_DOOR_ALL);
                    break;
            }

            broadcast(_player, home.getTemplate());
        }

        private void send(AionServerPacket pck) {
            _player.sendPck(pck);
        }
    }

    static final class CustomizeHouse implements Runnable {

        private final Player _player;
        private final Action _action;
        private final int _id;
        private final float _x, _y, _z;
        private final short _h;

        public CustomizeHouse(@NotNull Player player, @NotNull Action action, int id, float x, float y, float z, short h) {
            _player = player;
            _action = action;
            _id = id;
            _x = x;
            _y = y;
            _z = z;
            _h = h;
        }

        @Override
        public void run() {
            Integer playerUid = _player.getObjectId();
            House home = housing().getHome(playerUid);
            if (home == null) {
                return;
            }

            switch (_action) {
                case OPEN_EDITOR:
                    _player.sendPck(new SpEditorReply(new EditorAction(_action)));
                    sendHouseInventory(_player, home);
                    return;

                case INSTALL_ITEM:
                case MOVE_ITEM: {
                    ItemList houseItemList = ItemList.of(playerUid);
                    HouseItem item = houseItemList.get(_id);
                    if (item == null || !item.isInstallable()) {
                        return;
                    }

                    HouseItemTemplate tpl = item.getTemplate();
                    // check installation count
                    // TODO implement count for different houses??
                    //                    if (item.isUsable()) {
                    //                        if (_action == Action.INSTALL_ITEM) {
                    //                            if (houseItemList.getInstalledCountOf(tpl) >= tpl.installations) {
                    //                                _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_ACHIEVE_USE_COUNT);
                    //                                _player.sendPck(new SpEditorReply(new EditorAction(Action.INSTALL_ITEM)));
                    //                                sendHouseInventory(_player, home);
                    //                                return;
                    //                            }
                    //                        }
                    //                    }

                    item.install(_player.getWorldId(), _player.getInstanceId(), _x, _y, _z + 0.01f, _h);

                    item.store(home.getOwnerUid());

                    //PacketSendUtility.sendPacket(_player, new SpEditorReply(ItemInfo.of(home, item)));
                    PacketSendUtility.broadcastPacket(_player, new SpHouseItemInfo(ItemInfo.of(home, item)), true);
                    //PacketSendUtility.sendPacket(_player, new SpEditorReply(new RefreshItem(1, item.getObjectId())));

                    QuestEngine.getInstance().onHouseItemUseEvent(
                        new QuestEnv(null, _player, 0, 0), item.getItemId());
                    return;
                }

                case RECYCLE_ITEM: {
                    HouseItem item = ItemList.of(playerUid).get(_id);
                    if (item == null || !item.isInstallable()) {
                        return;
                    }

                    item.recycle();
                    item.store(home.getOwnerUid());

                    _player.sendPck(new SpDeleteHouseItem(item.getObjectId()));
                    sendHouseInventory(_player, home);
                    //                        PacketSendUtility.sendPacket(_player, new SpEditorReply(
                    //                            Blob.of(Action.RECYCLE.getId(), item.getObjectId()))
                    //                        );
                    return;
                }

                case CLOSE_EDITOR:
                case OPEN_EXTERIOR_MENU:
                case CLOSE_EXTERIOR_MENU:
                    _player.sendPck(new SpEditorReply(new EditorAction(_action)));
                    return;

                case CHANGE_EXTERIOR:
                    if (home.setExteriorId(_id)) {
                        save(home);
                        broadcast(_player, home.getTemplate());
                    }
                    return;

                case REGISTER_ITEM: {
                    Item item = _player.getInventory().getItemByObjId(_id);
                    if (item == null) {
                        return;
                    }

                    item = _player.getInventory().delete(item, ItemPacketService.ItemDeleteType.REGISTER);
                    if (item == null) {
                        return;
                    }

                    Integer itemId = findItemId(item);
                    HouseItemTemplate tpl = ITEM_TEMPLATES.get(itemId);
                    if (tpl == null) {
                        log().warn(String.format("No template for id: %d found", itemId));
                        return;
                    }

                    HouseItem hi = HouseItem.of(IDFactory.getInstance().nextId(), tpl);
                    ItemList.of(playerUid).put(hi);

                    hi.store(_player.getObjectId());

                    sendHouseInventory(_player, home);
                }

                case REMOVE: {
                    HouseItem item = ItemList.of(playerUid).remove(_id);
                    if (item != null) {
                        item.delete(playerUid);
                    }
                }
            }
        }

        static Integer findItemId(Item item) {
            ItemActions itemActions = item.getItemTemplate().getActions();
            if (itemActions == null) {
                return 0;
            }

            AbstractItemAction action = itemActions.getDecorateAction();
            if (action != null) {
                return ((DecorateAction) action).getTemplateId();
            }

            action = itemActions.getHouseObjectAction();
            if (action != null) {
                return ((SummonHouseObjectAction) action).getTemplateId();
            }

            return 0;
        }
    }

    public static HouseItem DEFAULT_WALL;
    public static HouseItem DEFAULT_FLOOR;

    public static class CustomizeAppearance implements Runnable {

        private final Player _player;
        private final int _itemUid;
        private final int _itemId;
        private final int _typeId;

        public CustomizeAppearance(Player player, int itemUid, int itemId, int typeId) {
            _player = player;
            _itemUid = itemUid;
            _itemId = itemId;
            _typeId = typeId;
        }

        @Override
        public void run() {
            Integer playerUid = _player.getObjectId();
            House home = housing().getHome(playerUid);
            if (home == null) {
                return;
            }

            House.AppearanceType type = House.AppearanceType.of(_typeId);
            if (type == null) {
                return;
            }

            if (_itemUid == 0) {
                switch (type) {
                    case INWALL_1:
                    case INWALL_2:
                    case INWALL_3:
                    case INWALL_4:
                    case INWALL_5:
                    case INWALL_6:
                        home.setAppearance(type, DEFAULT_WALL.getItemId());
                        break;
                    case FLOOR_1:
                    case FLOOR_2:
                    case FLOOR_3:
                    case FLOOR_4:
                    case FLOOR_5:
                    case FLOOR_6:
                        home.setAppearance(type, DEFAULT_FLOOR.getItemId());
                        break;
                }
            } else {
                HouseItem item = ItemList.of(playerUid).remove(_itemUid);
                if (item == null || !item.isAppearance()) {
                    return;
                }

                // TODO add validation of _typeId
                home.setAppearance(type, item.getItemId());

                SQL.delete(HouseItemsTable.class)
                    .where(HouseItemsTable.item_uid, item.getObjectId())
                    .submit();
            }

            save(home);

            PacketSendUtility.broadcastPacket(_player, new SpHouseInfo(home), true);

            QuestEngine.getInstance().onHouseItemUseEvent(new QuestEnv(null, _player, 0, 0), _itemId);
        }
    }

    static void tryTeleportToHouse(Player player, House house) {
        House.HouseTemplate tpl = house.getTemplate();
        int channelId, mapId = tpl.getMapId();
        float x, y, z;

        if (house.getTemplate().getType() == House.HouseType.FLAT) {
            Integer uid;
            if (house.getOwnerUid().equals(player.getObjectId()))  // player is teleporting to his own flat
            {
                uid = player.getObjectId();
            } else  // another player (e.g. friend) is teleporting to flat
            {
                uid = house.getOwnerUid();
            }

            WorldMapInstance channel = InstanceService.getPersonalInstance(mapId, uid);
            if (channel == null) {
                channel = InstanceService.getNextAvailableInstance(mapId, uid);
            }

            if (!channel.isRegistered(player.getObjectId())) {
                InstanceService.registerPlayerWithInstance(channel, player);
            }

            channelId = channel.getInstanceId();
            x = tpl.getX();
            y = tpl.getY();
            z = tpl.getZ();

            TeleportService.teleportBeam(player, mapId, channelId, x, y, z);
        } else {
            if (!hasAccess(player, house)) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_ENTER_NO_RIGHT2);
                return;
            }

            x = (tpl.getManagerX() + tpl.getPortalX()) / 2;
            y = (tpl.getManagerY() + tpl.getPortalY()) / 2;
            z = (tpl.getManagerZ() + tpl.getPortalZ()) / 2;

            TeleportService.teleportBeam(player, mapId, x, y, z);
        }
    }

    public static class UseDoor implements Runnable {

        private final Player _player;
        private final int _doorId;
        private final boolean _leave;

        public UseDoor(@NotNull Player player, int doorId, boolean leave) {
            _player = player;
            _doorId = doorId;
            _leave = leave;
        }

        @Override
        public void run() {
            House house = Iterables.tryFind(housing().getAllHouses(),
                new Predicate<House>() {
                    @Override
                    public boolean apply(House h) {
                        return h.getTemplate().getDoorId() == _doorId && _player.getRace() == findHouseRace(h.getTemplate());
                    }
                }
            ).orNull();

            if (house == null) {
                return;
            }

            House.HouseTemplate tpl = house.getTemplate();
            float x, y, z;

            if (_leave) { // leave house
                x = tpl.getSignX();
                y = tpl.getSignY();
                z = tpl.getSignZ();
                TeleportService.teleportBeam(_player, tpl.getMapId(), x, y, z);
            } else  // enter house
            {
                tryTeleportToHouse(_player, house);
            }
        }
    }

    public static class EnterLeaveFlat implements Runnable {

        private final Player _player;
        private final boolean _leave;

        public EnterLeaveFlat(@NotNull Player player, boolean leave) {
            _player = player;
            _leave = leave;
        }

        @Override
        public void run() {
            Integer playerId = _player.getObjectId();

            int mapId = 0;
            float x = 0, y = 0, z = 0;

            if (_leave) {
                if (WorldMapType.FLAT_ELYOS.equals(_player.getWorldId())) {
                    mapId = WorldMapType.ORIEL.getId();
                    x = 2573.0f;
                    y = 1961.0f;
                    z = 185.0f;
                } else if (WorldMapType.FLAT_ASMOS.equals(_player.getWorldId())) {
                    mapId = WorldMapType.PERNON.getId();
                    x = 1195.52f;
                    y = 2775.27f;
                    z = 236.37f;
                }

                TeleportService.teleportBeam(_player, mapId, x, y, z);
            } else {
                House flat = housing().getFlat(playerId);

                if (flat == null) {
                    _player.sendPck(SM_SYSTEM_MESSAGE.STR_HOUSING_ENTER_NEED_HOUSE);
                    return;
                }

                tryTeleportToHouse(_player, flat);
            }
        }
    }

    public static final class SpawnFlatTrigger implements Runnable {

        private final int _channelOwnerId;
        private final int _channelId;

        public SpawnFlatTrigger(int channelOwnerId, int channelId) {
            _channelOwnerId = channelOwnerId;
            _channelId = channelId;
        }

        @Override
        public void run() {
            House flat = findFlat(_channelOwnerId);
            if (flat == null) {
                return;
            }

            House.HouseTemplate t = flat.getTemplate();
            FlatTrigger tr = FlatTrigger.create(t, _channelOwnerId);

            SpawnEngine.bringIntoWorld(tr, t.getMapId(), _channelId, t.getX(), t.getY(), t.getZ(), (byte) 0);
            spawn(flat, findManagerId(t), t.getMapId(), t.getManagerX(), t.getManagerY(), t.getManagerZ(), t.getManagerH());
            spawn(flat, findPorlalId(t), t.getMapId(), t.getPortalX(), t.getPortalY(), t.getPortalZ(), t.getPortalH());
        }

        private Npc spawn(House flat, int npcId, int mapId, float x,
                          float y, float z, byte h) {
            SpawnTemplate stpl = SpawnEngine.addNewSingleTimeSpawn(mapId, npcId, x, y, z, h);

            NpcTemplate ntpl = DataManager.NPC_DATA.getNpcTemplate(npcId);
            if (ntpl == null) {
                throw new NullPointerException();
            }

            House.FlatNpc s = new House.FlatNpc(IDFactory.getInstance().nextId(), new NpcController(), stpl, ntpl);

            s.setKnownlist(new PlayerAwareKnownList(s));
            s.setEffectController(new EffectController(s));
            s.setCreatorId(flat.getHouseId());
            s.setMasterName("");
            s.flatUid = flat.getUid();

            SpawnEngine.bringIntoWorld(s, stpl, _channelId);

            return s;
        }
    }

    public static class QuerryRelationshipCrystal implements Runnable {

        private final Player _player;
        private final Npc _crystal;

        public QuerryRelationshipCrystal(@NotNull Player player, @NotNull Npc crystal) {
            _player = player;
            _crystal = crystal;
        }

        @Override
        public void run() {
            House house = _crystal instanceof House.FlatNpc ? findFlat(((House.FlatNpc) _crystal).flatUid) : findHouse(_crystal.getCreatorId());
            if (house == null) {
                return;
            }

            boolean allow = hasAccess(_player, house);

            if (!allow) {
                _player.sendPck(SM_SYSTEM_MESSAGE.STR_HOUSING_TELEPORT_CANT_USE);
                return;
            }

            _player.sendPck(new SM_DIALOG_WINDOW(_crystal.getObjectId(), DialogPage.HOUSING_FRIENDLIST.id()));
            _player.sendPck(new SM_FRIEND_LIST());
        }

        private House findFlat(int houseUid) {
            for (HouseList houses : housing()._ownedHouseLists.values()) {
                for (House house : houses) {
                    if (house.getUid().equals(houseUid)) {
                        return house;
                    }
                }
            }

            return null;
        }

        private House findHouse(int houseUid) {
            return housing()._housesByHouseId.get(houseUid);
        }
    }

    public static class VisitHouse implements Runnable {

        private final Player _requester;
        private final int _targetUid;
        private final int _actionId;

        public VisitHouse(Player requester, int targetUid, int actionId) {
            _requester = requester;
            _targetUid = targetUid;
            _actionId = actionId;
        }

        @Override
        public void run() {
            switch (_actionId) {
                case CpVisitHouse.TO_HOME: {
                    House home = housing().getHome(_requester.getObjectId());
                    if (home != null) {
                        tryTeleportToHouse(_requester, home);
                    }
                }
                break;
                case CpVisitHouse.VISIT_FRIEND: {
                    if (_requester.getFriendList().getFriend(_targetUid) != null) {
                        House home = housing().getHome(_targetUid);
                        if (home != null) {
                            tryTeleportToHouse(_requester, home);
                        }
                    }
                }
                break;
            }
        }
    }

    public static class UseGate implements Runnable {

        private final Integer _houseOwnerUid;
        private final Player _player;

        public UseGate(@NotNull Integer houseOwnerUid, @NotNull Player player) {
            _houseOwnerUid = houseOwnerUid;
            _player = player;
        }

        @Override
        public void run() {
            House home = housing().getHome(_houseOwnerUid);
            if (home == null) {
                return;
            }

            tryTeleportToHouse(_player, home);
        }
    }

    public static class UseInstalledItem implements Runnable {

        private final Player _player;
        private final int _itemUid;

        public UseInstalledItem(@NotNull Player player, int itemUid) {
            _player = player;
            _itemUid = itemUid;
        }

        @Override
        public void run() {
            final Integer playerUid = _player.getObjectId();
            House home = housing().getHome(playerUid);
            if (home == null) {
                _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_IS_ONLY_FOR_OWNER_VALID);
                return;
            }

            ItemList houseItemList = ItemList.of(playerUid);
            final HouseItem item = houseItemList.get(_itemUid);
            if (item == null || !item.isInstallable()) {
                return;
            }

            final HouseItemTemplate tpl = item.getTemplate();

            if (item.isMailbox()) {
                _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_USE(tpl.dscId));
                _player.getMailbox().sendMailList(false);
            } else if (item.isStorage()) {
                _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_USE(tpl.dscId));
                _player.sendPck(new SM_WAREHOUSE_INFO(_player.getStorage(tpl.storageId).getItemsWithKinah(), tpl.storageId, 0, true, _player));
                _player.sendPck(new SM_WAREHOUSE_INFO(null, tpl.storageId, 0, false, _player));
            }

            if (item.isUsable()) {
                if (checkInUse(item) &&
                    checkCooldown(item) &&
                    checkExpiration(item) &&
                    checkUsage(item) &&
                    checkFullInv() &&
                    checkRequirements(item)) {
                    new DelayedGameAction(_player, tpl.warmup) {
                        @Override
                        protected void preRun() {
                            _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_USE(tpl.dscId));
                            item.setInUse(true);
                        }

                        @Override
                        protected void postRun() {
                            housing().tell(new GiveReward(item, playerUid));
                        }
                    }.invoke();
                }
            } else {
                _player.sendPck(new SpUseHouseItem(new SpUseHouseItem.Info((byte) tpl.type.getId(), playerUid, _itemUid)));
            }
        }

        private boolean checkRequirements(HouseItem item) {
            HouseItemTemplate t = item.getTemplate();
            if (item.isFinalUsage() && t.hasFinalReward()) {
                return true;
            }

            if (t.isConsumingItem()) {
                if (_player.getInventory().getItemCountByItemId(t.consumeItemId) < t.consumeCount) {
                    int descId = DataManager.ITEM_DATA.getItemTemplate(t.consumeItemId).getNameId();
                    _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_CANT_USE_HOUSE_OBJECT_ITEM_CHECK(DescId.of(descId)));
                    return false;
                }
            }

            return true;
        }

        private boolean checkFullInv() {
            if (_player.getInventory().isFull()) {
                _player.sendPck(SM_SYSTEM_MESSAGE.STR_WAREHOUSE_TOO_MANY_ITEMS_INVENTORY);
                return false;
            }
            return true;
        }

        public boolean checkInUse(HouseItem item) {
            if (item.isInUse()) {
                _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_OCCUPIED_BY_OTHER);
                return false;
            }

            return true;
        }


        public boolean checkCooldown(HouseItem item) {
            if (!item.isReady()) {
                if (item.getTemplate().isDaily()) {
                    _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_CANT_USE_PER_DAY);
                } else {
                    _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANNOT_USE_FLOWERPOT_COOLTIME);
                }
                return false;
            }

            return true;
        }

        public boolean checkExpiration(HouseItem item) {
            if (item.isExpired()) {
                _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANNOT_USE_FLOWERPOT_COOLTIME);
                return false;
            }

            return true;
        }

        public boolean checkUsage(HouseItem item) {
            // unexpected bahaviour, e.g. through database
            if (item.getTemplate().isUseLimited() && item.getUsages() > item.getTemplate().usages) {
                _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_ACHIEVE_USE_COUNT);
                return false;
            }

            if (item.getTemplate().isUseLimited() && item.getUsages() == item.getTemplate().usages) {
                if (item.getTemplate().hasFinalReward()) {
                    return true;
                }

                _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_ACHIEVE_USE_COUNT);
                return false;
            }

            return true;
        }

        private class GiveReward implements Runnable {

            private final HouseItem _item;
            private final HouseItemTemplate _tpl;
            private final Integer _playerUid;

            public GiveReward(HouseItem item, Integer playerUid) {
                _item = item;
                _tpl = item.getTemplate();
                _playerUid = playerUid;
            }

            @Override
            public void run() {
                if (_tpl.hasReward() && checkFullInv()) {
                    if (_item.isFinalUsage() && _tpl.hasFinalReward()) {
                        //                        int count = 1;
                        //                        if (reward.finalRewardCountMax != 0)
                        //                            count = Rnd.get(reward.finalRewardCountMin, reward.finalRewardCountMax);

                        ItemService.addItem(_player, _tpl.finalRewardId, 1);
                    } else {
                        if (_tpl.isConsumingItem() && !removeRequired()) {
                            return;
                        }

                        if (_tpl.isEquipRequired() && !checkEquipped()) {
                            return;
                        }

                        if (_tpl.hasReward()) {
                            ItemService.addItem(_player, _tpl.rewardId, 1);
                        }
                    }
                }

                if (_item.isFinalUsage()) {
                    ItemList.of(_player.getObjectId()).remove(_item.getObjectId());
                    _item.recycle();
                    _item.delete(_player.getObjectId());
                    _player.sendPck(new SpDeleteHouseItem(_item.getObjectId()));
                } else {
                    _item.setUsages((short) (_item.getUsages() + 1));
                    if (_tpl.isDaily()) {
                        Calendar c = Calendar.getInstance();
                        c.add(Calendar.DAY_OF_YEAR, 1);
                        c.set(Calendar.HOUR_OF_DAY, 0);
                        c.set(Calendar.MINUTE, 0);
                        c.set(Calendar.SECOND, 0);
                        c.set(Calendar.MILLISECOND, 0);

                        _item.setCooldown((int) (c.getTimeInMillis() / 1000));
                    } else {
                        _item.setCooldown(Sys.seconds() + _tpl.cooldown);
                    }
                    _item.store(_player.getObjectId());
                    _item.setInUse(false);

                    byte checkType = 1; // FIXME
                    _player.sendPck(new SpUseHouseItem(new SpUseHouseItem.ExtInfo((byte) _tpl.type.getId(), _playerUid, _playerUid, _itemUid, _item.getUsages(), checkType)));
                }
            }

            private boolean removeRequired() {
                if (!checkRequirements(_item)) {
                    return false;
                }

                _player.getInventory().decreaseByItemId(_tpl.consumeItemId, _tpl.consumeCount);
                return true;
            }

            boolean checkEquipped() {
                List<Item> items = _player.getEquipment().getEquippedItemsByItemId(_tpl.equippedItemId);
                if (items.size() == 0) {
                    int descId = DataManager.ITEM_DATA.getItemTemplate(_tpl.equippedItemId).getNameId();
                    _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_CANT_USE_HOUSE_OBJECT_ITEM_EQUIP(DescId.of(descId)));
                    return false;
                }

                return true;
            }
        }
    }

    static class KickVisitors implements Runnable {

        private final Player _player;
        private final CpKickVisitors.Mode _mode;

        public KickVisitors(Player player, CpKickVisitors.Mode mode) {
            _player = player;
            _mode = mode;
        }

        @Override
        public void run() {
            switch (_mode) {
                case ALL_BUT_FRIENDS:
                    _player.getKnownList().doOnAllPlayers(new Visitor<Player>() {
                        @Override
                        public void visit(Player p) {
                            if (_player.getFriendList().getFriend(p.getObjectId()) != null) {
                                return;
                            }

                            teleport(p);
                        }
                    });
                    _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_ORDER_OUT_WITHOUT_FRIENDS);
                    break;
                case ALL:
                    _player.getKnownList().doOnAllPlayers(new Visitor<Player>() {
                        @Override
                        public void visit(Player p) {
                            teleport(p);
                        }
                    });
                    _player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_ORDER_OUT_ALL);
                    break;
            }
        }

        void teleport(Player p) {
            House home = housing().getHome(p.getObjectId());
            if (home == null) {
                home = housing().getFlat(p.getObjectId());
            }

            if (home != null) {
                tryTeleportToHouse(p, home);
            }
        }
    }
}
