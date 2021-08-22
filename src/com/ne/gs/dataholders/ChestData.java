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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import com.ne.gs.model.templates.chest.ChestTemplate;

/**
 * @author Wakizashi
 */
@XmlRootElement(name = "chest_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class ChestData {

    @XmlElement(name = "chest")
    private List<ChestTemplate> chests;

    /**
     * A map containing all npc templates
     */
    private final TIntObjectHashMap<ChestTemplate> chestData = new TIntObjectHashMap<>();
    private final TIntObjectHashMap<ArrayList<ChestTemplate>> instancesMap = new TIntObjectHashMap<>();
    private final THashMap<String, ChestTemplate> namedChests = new THashMap<>();

    /**
     * - Inititialize all maps for subsequent use - Don't nullify initial chest list as it will be used during reload
     *
     * @param u
     * @param parent
     */
    void afterUnmarshal(Unmarshaller u, Object parent) {
        chestData.clear();
        instancesMap.clear();
        namedChests.clear();

        for (ChestTemplate chest : chests) {
            chestData.put(chest.getNpcId(), chest);
            if (chest.getName() != null && !chest.getName().isEmpty()) {
                namedChests.put(chest.getName(), chest);
            }
        }

        chests = null;
    }

    public int size() {
        return chestData.size();
    }

    /**
     * @param npcId
     *
     * @return
     */
    public ChestTemplate getChestTemplate(int npcId) {
        return chestData.get(npcId);
    }
}
