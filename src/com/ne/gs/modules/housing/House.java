/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.housing;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.annotations.Nullable;
import com.ne.gs.controllers.NpcController;
import com.ne.gs.controllers.VisibleObjectController;
import com.ne.gs.model.gameobjects.AionObject;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.npc.NpcTemplate;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.utils.idfactory.IDFactory;
import com.ne.gs.world.World;
import com.ne.gs.world.WorldPosition;
import com.ne.gs.world.knownlist.PlayerAwareKnownList;

/**
 * @author hex1r0
 */
public class House extends AionObject {

    private static final Integer FREE = 0;
    private static final AtomicInteger idFactory = new AtomicInteger(1);

    private final HouseTemplate _template;
    private Integer _ownerUid;

    private State _state = State.INACTIVE;
    private Access _access = Access.PRIVATE;
    private SignOpt _signOpt = SignOpt.SHOW;
    private String _signText = "";
    private DoorFlag _doorFlag = DoorFlag.NONE;
    private final int[] _appearance = new int[AppearanceType.values().length];
    private int _exteriorId;

    public House(@NotNull HouseTemplate template) {
        this(FREE, template);
    }

    public House(@NotNull Integer ownerUid, @NotNull HouseTemplate template) {
        super(idFactory.getAndIncrement());
        _ownerUid = ownerUid;
        _template = template;

        _exteriorId = template.getExteriorTemplates().get(0).getId();
    }

    public Integer getHouseId() {
        return _template.getHouseId();
    }

    @Override
    @NotNull
    public String getName() {
        return _template.getName();
    }

    @NotNull
    public Integer getUid() {
        return getObjectId();
    }

    @NotNull
    public Integer getOwnerUid() {
        return _ownerUid;
    }

    public void setOwnerUid(@NotNull Integer ownerUid) {
        _ownerUid = ownerUid;
    }

    @NotNull
    public State getState() {
        return _state;
    }

    public void setState(@NotNull State state) {
        _state = state;

        if (getTemplate().getType() == House.HouseType.FLAT) {
            return;
        }

        Spawner.spawnSign(getTemplate(), state);

        if (!isFree()) {
            Spawner.spawnManager(getTemplate());
            Spawner.spawnPortal(getTemplate());
        } else {
            Spawner.despawnManager(getTemplate());
            Spawner.despawnPortal(getTemplate());
        }
    }

    public boolean isHome() {
        return getState() == State.HOME;
    }

    @NotNull
    public Access getAccess() {
        return _access;
    }

    public void setAccess(@NotNull Access access) {
        _access = access;
    }

    @NotNull
    public SignOpt getSignOpt() {
        return _signOpt;
    }

    public void setSignOpt(@NotNull SignOpt signOpt) {
        _signOpt = signOpt;
    }

    @NotNull
    public String getSignText() {
        return _signText;
    }

    public void setSignText(@NotNull String signText) {
        _signText = signText;
    }

    @NotNull
    public DoorFlag getDoorFlag() {
        return _doorFlag;
    }

    public void setDoorFlag(@NotNull DoorFlag doorFlag) {
        _doorFlag = doorFlag;
    }

    public int[] getAppearance() {
        return _appearance;
    }

    public int getAppearance(@NotNull AppearanceType appearanceType) {
        return _appearance[appearanceType.ordinal()];
    }

    public void setAppearance(@NotNull AppearanceType appearanceType, int value) {
        _appearance[appearanceType.ordinal()] = value;
    }

    @NotNull
    public HouseTemplate getTemplate() {
        return _template;
    }

    public boolean isFree() {
        return _ownerUid.equals(FREE);
    }

    public void setFree() {
        _ownerUid = FREE;
    }

    public int getExteriorId() {
        return _exteriorId;
    }

    public boolean setExteriorId(int exteriorId) {
        // validate exterior id to prevent hacks
        for (ExteriorTemplate exteriorTemplate : getTemplate().getExteriorTemplates()) {
            if (exteriorTemplate.getId().equals(exteriorId)) {
                _exteriorId = exteriorId;
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getUid().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof House && ((House) obj).getUid().equals(getUid());
    }

    public static enum State {
        HOME(1),
        // house is owned by player
        INACTIVE(2),
        // house is not selling and not owned
        SELLING(3),
        // house is open for bids
        WAITING(4);      // house is closed for bids

        private final int _id;

        private State(int id) {
            _id = id;
        }

        public int id() {
            return _id;
        }

        public static State of(int value) {
            for (State e : values()) {
                if (e.id() == value) {
                    return e;
                }
            }

            return HOME;
        }
    }

    public static enum Access {
        PUBLIC(1),
        // open for everybody
        PROTECTED(2),
        // open for friends and legion members
        PRIVATE(3); // open for owner only (closed)

        private final int _id;

        private Access(int id) {
            _id = id;
        }

        public int id() {
            return _id;
        }

        public static Access of(int value) {
            for (Access e : values()) {
                if (e.id() == value) {
                    return e;
                }
            }

            return PRIVATE;
        }
    }

    public static enum SignOpt {
        HIDE(0),// hide owner name
        SHOW(1); // show owner name

        private final int _id;

        private SignOpt(int value) {
            _id = value;
        }

        public int id() {
            return _id;
        }

        public static SignOpt of(int value) {
            for (SignOpt e : values()) {
                if (e.id() == value) {
                    return e;
                }
            }

            return HIDE;
        }
    }

    public static enum DoorFlag {
        NONE(0),
        LEFT(1),
        // left side
        RIGHT(2),
        // right side
        BOTH(3); // both sides

        private final int _id;

        private DoorFlag(int id) {
            _id = id;
        }

        public int id() {
            return _id;
        }

        public static DoorFlag of(int value) {
            for (DoorFlag e : values()) {
                if (e.id() == value) {
                    return e;
                }
            }

            return NONE;
        }
    }

    public static enum AppearanceType {
        ROOF(1),
        OUTWALL(2),
        FRAME(3),
        DOOR(4),
        GARDEN(5),
        FENCE(6),
        INWALL_1(7),
        INWALL_2(8),
        INWALL_3(9),
        INWALL_4(10),
        INWALL_5(11),
        INWALL_6(12),
        FLOOR_1(13),
        FLOOR_2(14),
        FLOOR_3(15),
        FLOOR_4(16),
        FLOOR_5(17),
        FLOOR_6(18),
        CHIMNEY(19);

        private final int _id;

        private AppearanceType(int id) {
            _id = id;
        }

        public int getId() {
            return _id;
        }

        @Nullable
        public static AppearanceType of(int id) {
            for (AppearanceType type : AppearanceType.values()) {
                if (type.getId() == id) {
                    return type;
                }
            }

            return null;
        }
    }

    public static enum HouseType {
        FLAT(0),
        HOUSE(1),
        MANSION(2),
        ESTATE(3),
        PALACE(4);

        private final int _id;

        private HouseType(int id) {
            _id = id;
        }

        public int id() {
            return _id;
        }
    }

    public static class Trigger extends VisibleObject {

        private final HouseTemplate _houseTemplate;

        private Npc _sign, _manager, _portal;

        private Trigger(Integer objectId, Controller ctrl, WorldPosition pos,
                        HouseTemplate tpl) {
            super(objectId, ctrl, null, null, pos);
            _houseTemplate = tpl;
        }

        @Override
        public String getName() {
            return "";
        }

        public HouseTemplate getHouseTemplate() {
            return _houseTemplate;
        }

        public static Trigger create(@NotNull Integer uid, @NotNull HouseTemplate tpl) {
            WorldPosition pos =
                World.getInstance()
                    .createPosition(tpl.getMapId(), tpl.getX(), tpl.getY(), tpl.getZ(), (byte) 0, 0);

            Controller c = new Controller();
            Trigger t = new Trigger(uid, c, pos, tpl);
            t.setKnownlist(new PlayerAwareKnownList(t));
            c.setOwner(t);

            return t;
        }

        public Npc getSign() {
            return _sign;
        }

        public void setSign(Npc sign) {
            _sign = sign;
        }

        @Nullable
        public Npc getManager() {
            return _manager;
        }

        public void setManager(@Nullable Npc manager) {
            _manager = manager;
        }

        @Nullable
        public Npc getPortal() {
            return _portal;
        }

        public void setPortal(@Nullable Npc portal) {
            _portal = portal;
        }

        private static class Controller extends VisibleObjectController<Trigger> {

            @Override
            public void notSee(VisibleObject object, boolean isOutOfRange) {
                Player p = (Player) object;
                Housing.housing().tell(new Housing.DeleteHouse(p, getOwner().getHouseTemplate()));
            }

            @Override
            public void see(VisibleObject object) {
                Player p = (Player) object;
                Housing.housing().tell(new Housing.SendHouseInfo(p, getOwner().getHouseTemplate()));
            }
        }
    }

    public static class FlatTrigger extends VisibleObject {

        private FlatTrigger(int objId, VisibleObjectController<? extends VisibleObject> c,
                            WorldPosition p) {
            super(objId, c, null, null, p);
        }

        @Override
        public String getName() {
            return "";
        }

        public static FlatTrigger create(@NotNull HouseTemplate tpl, Integer ownerUid) {
            WorldPosition pos = World.getInstance().createPosition(tpl.getMapId(), tpl.getX(), tpl.getY(), tpl.getZ(), (byte) 0, 0);
            Controller c = new Controller(ownerUid);
            FlatTrigger t = new FlatTrigger(IDFactory.getInstance().nextId(), c, pos);
            t.setKnownlist(new PlayerAwareKnownList(t));
            c.setOwner(t);

            return t;
        }

        private static class Controller extends VisibleObjectController<FlatTrigger> {

            private final Integer _ownerUid;

            public Controller(Integer ownerUid) {
                _ownerUid = ownerUid;
            }

            @Override
            public void notSee(VisibleObject object, boolean isOutOfRange) {
            }

            @Override
            public void see(VisibleObject object) {
                Player p = (Player) object;
                Housing.housing().tell(new Housing.SendFlatInfo(p, _ownerUid));
            }
        }
    }

    static final class FlatNpc extends Npc {

        int flatUid;

        public FlatNpc(int objId, NpcController controller, SpawnTemplate spawnTemplate,
                       NpcTemplate objectTemplate) {
            super(objId, controller, spawnTemplate, objectTemplate);
        }
    }

    @XmlRootElement(name = "regions")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class RegionList {

        @XmlElement(name = "region", required = true)
        private List<RegionTemplate> _regionTemplates;

        @NotNull
        public List<RegionTemplate> getRegionTemplates() {
            return _regionTemplates;
        }

        public void setRegionTemplates(@NotNull List<RegionTemplate> tpls) {
            _regionTemplates = tpls;
        }
    }

    @XmlType(propOrder = {"_exteriorTemplates",
                          "_houseTemplates",
                          "_minLevel",
                          "_rent",
                          "_price",
                          "_houseType",
                          "_mapId",
                          "_uid"})
    @XmlAccessorType(XmlAccessType.NONE)
    public static class RegionTemplate {

        @XmlAttribute(name = "id", required = true)
        private Integer _uid;

        @XmlAttribute(name = "mapId", required = true)
        private int _mapId;

        @XmlAttribute(name = "type", required = true)
        private HouseType _houseType;

        @XmlAttribute(name = "price", required = true)
        private long _price;

        @XmlAttribute(name = "rent", required = true)
        private long _rent;

        @XmlAttribute(name = "minLevel", required = true)
        private int _minLevel;

        @XmlElement(name = "house", required = true)
        private List<HouseTemplate> _houseTemplates;

        @XmlElement(name = "exterior", required = true)
        private List<ExteriorTemplate> _exteriorTemplates;

        @NotNull
        public Integer getUid() {
            return _uid;
        }

        @NotNull
        public HouseType getHouseType() {
            return _houseType;
        }

        public int getMapId() {
            return _mapId;
        }

        @NotNull
        public List<HouseTemplate> getHouseTemplates() {
            return _houseTemplates;
        }

        @NotNull
        public List<ExteriorTemplate> getExteriorTemplates() {
            return _exteriorTemplates;
        }

        public void setUid(@NotNull Integer uid) {
            _uid = uid;
        }

        public void setMapId(int mapId) {
            _mapId = mapId;
        }

        public void setHouseType(@NotNull HouseType houseType) {
            _houseType = houseType;
        }

        public void setHouseTemplates(@NotNull List<HouseTemplate> tpls) {
            _houseTemplates = tpls;
        }

        public void setExteriorTemplates(@NotNull List<ExteriorTemplate> tpls) {
            _exteriorTemplates = tpls;
        }

        public long getRent() {
            return _rent;
        }

        public void setRent(long rent) {
            _rent = rent;
        }

        public int getMinLevel() {
            return _minLevel;
        }

        public void setMinLevel(int minLevel) {
            _minLevel = minLevel;
        }

        public long getPrice() {
            return _price;
        }

        public void setPrice(long price) {
            _price = price;
        }
    }

    @XmlType(propOrder = {"_portalPos", "_managerPos", "_signPos", "_z", "_y", "_x", "_doorId", "_name", "_houseId"})
    @XmlAccessorType(XmlAccessType.NONE)
    public static class HouseTemplate {

        @XmlAttribute(name = "id", required = true)
        private Integer _houseId;

        @XmlAttribute(name = "name", required = true)
        private String _name;

        @XmlAttribute(name = "doorId", required = true)
        private int _doorId;

        @XmlAttribute(name = "x", required = true)
        private float _x;

        @XmlAttribute(name = "y", required = true)
        private float _y;

        @XmlAttribute(name = "z", required = true)
        private float _z;

        @XmlElement(name = "sign", required = true)
        private Pos _signPos;

        @XmlElement(name = "manager", required = true)
        private Pos _managerPos;

        @XmlElement(name = "portal", required = true)
        private Pos _portalPos;

        @XmlTransient
        private RegionTemplate _regionTemplate;

        @NotNull
        public Integer getHouseId() {
            return _houseId;
        }

        @NotNull
        public String getName() {
            return _name;
        }

        public int getDoorId() {
            return _doorId;
        }

        public float getX() {
            return _x;
        }

        public float getY() {
            return _y;
        }

        public float getZ() {
            return _z;
        }

        public float getSignX() {
            return _signPos.getX();
        }

        public float getSignY() {
            return _signPos.getY();
        }

        public float getSignZ() {
            return _signPos.getZ();
        }

        public byte getSignH() {
            return _signPos.getH();
        }

        public float getManagerX() {
            return _managerPos.getX();
        }

        public float getManagerY() {
            return _managerPos.getY();
        }

        public float getManagerZ() {
            return _managerPos.getZ();
        }

        public byte getManagerH() {
            return _managerPos.getH();
        }

        public float getPortalX() {
            return _portalPos.getX();
        }

        public float getPortalY() {
            return _portalPos.getY();
        }

        public float getPortalZ() {
            return _portalPos.getZ();
        }

        public byte getPortalH() {
            return _portalPos.getH();
        }

        public void setSignPos(Pos pos) {
            _signPos = pos;
        }

        public void setManagerPos(Pos pos) {
            _managerPos = pos;
        }

        public void setPortalPos(Pos pos) {
            _portalPos = pos;
        }

        @NotNull
        public HouseType getType() {
            return _regionTemplate.getHouseType();
        }

        public int getMapId() {
            return _regionTemplate.getMapId();
        }

        public long getPrice() {
            return _regionTemplate.getPrice();
        }

        public int getMinLevel() {
            return _regionTemplate.getMinLevel();
        }

        public long getRent() {
            return _regionTemplate.getRent();
        }

        @NotNull
        public List<ExteriorTemplate> getExteriorTemplates() {
            return _regionTemplate.getExteriorTemplates();
        }

        public void setHouseId(@NotNull Integer houseId) {
            _houseId = houseId;
        }

        public void setName(@NotNull String name) {
            _name = name;
        }

        public void setDoorId(int doorId) {
            _doorId = doorId;
        }

        public void setX(float x) {
            _x = x;
        }

        public void setY(float y) {
            _y = y;
        }

        public void setZ(float z) {
            _z = z;
        }

        public RegionTemplate getRegionTemplate() {
            return _regionTemplate;
        }

        public void setRegionTemplate(@NotNull RegionTemplate tpl) {
            _regionTemplate = tpl;
        }

        void afterUnmarshal(Unmarshaller u, Object parent) {
            _regionTemplate = (RegionTemplate) parent;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof HouseTemplate && ((HouseTemplate) obj).getHouseId().equals(getHouseId());
        }

        @Override
        public int hashCode() {
            return getHouseId().hashCode();
        }

        @Override
        public String toString() {
            return getName() + "(" + getHouseId() + ")";
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    public static class ExteriorTemplate {

        @XmlAttribute(name = "id", required = true)
        private Integer _id;

        public Integer getId() {
            return _id;
        }

        public void setId(@NotNull Integer id) {
            _id = id;
        }
    }

    @XmlType(propOrder = {"_h", "_z", "_y", "_x"})
    @XmlAccessorType(XmlAccessType.NONE)
    public static class Pos {

        @XmlAttribute(name = "x", required = true)
        private float _x;

        @XmlAttribute(name = "y", required = true)
        private float _y;

        @XmlAttribute(name = "z", required = true)
        private float _z;

        @XmlAttribute(name = "h", required = true)
        private byte _h;

        public Pos() {
        }

        public Pos(float x, float y, float z, byte h) {
            _x = x;
            _y = y;
            _z = z;
            _h = h;
        }

        public float getX() {
            return _x;
        }

        public void setX(float x) {
            _x = x;
        }

        public float getY() {
            return _y;
        }

        public void setY(float y) {
            _y = y;
        }

        public float getZ() {
            return _z;
        }

        public void setZ(float z) {
            _z = z;
        }

        public byte getH() {
            return _h;
        }

        public void setH(byte h) {
            _h = h;
        }
    }

    //    @XmlType(propOrder = {"_type", "_name", "_id"})
    //    @XmlAccessorType(XmlAccessType.NONE)
    //    public static class AppearanceTemplate {
    //        @XmlAttribute(name = "id", required = true)
    //        private Integer _id;
    //
    //        @XmlAttribute(name = "name", required = true)
    //        private String _name;
    //
    //        @XmlAttribute(name = "type", required = true)
    //        private AppearanceType _type;
    //
    //        public Integer getId() { return _id; }
    //
    //        public void setId(final Integer id) { _id = id; }
    //
    //        public String getName() { return _name; }
    //
    //        public void setName(final String name) { _name = name; }
    //
    //        public AppearanceType getType() { return _type; }
    //
    //        public void setType(final AppearanceType type) { _type = type; }
    //    }
}
