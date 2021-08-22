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

import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.templates.npc.NpcTemplate;

/**
 * This is a container holding and serving all {@link NpcTemplate} instances.<br>
 * Briefly: Every {@link Npc} instance represents some class of NPCs among which each have the same id, name, items,
 * statistics. Data for such NPC class is defined in {@link NpcTemplate} and is uniquely identified by npc id.
 *
 * @author Luno
 */
@XmlRootElement(name = "npc_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class NpcData {

    @XmlElement(name = "npc_template")
    private List<NpcTemplate> npcs;

    /**
     * A map containing all npc templates
     */
    private final TIntObjectHashMap<NpcTemplate> npcData = new TIntObjectHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (NpcTemplate npc : npcs) {
            npcData.put(npc.getTemplateId(), npc);
        }
        npcs = null;
    }

    public int size() {
        return npcData.size();
    }

    /**
     * /** Returns an {@link NpcTemplate} object with given id.
     *
     * @param id
     *     id of NPC
     *
     * @return NpcTemplate object containing data about NPC with that id.
     */
    public NpcTemplate getNpcTemplate(int id) {
        return npcData.get(id);
    }


    /**
     * @return the npcData
     */
    public TIntObjectHashMap<NpcTemplate> getNpcData() {
        return npcData;
    }

}
