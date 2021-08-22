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

import com.ne.gs.model.templates.panels.SkillPanel;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "polymorph_panels")
public class PanelSkillsData {

    @XmlElement(name = "panel")
    protected List<SkillPanel> templates;
    private final TIntObjectHashMap<SkillPanel> skillPanels = new TIntObjectHashMap<>();

    void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        for (SkillPanel panel : templates) {
            skillPanels.put(panel.getPanelId(), panel);
        }
        templates = null;
    }

    public SkillPanel getSkillPanel(int id) {
        return skillPanels.get(id);
    }

    public int size() {
        return skillPanels.size();
    }
}
