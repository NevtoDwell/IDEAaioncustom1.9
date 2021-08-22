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
import java.util.List;
import gnu.trove.map.hash.TIntObjectHashMap;

import com.ne.gs.model.templates.factions.NpcFactionTemplate;


/**
 * @author vlog
 */
@XmlRootElement(name = "npc_factions")
@XmlAccessorType(XmlAccessType.FIELD)
public class NpcFactionsData {

    @XmlElement(name = "npc_faction", required = true)
    protected List<NpcFactionTemplate> npcFactionsData;
    private final TIntObjectHashMap<NpcFactionTemplate> factionsById = new TIntObjectHashMap<>();
    private final TIntObjectHashMap<NpcFactionTemplate> factionsByNpcId = new TIntObjectHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        factionsById.clear();
        for (NpcFactionTemplate template : npcFactionsData) {
            factionsById.put(template.getId(), template);
            if (template.getNpcId() != 0) {
                factionsByNpcId.put(template.getNpcId(), template);
            }
        }
    }

    public NpcFactionTemplate getNpcFactionById(int id) {
        return factionsById.get(id);
    }

    public NpcFactionTemplate getNpcFactionByNpcId(int id) {
        return factionsByNpcId.get(id);
    }

    public List<NpcFactionTemplate> getNpcFactionsData() {
        return npcFactionsData;
    }

    public int size() {
        return npcFactionsData.size();
    }
}
