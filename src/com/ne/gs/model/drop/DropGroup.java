/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.drop;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.ne.commons.utils.Rnd;
import com.ne.commons.utils.XMath;
import com.ne.gs.configs.main.DropConfig;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;
import mw.engines.geo.GeoEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dropGroup", propOrder = {"drop"})
public class DropGroup implements DropCalculator {

    private static final Logger Log = LoggerFactory.getLogger(GeoEngine.class);

    @XmlAttribute(name="droplist_id")
    protected int id;

    @XmlElement(name="drop")
    protected List<Drop> drop;

    @XmlAttribute
    protected Race race = Race.PC_ALL;

    @XmlAttribute(name = "use_category")
    protected Boolean useCategory = true;

    @XmlAttribute(name = "name")
    protected String group_name;

    public List<Drop> getDrop() {
        return drop;
    }

    public Race getRace() {
        return race;
    }

    public int getId() {
        return id;
    }

    public Boolean isUseCategory() {
        return useCategory;
    }

    public String getGroupName() {
        if (group_name == null) {
            return "";
        }
        return group_name;
    }

    @Override
    public int dropCalculator(Set<DropItem> result, int index, float dropModifier, Race race, Collection<Player> groupMembers) {


        if(drop == null)
        {
            Log.warn("Droplist was empty; Id:" + id);
            drop = new ArrayList<>();
        }

        if (DropConfig.EXPERIMENTAL_MODIFIER > 0) {
            int size = drop.size();
            List<Drop> copy = new ArrayList<>(drop);

            int count = XMath.limit(1, size / 100f * DropConfig.EXPERIMENTAL_MODIFIER, size);
            for (int i = 0; i < count; i++) {
                Drop d = copy.remove(Rnd.get(copy.size()));
                index = d.dropCalculator(result, index, dropModifier, race, groupMembers);
            }
        } else {
            if (useCategory || DropConfig.FORCE_USE_CATEGORY) {
                Drop d = drop.get(Rnd.get(0, drop.size() - 1));
                return d.dropCalculator(result, index, dropModifier, race, groupMembers);
            }

            for (Drop d : drop) {
                index = d.dropCalculator(result, index, dropModifier, race, groupMembers);
            }
        }

        return index;
    }
}
