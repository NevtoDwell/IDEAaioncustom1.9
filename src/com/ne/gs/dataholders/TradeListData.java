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
import com.ne.gs.model.templates.tradelist.TradeListTemplate;

/**
 * This is a container holding and serving all {@link NpcTemplate} instances.<br>
 * Briefly: Every {@link Npc} instance represents some class of NPCs among which each have the same id, name, items,
 * statistics. Data for such NPC class is defined in {@link NpcTemplate} and is uniquely identified by npc id.
 *
 * @author Luno
 */
@XmlRootElement(name = "npc_trade_list")
@XmlAccessorType(XmlAccessType.FIELD)
public class TradeListData {

    @XmlElement(name = "tradelist_template")
    private List<TradeListTemplate> tlist;

    @XmlElement(name = "trade_in_list_template")
    private List<TradeListTemplate> tInlist;

    /**
     * A map containing all trade list templates
     */
    private final TIntObjectHashMap<TradeListTemplate> npctlistData = new TIntObjectHashMap<>();

    private final TIntObjectHashMap<TradeListTemplate> npcTradeInlistData = new TIntObjectHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (TradeListTemplate npc : tlist) {
            npctlistData.put(npc.getNpcId(), npc);
        }

        for (TradeListTemplate npc : tInlist) {
            npcTradeInlistData.put(npc.getNpcId(), npc);
        }

        tlist = null;
        tInlist = null;
    }

    public int size() {
        return npctlistData.size();
    }

    /**
     * Returns an {@link TradeListTemplate} object with given id.
     *
     * @param id
     *     id of NPC
     *
     * @return TradeListTemplate object containing data about NPC with that id.
     */
    public TradeListTemplate getTradeListTemplate(int id) {
        return npctlistData.get(id);
    }

    public TradeListTemplate getTradeInListTemplate(int id) {
        return npcTradeInlistData.get(id);
    }

    /**
     * @return id of NPC.
     */
    public TIntObjectHashMap<TradeListTemplate> getTradeListTemplate() {
        return npctlistData;
    }

}
