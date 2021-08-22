/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.housing;

import com.ne.commons.Sys;
import com.ne.commons.annotations.NotNull;
import com.ne.commons.database.SQL;
import com.ne.commons.database.TableRow;
import com.ne.gs.model.gameobjects.AionObject;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.templates.spawns.SpawnTemplate;
import com.ne.gs.spawnengine.SpawnEngine;
import com.ne.gs.world.World;

/**
 * @author hex1r0
 */
public class HouseItem extends AionObject {

    private final HouseItemTemplate _template;
    private float _x, _y, _z;
    private short _h;
    private int _cooldown, _installedTime, _installTimestamp;
    private short _usages;
    private boolean _installed, _inUse;

    protected HouseItem(Integer uniqueId, HouseItemTemplate template) {
        super(uniqueId);
        _template = template;
    }

    @Override
    public String getName() {
        return "";
    }

    public HouseItemTemplate getTemplate() {
        return _template;
    }

    public Integer getItemId() {
        return _template.id;
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

    public short getH() {
        return _h;
    }

    public void install(int mapId, int channelId, float x, float y, float z,
                        short h) {
        _installed = true;
        _x = x;
        _y = y;
        _z = z;
        _h = h;
        _installTimestamp = Sys.seconds();
    }

    public void recycle() {
        _x = 0;
        _y = 0;
        _z = 0;
        _h = 0;
        _installedTime += getInstalledTime();
        _installed = false;
    }

    public int getCooldown() {
        return _cooldown;
    }

    public int readyIn() {
        return Math.max(0, getCooldown() - Sys.seconds());
    }

    public int expiresIn() {
        return Math.max(0, getTemplate().lifetime - getInstalledTime());
    }

    public boolean isReady() {
        return readyIn() == 0;
    }

    public boolean isExpired() {
        return _template.isExpirable() && expiresIn() == 0;
    }

    public boolean isFinalUsage() {
        return getUsages() == getTemplate().usages && _template.isUseLimited();
    }

    public int getInstalledTime() {
        int total = _installedTime;
        if (isInstalled()) {
            total += Sys.seconds() - _installTimestamp;
        }

        return total;
    }

    public void setInstalledTime(int installedTime) {
        _installedTime = installedTime;
    }

    public short getUsages() {
        return _usages;
    }

    public void setCooldown(int cooldown) {
        _cooldown = cooldown;
    }

    public void setUsages(short usages) {
        _usages = usages;
    }

    public boolean isInstalled() {
        return _installed;
    }

    public boolean isInUse() {
        return _inUse;
    }

    public void setInUse(boolean inUse) {
        _inUse = inUse;
    }

    public void store(Integer playerId) {
        SQL.insertOrUpdate(HouseItemsTable.class,
            TableRow.of(HouseItemsTable.class)
                .set(HouseItemsTable.item_uid, getObjectId())
                .set(HouseItemsTable.player_id, playerId)
                .set(HouseItemsTable.item_id, getItemId())
                .set(HouseItemsTable.installed, isInstalled())
                .set(HouseItemsTable.x, getX())
                .set(HouseItemsTable.y, getY())
                .set(HouseItemsTable.z, getZ())
                .set(HouseItemsTable.h, getH())
                .set(HouseItemsTable.installedtime, getInstalledTime())
                .set(HouseItemsTable.cooldown, getCooldown())
                .set(HouseItemsTable.usages, getUsages()))
            .submit();
    }

    public void delete(Integer playerId) {
        SQL.delete(HouseItemsTable.class)
            .where(HouseItemsTable.item_uid, getObjectId())
            .and(HouseItemsTable.player_id, playerId)
            .submit();
    }

    public boolean isInstallable() {
        return Type.INSTALLABLE.matches(getTemplate().type);
    }

    public boolean isUsable() {
        return Type.USABLE.matches(getTemplate().type);
    }

    public boolean isAppearance() {
        switch (getTemplate().type) {
            case ROOF:
            case OUTWALL:
            case FRAME:
            case DOOR:
            case GARDEN:
            case FENCE:
            case INWALL_1:
            case INWALL_2:
            case INWALL_3:
            case INWALL_4:
            case INWALL_5:
            case INWALL_6:
            case FLOOR_1:
            case FLOOR_2:
            case FLOOR_3:
            case FLOOR_4:
            case FLOOR_5:
            case FLOOR_6:
            case CHIMNEY:
                return true;
            default:
                return false;
        }
    }

    public boolean isMailbox() {
        return getTemplate().type == Type.MAILBOX;
    }

    public boolean isStorage() {
        return getTemplate().type == Type.STORAGE;
    }

    public static HouseItem of(@NotNull Integer uniqueId, @NotNull HouseItemTemplate template) {
        if (template.type == Type.NPC) {
            return new HouseNpc(uniqueId, template);
        }

        return new HouseItem(uniqueId, template);
    }

    static class HouseNpc extends HouseItem {

        protected int _npcUid;

        public HouseNpc(Integer uniqueId, HouseItemTemplate template) {
            super(uniqueId, template);
        }

        public int getNpcUid() {
            return _npcUid;
        }

        @Override
        public void install(int mapId, int channelId, float x, float y, float z,
                            short h) {
            super.install(mapId, channelId, x, y, z, h);

            despawn();

            byte heading = (byte) (int) Math.ceil(h / 3.0F);
            int npcId = getTemplate().npcId;
            SpawnTemplate spawn = SpawnEngine.addNewSingleTimeSpawn(mapId, npcId, x, y, z, heading);
            _npcUid = SpawnEngine.spawnObject(spawn, channelId).getObjectId();
        }

        @Override
        public void recycle() {
            super.recycle();

            despawn();
        }

        private void despawn() {
            if (_npcUid != 0) {
                VisibleObject o = World.getInstance().findVisibleObject(_npcUid);
                if (o != null) {
                    o.getController().onDelete();
                }

                _npcUid = 0;
            }
        }
    }

    public static enum Type {
        // under mask
        PICTURE(0),
        // 1
        USABLE(1),
        // 2
        STORAGE(2),
        // 3
        MAILBOX(3),
        // 4
        CHAIR(5),
        // 5
        JUKE_BOX(6),
        // 6
        NPC(7),
        // 7

        NONE(Integer.MIN_VALUE),
        // 8
        // under mask

        ROOF(-1),
        OUTWALL(-2),
        FRAME(-3),
        DOOR(-4),
        GARDEN(-5),
        FENCE(-6),
        INWALL_1(-7),
        INWALL_2(-8),
        INWALL_3(-9),
        INWALL_4(-10),
        INWALL_5(-11),
        INWALL_6(-12),
        FLOOR_1(-13),
        FLOOR_2(-14),
        FLOOR_3(-15),
        FLOOR_4(-16),
        FLOOR_5(-17),
        FLOOR_6(-18),
        CHIMNEY(-19),

        INSTALLABLE(Integer.MAX_VALUE, PICTURE._mask | USABLE._mask | STORAGE._mask
            | MAILBOX._mask | CHAIR._mask | JUKE_BOX._mask | NPC._mask | NONE._mask);

        private final int _id;
        private final int _mask;

        private Type(int id) {
            _id = id;
            _mask = 1 << (ordinal() + 1);
        }

        private Type(int id, int mask) {
            _id = id;
            _mask = mask;
        }

        public int getId() {
            return _id;
        }

        public static Type of(int id) {
            for (Type type : Type.values()) {
                if (type.getId() == id) {
                    return type;
                }
            }

            return PICTURE;
        }

        public boolean matches(Type type) {
            return (_mask & type._mask) == type._mask;
        }
    }
}
