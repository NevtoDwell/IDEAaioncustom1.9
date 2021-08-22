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
import com.ne.gs.model.templates.teleport.TeleporterTemplate;

/**
 * This is a container holding and serving all {@link NpcTemplate} instances.<br>
 * Briefly: Every {@link Npc} instance represents some class of NPCs among which each have the same id, name, items,
 * statistics. Data for such NPC class is defined in {@link NpcTemplate} and is uniquely identified by npc id.
 *
 * @author orz
 */
@XmlRootElement(name = "npc_teleporter")
@XmlAccessorType(XmlAccessType.FIELD)
public class TeleporterData {

    @XmlElement(name = "teleporter_template")
    private List<TeleporterTemplate> tlist;

    /**
     * A map containing all trade list templates
     */
    private final TIntObjectHashMap<TeleporterTemplate> npctlistData = new TIntObjectHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (TeleporterTemplate template : tlist) {
            npctlistData.put(template.getTeleportId(), template);
        }

        tlist = null;
    }

    public int size() {
        return npctlistData.size();
    }

    public TeleporterTemplate getTeleporterTemplateByNpcId(int npcId) {
        for (TeleporterTemplate template : npctlistData.valueCollection()) {
            if (template.containNpc(npcId)) {
                return template;
            }
        }
        return null;
    }

    /**
     * Returns an {@link NpcTemplate} object with given id.
     *
     * @param teleportId
     *     id of NPC
     *
     * @return NpcTemplate object containing data about NPC with that id.
     */
    public TeleporterTemplate getTeleporterTemplateByTeleportId(int teleportId) {
        return npctlistData.get(teleportId);
    }
}
