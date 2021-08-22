/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.dataholders;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import gnu.trove.map.hash.THashMap;

import com.ne.gs.model.PlayerClass;
import com.ne.gs.model.Race;
import com.ne.gs.model.templates.item.ItemTemplate;

/**
 * This table contains all nesessary data for new players. <br/>
 * Created on: 09.08.2009 18:20:41
 *
 * @author Aquanox
 */
@XmlRootElement(name = "player_initial_data")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlayerInitialData {

    @XmlElement(name = "player_data")
    private List<PlayerCreationData> dataList;

    @XmlElement(name = "elyos_spawn_location", required = true)
    private LocationData elyosSpawnLocation;
    @XmlElement(name = "asmodian_spawn_location", required = true)
    private LocationData asmodianSpawnLocation;

    private THashMap<Race, THashMap<PlayerClass, PlayerCreationData>> data = new THashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        if (dataList == null) {
            return;
        }


        for (PlayerCreationData pt : dataList) {

            if(pt.requiredPlayerRace != Race.ELYOS && pt.requiredPlayerRace != Race.ASMODIANS)
                throw new Error("Invalid race for player initial data");

            if(!data.containsKey(pt.requiredPlayerRace))
                data.put(pt.requiredPlayerRace, new THashMap<>());

            data.get(pt.requiredPlayerRace).put(pt.getRequiredPlayerClass(), pt);
        }

        dataList = null;
    }

    public PlayerCreationData getPlayerCreationData(Race race, PlayerClass cls) {
        return data.get(race).get(cls);
    }

    public int size() {
        return data.size();
    }

    public LocationData getSpawnLocation(Race race) {
        switch (race) {
            case ASMODIANS:
                return asmodianSpawnLocation;
            case ELYOS:
                return elyosSpawnLocation;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Player creation data holder.
     */
    public static class PlayerCreationData {

        @XmlAttribute(name = "class")
        private PlayerClass requiredPlayerClass;

        @XmlAttribute(name = "race")
        private Race requiredPlayerRace;

        @XmlElement(name = "items")
        private ItemsType itemsType;

        // @XmlElement(name="shortcuts")
        // private ShortcutType shortcutData;

        PlayerClass getRequiredPlayerClass() {
            return requiredPlayerClass;
        }

        public List<ItemType> getItems() {
            return Collections.unmodifiableList(itemsType.items);
        }

        public Race getRequiredPlayerRace() {
            return requiredPlayerRace;
        }

        static class ItemsType {

            @XmlElement(name = "item")
            public List<ItemType> items = new ArrayList<>();
        }

        public static class ItemType {

            @XmlAttribute(name = "id")
            @XmlIDREF
            public ItemTemplate template;

            @XmlAttribute(name = "count")
            public int count;

            public ItemTemplate getTemplate() {
                return template;
            }

            public int getCount() {
                return count;
            }

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append("ItemType");
                sb.append("{template=").append(template);
                sb.append(", count=").append(count);
                sb.append('}');
                return sb.toString();
            }
        }

        // public static class ShortcutType
        // {
        // public List<Shortcut> shortcuts;
        // }
    }

    /**
     * Location data holder.
     */
    public static class LocationData {

        @XmlAttribute(name = "map_id")
        private int mapId;
        @XmlAttribute(name = "x")
        private float x;
        @XmlAttribute(name = "y")
        private float y;
        @XmlAttribute(name = "z")
        private float z;
        @XmlAttribute(name = "heading")
        private byte heading;

        LocationData() {
            //
        }

        public int getMapId() {
            return mapId;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getZ() {
            return z;
        }

        public byte getHeading() {
            return heading;
        }
    }

}
